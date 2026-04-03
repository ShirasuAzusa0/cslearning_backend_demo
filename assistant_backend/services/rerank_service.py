import torch
import numpy as np
import logging

logger = logging.getLogger(__name__)

class RerankerService:

    def __init__(
        self,
        tokenizer,
        model,
        token_true_id,
        token_false_id,
        prefix_tokens,
        suffix_tokens,
        max_length
    ):
        self.tokenizer = tokenizer
        self.model = model
        self.token_true_id = token_true_id
        self.token_false_id = token_false_id
        self.prefix_tokens = prefix_tokens
        self.suffix_tokens = suffix_tokens
        self.max_length = max_length
        self.device = next(model.parameters()).device

        logger.info(f"RerankerService initialized on {self.device}")

    def format_instruction(self, instruction, query, doc):
        if instruction is None:
            instruction = "Given a web search query, retrieve relevant passages that answer the query"

        return f"<Instruct>: {instruction}\n<Query>: {query}\n<Document>: {doc}"

    def process_inputs(self, pairs):

        inputs = self.tokenizer(
            pairs,
            padding=False,
            truncation="longest_first",
            return_attention_mask=False,
            max_length=self.max_length - len(self.prefix_tokens) - len(self.suffix_tokens)
        )

        for i, ele in enumerate(inputs["input_ids"]):
            inputs["input_ids"][i] = self.prefix_tokens + ele + self.suffix_tokens

        inputs = self.tokenizer.pad(
            inputs,
            padding=True,
            return_tensors="pt",
            max_length=self.max_length
        )

        for key in inputs:
            inputs[key] = inputs[key].to(self.model.device)

        return inputs

    @torch.no_grad()
    def rerank(self, query, documents, batch_size: int = 8):
        if not documents:
            return []

        instruction = "Given a web search query, retrieve relevant passages that answer the query"

        pairs = [
            self.format_instruction(instruction, query, doc)
            for doc in documents
        ]

        all_scores = []

        # 分批推理，避免 GPU OOM
        for i in range(0, len(pairs), batch_size):
            batch_pairs = pairs[i:i + batch_size]

            inputs = self.process_inputs(batch_pairs)

            outputs = self.model(**inputs)
            batch_scores = outputs.logits[:, -1, :]

            yes_logits = batch_scores[:, self.token_true_id]
            no_logits = batch_scores[:, self.token_false_id]

            scores = (yes_logits - no_logits).cpu().tolist()
            all_scores.extend(scores)

        # Z-score 校正
        scores_array = np.array(all_scores)
        mean_score = np.mean(scores_array)
        std_score = np.std(scores_array)

        if std_score == 0:
            std_score = 1

        z_scores = (scores_array - mean_score) / std_score

        corrected_scores = []

        for score, z in zip(all_scores, z_scores):
            if z > 1.5:
                corrected_scores.append(mean_score + 1.5 * std_score)
            elif z < -1.5:
                corrected_scores.append(mean_score - 1.5 * std_score)
            else:
                corrected_scores.append(score)

        return corrected_scores

    def select_top_documents(
        self,
        reranked_results,
        max_tokens=3000,
        threshold_percentile=60
    ):
        scores = [r[3] for r in reranked_results]
        scores_array = np.array(scores)

        threshold = np.percentile(scores_array, threshold_percentile)
        mean_score = np.mean(scores_array)

        if threshold < mean_score * 0.6:
            threshold = mean_score * 0.7

        candidates = [r for r in reranked_results if r[3] >= threshold]

        if len(candidates) < 3:
            candidates = reranked_results[:6]

        def estimate_tokens(text):
            chinese_chars = sum(1 for c in text if '\u4e00' <= c <= '\u9fff')
            english_words = len(text.split())
            return int(chinese_chars * 1.5 + english_words * 1.3)

        selected = []
        total_tokens = 0

        for doc, meta, distance, score in candidates:
            doc_tokens = estimate_tokens(doc)

            if total_tokens + doc_tokens <= max_tokens:
                selected.append((doc, meta, distance, score))
                total_tokens += doc_tokens

        if not selected and reranked_results:
            selected = [reranked_results[0]]

        metrics = {
            "threshold": float(threshold),
            "selected_count": len(selected),
            "total_tokens": total_tokens
        }

        return selected, metrics
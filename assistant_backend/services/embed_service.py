import hashlib

import torch
import torch.nn.functional as f
from torch import Tensor
import logging
from typing import List, Dict, Tuple

from models.embed_model import JavaServiceDataItem

logger = logging.getLogger(__name__)


class EmbeddingService:
    # 构造函数
    def __init__(self, tokenizer, model, chroma_repo):
        self.tokenizer = tokenizer
        self.model = model
        self.chroma_repo = chroma_repo
        self.device = next(model.parameters()).device

        logger.info(f"EmbeddingService initialized on {self.device}")

    # 对有效 token 池化，用于生成句向量
    @staticmethod
    def last_token_pool(last_hidden_states: Tensor, attention_mask: Tensor) -> Tensor:
        left_padding = (attention_mask[:, -1].sum() == attention_mask.shape[0])

        if left_padding:
            return last_hidden_states[:, -1]
        else:
            sequence_lengths = attention_mask.sum(dim=1) - 1
            batch_size = last_hidden_states.shape[0]

            return last_hidden_states[
                torch.arange(batch_size, device=last_hidden_states.device),
                sequence_lengths
            ]

    # embedding 操作，对 context 进行向量化处理
    @torch.no_grad()
    def _embed_texts(self, texts: List[str], max_length: int = 8192):

        batch_dict = self.tokenizer(
            texts,
            padding=True,
            truncation=True,
            max_length=max_length,
            return_tensors="pt"
        )

        batch_dict = {k: v.to(self.device) for k, v in batch_dict.items()}

        outputs = self.model(**batch_dict)

        embeddings = self.last_token_pool(
            outputs.last_hidden_state,
            batch_dict["attention_mask"]
        )

        embeddings = f.normalize(embeddings, p=2, dim=1)

        return embeddings.cpu().numpy()

    # 按批次循环调用 _embed_texts，避免一次性推理过多文本
    def _batch_embed(self, texts: List[str], batch_size=None):
        # 动态 batch 适配 GPU
        if batch_size is None:
            if torch.cuda.is_available():
                total_mem = torch.cuda.get_device_properties(0).total_memory

                if total_mem < 8 * 1024 ** 3:
                    batch_size = 4
                elif total_mem < 16 * 1024 ** 3:
                    batch_size = 8
                else:
                    batch_size = 16
            else:
                batch_size = 4

        all_embeddings = []

        for i in range(0, len(texts), batch_size):
            batch = texts[i:i + batch_size]
            batch_embeddings = self._embed_texts(batch)
            all_embeddings.extend(batch_embeddings)

        return all_embeddings

    # 处理传输过来的原始数据，构造ID、生成metadata，批量embedding并存入Chroma
    def process_and_store(
            self,
            data_items: List[JavaServiceDataItem]
    ) -> Tuple[int, List[Dict]]:

        if not data_items:
            return 0, []

        ids = []
        documents = []
        metadatas = []
        texts_to_embed = []
        failed_items = []

        for item in data_items:
            try:
                # 生成稳定 hash
                content = item.context.strip()
                content_hash = hashlib.md5(content.encode("utf-8")).hexdigest()[:12]

                unique_id = f"{item.kbId}_{item.documentId}_{content_hash}"
                metadata = {
                    "kbId": item.kbId,
                    "documentId": item.documentId
                }

                ids.append(unique_id)
                documents.append(item.context)
                metadatas.append(metadata)
                texts_to_embed.append(item.context)

            except Exception as e:
                logger.error(f"Prepare failed: {str(e)}")
                failed_items.append({
                    "kbId": item.kbId,
                    "document": item.document,
                    "error": str(e)
                })

        if not texts_to_embed:
            return 0, failed_items

        try:
            embeddings = self._batch_embed(texts_to_embed)
            self.chroma_repo.add_vectors(
                ids=ids,
                embeddings=embeddings,
                metadatas=metadatas,
                documents=documents
            )
            return len(ids), failed_items

        except Exception as e:
            logger.error(f"Processing failed: {str(e)}")
            return 0, failed_items

    # 根据知识库 ID 或文档 ID 删除对应的向量数据
    def delete_vector(self, id_type: str, data_id: int):
        if id_type == 'kb':
            return self.chroma_repo.delete_by_kb_id(data_id)
        else:
            return self.chroma_repo.delete_by_document_id(data_id)

    # 获取当前向量总数和服务健康状态
    def get_statistics(self):
        total = self.chroma_repo.count()
        return {
            "total_vectors": total,
            "status": "healthy"
        }

    def embed_texts(self, texts: List[str]):
        return self._batch_embed(texts)
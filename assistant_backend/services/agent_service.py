class AgentService:

    def __init__(
        self,
        embedding_service,
        reranker_service,
        chroma_repo
    ):
        self.embedding_service = embedding_service
        self.reranker_service = reranker_service
        self.chroma_repo = chroma_repo

    def query_search(self, query_text, kb_id, top_k=10):
        # step1 embedding
        global metas, distances, docs
        query_embedding = self.embedding_service.embed_texts([query_text])[0]

        # step2 向量检索
        results = self.chroma_repo.query(
            query_embedding=query_embedding,
            kb_id=kb_id,
            top_k=top_k
        )
        if isinstance(results, list):
            # 提取各个字段
            docs = [item.get('content', '') for item in results]
            metas = [item.get('metadata', {}) for item in results]
            distances = [item.get('score', '') for item in results]

        # step3 rerank
        rerank_scores = self.reranker_service.rerank(query_text, docs)
        reranked = sorted(
            zip(docs, metas, distances, rerank_scores),
            key=lambda x: x[3],
            reverse=True
        )

        # step4 考虑token限度的动态阈值方案，筛选 rerank 后的分段
        selected_docs, metrics = self.reranker_service.select_top_documents(
            reranked_results=reranked
        )
        return {
            "documents": selected_docs,
            "metrics": metrics
        }
import chromadb
from typing import List, Dict, Any
import logging

from configs.chroma_config import ChromaConfig

logger = logging.getLogger(__name__)


class ChromaRepository:

    _client = None
    _collection = None

    # 构造函数，初始化 Chroma 仓库，加载配置，创建或复用 Client 与 Collection
    def __init__(self):

        self.config = ChromaConfig()

        if ChromaRepository._client is None:
            ChromaRepository._client = self._create_client()

        self.client = ChromaRepository._client

        if ChromaRepository._collection is None:
            ChromaRepository._collection = self._get_or_create_collection()

        self.collection = ChromaRepository._collection

        if ChromaRepository._client is None:
            ChromaRepository._client = self._create_client()
            logger.info("Chroma client created")

        if ChromaRepository._collection is None:
            ChromaRepository._collection = self._get_or_create_collection()
            logger.info(f"Collection initialized: {self.config.CHROMA_COLLECTION}")

    # 根据配置创建 HttpClient 或 PersistentClient
    def _create_client(self):
        if self.config.CHROMA_CONNECTION_TYPE == 'http':
            return chromadb.HttpClient(
                host=self.config.CHROMA_HOST,
                port=self.config.CHROMA_PORT,
                ssl=self.config.CHROMA_SSL
            )
        else:
            return chromadb.PersistentClient(
                path=self.config.CHROMA_PERSIST_DIR
            )

    # 获取已存在的集合，若不存在则创建新集合（默认 cosine 距离）
    def _get_or_create_collection(self):
        try:
            return self.client.get_collection(self.config.CHROMA_COLLECTION)
        except Exception:
            return self.client.create_collection(
                name=self.config.CHROMA_COLLECTION,
                metadata={"hnsw:space": "cosine"}
            )

    # 向指定集合中批量添加向量数据、元数据以及原始文本
    def add_vectors(
            self,
            ids: List[str],
            embeddings: List[List[float]],
            metadatas: List[Dict[str, Any]],
            documents: List[str]
    ):
        self.collection.upsert(
            ids=ids,
            embeddings=embeddings,
            metadatas=metadatas,
            documents=documents
        )

    # 根据知识库 kb_id 删除对应向量数据
    def delete_by_kb_id(self, kb_id: int):
        self.collection.delete(where={"kbId": kb_id})
        return True

    # 根据知识库中的文档的 document_id 删除对应向量数据
    def delete_by_document_id(self, document_id: int):
        self.collection.delete(where={"documentId": document_id})
        return True

    # 返回档期那集合中存储的向量总数量
    def count(self):
        return self.collection.count()

    # 根据 embedding 后的 query 再指定知识库中进行向量检索
    def query(self, query_embedding: list, kb_id: int, top_k: int):
        try:
            results = self.collection.query(
                query_embeddings=[query_embedding],
                n_results=top_k,
                where={"kbId": kb_id}
            )

            # 统一整理返回结构
            documents = results.get("documents", [[]])[0]
            metadatas = results.get("metadatas", [[]])[0]
            distances = results.get("distances", [[]])[0]
            ids = results.get("ids", [[]])[0]

            formatted_results = []

            for i in range(len(documents)):
                formatted_results.append({
                    "id": ids[i],
                    "content": documents[i],
                    "metadata": metadatas[i],
                    "score": 1 - distances[i]  # 距离转相似度
                })

            return formatted_results

        except Exception as e:
            print(f"Chroma query error: {e}")
            return []
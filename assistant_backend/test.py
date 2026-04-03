import chromadb
from chromadb.config import Settings


class VectorAdminService:
    def __init__(
        self,
        host: str = "localhost",
        port: int = 8000,
        tenant: str = "default_tenant",
        database: str = "default_database",
        collection_name: str = "qihui_kb_vectors",
    ):
        self.client = chromadb.HttpClient(
            host=host,
            port=port,
            settings=Settings(),
            tenant=tenant,
            database=database,
        )

        self.collection = self.client.get_collection(collection_name)

    # ===============================
    # 获取向量统计信息
    # ===============================
    def get_statistics(self):
        count = self.collection.count()

        stats = {
            "collection_name": self.collection.name,
            "total_vectors": count,
        }

        return stats

    # ===============================
    # 删除向量
    # ===============================
    # 按 kbId 删除
    def delete_by_kb(self, kb_id):
        self.collection.delete(
            where={"kbId": kb_id}
        )
        print(f"Deleted vectors with kbId={kb_id}")

    # 按 documentId 删除
    def delete_by_document(self, document_id):
        self.collection.delete(
            where={"documentId": document_id}
        )
        print(f"Deleted vectors with documentId={document_id}")

    # 按 id 精确删除
    def delete_by_ids(self, ids: list):
        self.collection.delete(
            ids=ids
        )
        print(f"Deleted specific ids: {ids}")

    # ===============================
    # ④ 列出所有数据（分页安全版）
    # ===============================
    def list_all_vectors(self, batch_size: int = 100, include_embeddings: bool = False):
        total = self.collection.count()
        print(f"Total vectors: {total}")

        all_data = []

        for offset in range(0, total, batch_size):
            results = self.collection.get(
                limit=batch_size,
                offset=offset,
                include=[
                    "metadatas",
                    "documents",
                ] + (["embeddings"] if include_embeddings else [])
            )

            batch_count = len(results["ids"])
            print(f"Fetched {batch_count} records (offset={offset})")

            for i in range(batch_count):
                record = {
                    "id": results["ids"][i],
                    "metadata": results["metadatas"][i],
                    "document": results["documents"][i],
                }

                if include_embeddings:
                    record["embedding"] = results["embeddings"][i]

                all_data.append(record)

        return all_data

# ===============================
# 可直接运行测试
# ===============================
if __name__ == "__main__":
    admin = VectorAdminService(
        collection_name="qihui_kb_vectors"
    )

    # 查看统计
    stats = admin.get_statistics()
    print("Statistics:", stats)

    all_vectors = admin.list_all_vectors(batch_size=50)

    print(f"Loaded {len(all_vectors)} records")
    print(all_vectors[:3])

    # 删除示例（按需打开）
    # admin.delete_by_kb(1)
    # admin.delete_by_document(100)
    # admin.delete_by_ids(["1_100_abcd1234"])
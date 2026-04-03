import os
from pathlib import Path

class ChromaConfig:
    # Chroma连接配置
    CHROMA_HOST = os.getenv('CHROMA_HOST', 'localhost')
    CHROMA_PORT = int(os.getenv('CHROMA_PORT', '8000'))
    CHROMA_SSL = os.getenv('CHROMA_SSL', 'false').lower() == 'true'

    # 持久化配置（本地模式）
    CHROMA_PERSIST_DIR = os.getenv('CHROMA_PERSIST_DIR', str(Path(__file__).parent.parent / 'chroma_data'))

    # 集合名称
    CHROMA_COLLECTION = os.getenv('CHROMA_COLLECTION', 'qihui_kb_vectors')

    # 连接方式
    CHROMA_CONNECTION_TYPE = os.getenv('CHROMA_CONNECTION_TYPE', 'http')

    @classmethod
    def get_chroma_settings(cls):
        # 获取Chroma设置
        if cls.CHROMA_CONNECTION_TYPE == 'http':
            return {
                'type': 'http',
                'host': cls.CHROMA_HOST,
                'port': cls.CHROMA_PORT,
                'ssl': cls.CHROMA_SSL
            }
        else:
            return {
                'type': 'persistent',
                'path': cls.CHROMA_PERSIST_DIR
            }
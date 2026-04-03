from flask import Flask
from flask_cors import CORS
import logging

from py_eureka_client import eureka_client

from configs.eureka_config import EUREKA_SERVER, APP_NAME, INSTANCE_HOST, INSTANCE_PORT
from configs.rabbitmq_config import RABBITMQ_URL
from repositories.chroma_repository import ChromaRepository
from services.agent_service import AgentService
from services.embed_service import EmbeddingService
from services.rerank_service import RerankerService
from utils.model_loader import get_embedding_model, get_reranker_model
from api.api_V1.agent_routes import create_agent_routes

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def create_app():
    app = Flask(__name__)

    # 解决跨域问题
    CORS(app)

    logger.info("Initializing application...")

    # 初始化 Chroma Repository
    chroma_repo = ChromaRepository()

    # 加载 embedding 模型
    embedding_tokenizer, embedding_model = get_embedding_model()

    # 加载 reranker 模型
    reranker_tokenizer, reranker_model, token_true_id, token_false_id, prefix_tokens, suffix_tokens, max_length = get_reranker_model()

    # 创建 Service
    agent_service = AgentService(
        embedding_service=EmbeddingService(
            embedding_tokenizer,
            embedding_model,
            chroma_repo
        ),
        reranker_service=RerankerService(
            reranker_tokenizer,
            reranker_model,
            token_true_id,
            token_false_id,
            prefix_tokens,
            suffix_tokens,
            max_length
        ),
        chroma_repo=chroma_repo
    )

    eureka_client.init(
        eureka_server=EUREKA_SERVER,
        app_name=APP_NAME,
        instance_host=INSTANCE_HOST,
        instance_port=INSTANCE_PORT
    )

    # 注册路由
    agent_bp = create_agent_routes(agent_service,
                                   rabbitmq_url=RABBITMQ_URL,
                                   queue_name='vectorization_queue')
    app.register_blueprint(agent_bp)

    @app.route("/")
    def index():
        return {
            "service": "Qihui-Agent Service",
            "status": "running"
        }

    return app
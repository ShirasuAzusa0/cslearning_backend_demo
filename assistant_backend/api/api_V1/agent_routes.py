from flask import Blueprint, request

from configs.rabbitmq_config import RABBITMQ_URL
from models.embed_model import JavaServiceRequest, JavaServiceDataItem
from utils.response import api_response, api_error
import logging
import threading
import pika
import json

logger = logging.getLogger(__name__)

# 带依赖注入的 Blueprint 工厂函数，设置路由前缀为'/api/agent/v1/er'
def create_agent_routes(agent_service, rabbitmq_url=RABBITMQ_URL, queue_name='vectorization_queue'):
    vector_bp = Blueprint(
        'agent',
        __name__,
        url_prefix='/api/agent/v1/er'
    )

    # ---------------- HTTP 接口 ----------------
    """
        description: 接收来自 qihui-backend 的文档分段数据，向量化后存储到 Chroma 中
        url: /api/agent/v1/er/store
        methods: POST
    """
    @vector_bp.route('/store', methods=['POST'])
    def store_vectors():
        try:
            data = request.get_json()

            if not data:
                return api_error("No data provided", code=400)

            try:
                java_request = JavaServiceRequest(**data)
            except Exception as e:
                return api_error(f"Invalid data format: {str(e)}", code=400)

            success_count, failed_items = agent_service.embedding_service.process_and_store(java_request.data)

            if failed_items:
                msg = f"Processed {success_count}/{len(java_request.data)} items"
                code = 200 if success_count > 0 else 500
            else:
                msg = f"Successfully processed {success_count} items"
                code = 200

            response_data = {
                "total_processed": success_count,
                "failed_items": failed_items
            }

            return api_response(
                data=response_data,
                message=msg,
                code=code
            )

        except Exception as e:
            logger.error(f"Unexpected error: {str(e)}")
            return api_error("Internal server error", code=500)

    """
        description: 根据知识库 kbId 或文档 documentId 删除对应的向量数据
        url: /api/agent/v1/er/delete
        methods: DELETE
    """
    @vector_bp.route('/delete', methods=['DELETE'])
    def delete_vector():
        data = request.get_json()
        if not data:
            return api_error("data is required", code=500)

        id_type = data.get("idType")
        if id_type not in ["kb", "document"]:
            return api_error("idType is required", code=500)

        data_id = data.get("dataId")
        if not data_id:
            return api_error("dataId is required", code=500)

        try:
            success = agent_service.delete_vector(id_type, data_id)
            if success and id_type == "kb":
                return api_response(message=f"Deleted vectors for kb {data_id}")
            elif success and id_type == "document":
                return api_response(message=f"Deleted vectors for document {data_id}")
            else:
                if id_type == "kb":
                    return api_error(
                        f"Failed to delete vectors for kb {data_id}",
                        code=500
                    )
                else:
                    return api_error(
                        f"Failed to delete vectors for document {data_id}",
                        code=500
                    )
        except Exception as e:
            return api_error(str(e), code=500)

    """
        description: 获取当前向量库统计信息（向量总数和服务状态）
        url: /api/agent/v1/er/stats
        methods: GET
    """
    @vector_bp.route('/stats', methods=['GET'])
    def get_stats():
        try:
            stats = agent_service.get_statistics()
            return api_response(data=stats)
        except Exception as e:
            return api_error(str(e), code=500)

    """
        description: 调用知识库获取与从 qihui-backend 传来的查询相关的分段
        url: /api/agent/v1/er/search
        methods: POST
    """
    @vector_bp.route("/search", methods=["POST"])
    def search():

        data = request.get_json()
        query = data.get("query")
        kb_id = data.get("kbId")

        result = agent_service.query_search(query, kb_id)
        return api_response(data=result)


    # ---------------- RabbitMQ 消费 ----------------
    def start_rabbitmq_consumer():
        try:
            params = pika.URLParameters(rabbitmq_url)
            connection = pika.BlockingConnection(params)
            channel = connection.channel()
            channel.queue_declare(queue=queue_name, durable=True)

            def callback(ch, method, properties, body):
                try:
                    dtos_json = json.loads(body)
                    dtos = [JavaServiceDataItem(**item) for item in dtos_json]
                    success_count, failed_items = agent_service.embedding_service.process_and_store(dtos)
                    logger.info(f"RabbitMQ batch processed: success={success_count}, failed={len(failed_items)}")
                    # 成功后手动确认
                    ch.basic_ack(delivery_tag=method.delivery_tag)
                except Exception as e:
                    logger.error(f"Failed to process RabbitMQ message: {e}")

                    # 处理失败可以选择是否重回队列
                    ch.basic_nack(
                        delivery_tag=method.delivery_tag,
                        requeue=True  # True = 重新入队，False = 丢弃
                    )

            channel.basic_consume(queue=queue_name, on_message_callback=callback, auto_ack=False)
            logger.info(f"Started RabbitMQ consumer on queue {queue_name}")
            channel.start_consuming()
        except Exception as e:
            logger.error(f"RabbitMQ consumer failed: {e}")

    # 后台线程启动 RabbitMQ 消费
    threading.Thread(target=start_rabbitmq_consumer, daemon=True).start()

    return vector_bp
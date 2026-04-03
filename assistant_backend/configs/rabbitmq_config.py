import os

RABBITMQ_DEFAULT_USER = os.getenv("RABBITMQ_DEFAULT_USER", "admin")
RABBITMQ_DEFAULT_PASS = os.getenv("RABBITMQ_DEFAULT_PASS", "admin123")
RABBITMQ_URL = os.getenv("RABBITMQ_URL", "amqp://admin:admin123@localhost:5672/%2F")

RabbitMQ_config = [
    RABBITMQ_URL,
    RABBITMQ_DEFAULT_USER,
    RABBITMQ_DEFAULT_PASS
]
import os

EUREKA_SERVER = os.getenv("EUREKA_SERVER", "http://localhost:8280/eureka")
APP_NAME = os.getenv("APP_NAME", "ai-agent-service")
INSTANCE_PORT = os.getenv("INSTANCE_PORT", 5050)
INSTANCE_HOST = os.getenv("INSTANCE_HOST", "localhost")
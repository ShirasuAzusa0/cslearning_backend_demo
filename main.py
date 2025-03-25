#from resources import app
from resources import create_app

app = create_app()

# 后端程序运行入口（调试模式启动，端口为5000）
if __name__ == "__main__":
    app.run(host='0.0.0.0', debug=False, port=5000)
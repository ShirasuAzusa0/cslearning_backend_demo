from resources import app

# 后端程序运行入口（调试模式启动，端口为5000）
if __name__ == "__main__":
    app.run(host='0.0.0.0', debug=False, port=5050)
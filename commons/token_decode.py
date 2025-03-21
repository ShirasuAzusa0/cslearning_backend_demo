from commons.contants import LOGIN_SECRET
from flask import request
import jwt

# 解析客户端发送过来的token，还原其中的信息
def token_decode():
    jwt_token = request.headers.get('Authorization', None)
    user_info = jwt.decode(jwt_token, LOGIN_SECRET, algorithms='HS256')
    return user_info
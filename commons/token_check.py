from functools import wraps

from commons.contants import LOGIN_SECRET
from flask import request
import jwt

# 封装器实现验证客户端请求中所携带的JWT令牌（token），确保用户已经登录且身份合法，保护被装饰的接口
def token_required(f):
    @wraps(f)
    def wrapper(*args, **kwargs):
        # 获取token
        jwt_token = request.headers.get('Authorization', None)
        if not jwt_token:
            return {'status':'fail', 'msg': 'User unauthorized'},401
        try:
            # 解码token，并验证令牌中是否有有效的email字段（该字段唯一）
            user_info = jwt.decode(jwt_token, LOGIN_SECRET, algorithms='HS256')
            if not user_info or not user_info.get('attributes', None).get('email', None):
                return {'status':'fail', 'msg': 'User unauthorized'},401
        except Exception as e:
            return {'status':'fail', 'msg':f'{e}'},401
            #return {'error': 'User unauthorized'},401
        result = f(*args, **kwargs)
        return result
    return wrapper
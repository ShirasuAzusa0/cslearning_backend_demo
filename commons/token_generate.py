import jwt
import datetime
from commons.contants import LOGIN_SECRET

def generate_token(user_json):
    # 设置token的有效时限，此处设置为一天（注意使用now()函数而不是utcnow()函数，因为后者由于时差问题并不准确，基本已被弃用）
    expiration_time = datetime.datetime.now() + datetime.timedelta(hours=24)
    # 有效时限要通过转换为时间戳并转为int整数才可组装入token的payload部分（json格式），这样在身份验证的时候就不会因为时间数据的类型不对而报错了
    user_json['exp'] = int(expiration_time.timestamp())
    # 生成一个JWT的token令牌，采用HS256算法，自定义密钥为LOGIN_SECRET
    return jwt.encode(user_json, LOGIN_SECRET, algorithm='HS256')
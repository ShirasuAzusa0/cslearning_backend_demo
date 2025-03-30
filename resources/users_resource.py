from flask import request, send_file
from flask_restful import Resource, reqparse
from sqlalchemy import VARCHAR
from datetime import datetime
from werkzeug.datastructures import FileStorage

from commons import utils
from commons.RSAkey_load import load_private_key
from commons.token_check import token_required
from commons.token_decode import token_decode
from commons.token_generate import generate_token
from models.users_model import UsersModel
from resources import api
from services.users_service import UsersService

from cryptography.hazmat.primitives.asymmetric import padding
import base64


class RegisterResource(Resource):
    def __init__(self):
        # 加载私钥
        self.private_key = load_private_key()

    # 处理注册请求
    def post(self):# 继承自flask-restful的Resource的类，定义了处理前端HTTP请求的方法（如GET、POST、PUT、DELETE）
        id = request.args.get('id', None)
        avatarUrl = request.args.get('avatarUrl', None)
        userName = request.args.get('userName', None)
        email = request.args.get('email', None)
        password = request.args.get('password', None)
        type = request.args.get('type', None)
        # 通过私钥解密密码
        try:
            encrypted_password = base64.b64decode(password)  # 将Base64编码的密码字符串解码为字节数据
            user_password = self.private_key.decrypt(  # 使用私钥对解码后的字节数据进行解密
                encrypted_password,
                padding.PKCS1v15()
            ).decode('utf-8')  # 解密后的字节数据解码为UTF-8字符串
        except Exception as e:
            return {'status': 'fail', 'msg': f'解密失败：{e}'}, 400

        user_model = UsersModel(id=id,
                                avatarUrl=avatarUrl,
                                userName=userName,
                                email=email,
                                password=password,
                                type=type,
                                selfDescription='这个用户很懒,什么都没有留下',
                                registerDate=datetime.now(),
                                reply=0, topics=0, follower=0, following=0)
        user_model = UsersService().register(user_model)
        if user_model:
            user_json = user_model.serialize_1()
            # 生成一个JWT的token令牌
            jwt_token = generate_token(user_json)
            # 直接给前端返回token即可
            return {'Authorization': jwt_token}
        else:
            return {"error":"User has existed"},412

class LoginResource(Resource):
    def __init__(self):
        # 加载私钥
        self.private_key = load_private_key()

    # 处理登录请求
    def post(self):
        account = request.json.get('account', None)
        user_password = request.json.get('password', None)

        # 通过私钥解密密码
        try:
            encrypted_password = base64.b64decode(user_password)  # 将Base64编码的密码字符串解码为字节数据
            user_password = self.private_key.decrypt(  # 使用私钥对解码后的字节数据进行解密
                encrypted_password,
                padding.PKCS1v15()
            ).decode('utf-8')  # 解密后的字节数据解码为UTF-8字符串
        except Exception as e:
            return {'status': 'fail', 'msg': f'解密失败：{e}'}, 400

        user_model = UsersService().login(account, user_password)
        if user_model:
            user_json = user_model.serialize_mode1()
            # 生成一个JWT的token令牌
            jwt_token = generate_token(user_json)
            # 直接给前端返回token即可
            return {
                'status': 'success',
                'msg': '登录成功',
                'data': {
                    'userId': str(user_model.id),
                    'token': jwt_token
                }
            }
        else:
            return {'status': 'fail', 'msg': '用户输入的邮箱或密码不正确'},404

class LogoutResource(Resource):
    # 处理登出请求
    @token_required
    def get(self):
        user_info = token_decode()
        user_id = user_info.get('id', None)
        user_model = UsersService().logout(user_id)
        if user_model:
            return {
                'status': 'log out',
                'user': str(user_model.userName)
            }

class LogoffResource(Resource):
    # 处理注销请求
    @token_required
    def delete(self):
        user_info = token_decode()
        user_id = user_info.get('id', None)
        UsersService().logoff(user_id)
        return {'status':'logoff successfully'}

class SettingResource(Resource):
    # 初始化方法
    def __init__(self):
        # 定义一个语法分析器parser，其值为RequestParser函数，用于处理请求参数的输入
        self.parser = reqparse.RequestParser()
        # 通过add_argument方法定义需要解析的参数
        self.parser.add_argument("avatar",  # 参数名称
                                 required=True,  # 确保上述参数必须存在
                                 type=FileStorage,  # 文件存储类型
                                 location="files",  # 提取参数的位置，将数据转换成文件存储
                                 help="Please private avatar file")  # 请求中没有参数则报错的内容
        # 加载私钥
        self.private_key = load_private_key()

    # 处理用户信息修改请求
    @token_required
    def put(self):
        user_email = request.form.get('email', None)
        user_name = request.form.get('userName', None)
        user_description = request.form.get('selfDescription', None)
        user_password = request.form.get('password', None)
        password_again = request.form.get('password_again', None)

        # 通过私钥解密密码
        try:
            encrypted_password = base64.b64decode(user_password)    # 将Base64编码的密码字符串解码为字节数据
            user_password = self.private_key.decrypt(               # 使用私钥对解码后的字节数据进行解密
                encrypted_password,
                padding.PKCS1v15()
            ).decode('utf-8')                                       # 解密后的字节数据解码为UTF-8字符串'''

           # encrypted_password = base64.b64decode(password_again)
           # password_again = self.private_key.decrypt(
           #     encrypted_password,
           #     padding.PKCS1v15()
           # ).decode('utf-8')
        except Exception as e:
            return {'status': 'fail', 'msg': f'解密失败：{e}'}, 400

       # if user_password != password_again:
       #     return {'status': 'fail', 'msg': '两次输入的密码不一致'}, 400

        # 获取文件，通过self.parser.parse_args()解析请求参数并从中获取名为attachment的文件
        attachment_file = self.parser.parse_args().get("avatar")
        # 上传内容只允许为图片
        allowed_extensions = {'png', 'jpg', 'jpeg', 'gif'}
        if not attachment_file or attachment_file.filename == '':
            return {'status': 'fail', 'msg': 'No file part'}, 400
        # 匹配文件拓展名
        file_extension = attachment_file.filename.rsplit('.', 1)[1].lower()
        # 检查文件拓展名是否为允许的图片格式
        if file_extension not in allowed_extensions:
            return {'status': 'fail', 'msg': 'Invalid file format'}, 400
        # 提取文件名
        # filename = attachment_file.filename
        # 生成文件路径
        # 不要传参传成(attachment_file,filename),会报错argument should be a str or an os.PathLike object where __fspath__ returns a str, not 'FileStorage'
        save_path, avatar_path = utils.get_attachment_path()
        # 拼接绝对路径，用于保存头像文件
        save_path = save_path.joinpath(attachment_file.filename)
        # 相对路径，保存在数据库中和供前端调用
        avatar_path = avatar_path.joinpath(attachment_file.filename)
        # 将文件按当前路径保存
        attachment_file.save(save_path)
        # 解析token获取用户id
        user_info = token_decode()
        user_id = user_info.get('id', None)
        user_model = UsersService().update_user_data(user_id, avatar_path, user_email, user_name, user_description, user_password)
        if user_model:
            # 构建用户修改信息字典
            res = {
                'email': user_email,
                'userName': user_name,
                'selfDescription': user_description,
                'password': user_password,
                'avatarUrl': str(avatar_path)
            }
            # 构造返回数据模型
            result = {
                'status': 'success',
                'msg': '用户信息更新成功',
                'data': {}
            }
            # 遍历获取用户修改的数据
            for key, value in res.items():
                if value is not None:
                    result['data'][key] = value
            return result
        else:
            return {'status': 'fail', 'msg':'用户信息更新失败'}, 404

class UserPageResource(Resource):
    # 获取用户主页信息
    @ token_required
    def get(self, userId: VARCHAR):
        user_model = UsersService().get_user_by_id(userId)
        if user_model is not None:
            return user_model.serialize_mode2()
        else:
            return {'status': 'fail', 'message': '找不到该用户', 'data': None},404

# 管理员登录界面（暂时）
class AdminLoginResource(Resource):
    # 登录方法
    def post(self):
        account = request.json.get('account', None)
        type = request.json.get('type', None)
        password = request.json.get('password', None)
        if type != 'admin':
            return {'status': 'fail', 'msg': '非管理员，无权限登录', 'data': None},401
        user_model = UsersService().login(account, password)
        if user_model:
            user_json = user_model.serialize_mode1()
            # 生成一个JWT的token令牌
            jwt_token = generate_token(user_json)
            # 直接给前端返回token即可
            return {
                'status': 'success',
                'msg': '登录成功',
                'data': {
                    'userId': str(user_model.id),
                    'token': jwt_token
                }
            }

api.add_resource(RegisterResource, '/register')
api.add_resource(LoginResource, '/login')
api.add_resource(LogoutResource, '/user/logout')
api.add_resource(LogoffResource, '/user/logoff')
api.add_resource(SettingResource, '/user/setting')
api.add_resource(UserPageResource, '/user/profile/<string:userId>')
api.add_resource(AdminLoginResource, '/login/admin')
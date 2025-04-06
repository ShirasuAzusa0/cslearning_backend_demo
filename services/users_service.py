from sqlalchemy import VARCHAR, Select, func, asc

from models.users_model import UsersModel
from resources import db
from datetime import datetime

# 启用数据库服务的类，定义了对数据库的增、删、改、查等各类操作，并将结果返回
class UsersService:
    def get_total_user(self):
        return db.session.query(func.count(UsersModel.id)).scalar()

    def generate_userId(self):
        num = self.get_total_user()
        userId = 'user_' + str(num+1)
        return userId

    def get_user_by_email(self, user_email:VARCHAR):
        # 使用db.session这一与数据库交互的对象，调用get方法从数据库中查找单一数据记录，并将其返回
        return UsersModel.query.filter_by(email=user_email).first()

    def get_user_by_id(self, user_id:VARCHAR):
        # 使用db.session这一与数据库交互的对象，调用get方法根据主键（primary_key）从数据库中查找单一数据记录，并将其返回
        return db.session.get(UsersModel, user_id)

    # 更新用户信息方法
    def update_user_data(self, id:VARCHAR, avatar:VARCHAR, email:VARCHAR, name:VARCHAR, description:VARCHAR, password:VARCHAR):
        user_model = self.get_user_by_id(id)
        if user_model:
            # 构建更新项字典
            updates = {
                'email': email,
                'userName': name,
                'selfDescription': description,
                'password': password,
                'avatarUrl': avatar
            }
            # 遍历字典的所有键值对
            for key, value in updates.items():
                if value is not None:
                    # 通过setattr()动态设置对象属性，将对象user_model的key属性赋值为value
                    setattr(user_model, key, value)
            db.session.commit()
            return user_model

    # 注册方法
    def signup(self, user_model:UsersModel):
        # exist_user = self.get_user_by_id(user_model.id)
        exist_user = self.get_user_by_email(user_model.email)
        # 若用户已经存在则抛出异常
        if exist_user:
            # raise Exception(f'User exists with email"{user_model.email}"')
            return None
        # 将一个数据对象user_model添加到当前的数据库对话（session）中
        db.session.add(user_model)
        db.session.commit()
        return user_model

    # 登录方法
    def login(self, user_email:VARCHAR, user_password:VARCHAR):
        # 通过email找到第一个匹配的的用户
        query = Select(UsersModel).where(UsersModel.email == user_email)
        user_model = db.session.scalars(query).first()
        # 对比其密码
        if user_model and user_model.password == user_password:
            user_model.registerDate = datetime.now()
            db.session.commit()
            return user_model
        else:
            return None

    # 登出方法
    def logout(self, user_id:VARCHAR):
        user_model = self.get_user_by_id(user_id)
        return user_model

    # 注销方法
    def logoff(self, user_id: VARCHAR):
        user_model = self.get_user_by_id(user_id)
        db.session.delete(user_model)
        db.session.commit()
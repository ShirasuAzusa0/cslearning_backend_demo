from sqlalchemy import VARCHAR, Select, asc

from models.posts_model import PostsModel
from resources import db

# 启用数据库服务的类，定义了对数据库的增、删、改、查等各类操作，并将结果返回
class PostsService:
    # 获取帖子的具体内容
    def get_post_by_id(self, postId:VARCHAR):
        # 使用db.session这一与数据库交互的对象，调用get方法根据主键（primary_key）从数据库中查找单一数据记录，并将其返回
        return db.session.get(PostsModel, postId)

    # 获取所有帖子的方法
    def get_all_posts(self):
        query = Select(PostsModel).order_by(asc(PostsModel.postId))
        return db.session.scalars(query).all()

    # 获取指定数量的帖子的方法
    def get_limited_posts(self, limit:int = 20, start:int = 0, method:int = 0):
        if method == 0:
            query = Select(PostsModel).order_by(asc(PostsModel.lastCommentedAt))
        elif method == 1:
            query = Select(PostsModel).order_by(asc(PostsModel.likesCount))
        elif method == 2:
            query = Select(PostsModel).order_by(asc(PostsModel.createdAt))
        else:
            return None

        if limit is not None:
            query = query.limit(limit).offset(start)
        return db.session.scalars(query).all()


from sqlalchemy import VARCHAR, Select, asc

from models.categories_model import CategoriesModel
from resources import db
from datetime import datetime

# 启用数据库服务的类，定义了对数据库的增、删、改、查等各类操作，并将结果返回
class CategoriesService:
    # 获取所有标签的方法
    def get_all_tags(self):
        query = Select(CategoriesModel).order_by(asc(CategoriesModel.tagId))
        return db.session.scalars(query).all()

    # 获取指定标签对应的帖子列表
    def get_post_list_by_tagName(self, tag_name:VARCHAR):
        return CategoriesModel.query.filter_by(tagName=tag_name).first()
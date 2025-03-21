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
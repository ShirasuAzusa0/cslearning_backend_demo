from sqlalchemy import VARCHAR, Select, asc

from models.comments_model import CommentsModel
from resources import db

# 启用数据库服务的类，定义了对数据库的增、删、改、查等各类操作，并将结果返回
class CommentsService:
    # 根据创建时间搜索
    pass
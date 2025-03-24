from sqlalchemy import VARCHAR, JSON, asc
from sqlalchemy.sql import Select

from models.crawlerdatas_model import CrawlerDatasModel
from models.crawlerids_model import CrawlerIdsModel
from resources import db

# 启用数据库服务的类，定义了对数据库的增、删、改、查等各类操作，并将结果返回
class CrawlerService:
    # 获取预爬取的内容的id关键字
    def get_data_by_id(self, dataId:VARCHAR):
        return db.session.get(CrawlerIdsModel, dataId)

    # 获取预爬取的内容
    def get_all_datas_by_id(self, dataId:VARCHAR):
        query = Select(CrawlerDatasModel).where(CrawlerDatasModel.dataId == dataId).order_by(asc(CrawlerDatasModel.dataId))
        return db.session.scalars(query).all()

    # 更新保存到数据库预爬取内容
    def update_data(self, dataId:VARCHAR, crawlerData:JSON):
        new_crawlerData = CrawlerDatasModel(dataId=dataId, crawlerData=crawlerData)
        db.session.add(new_crawlerData)
        db.session.commit()

    # 更新保存到数据库的预爬取内容关键字
    def update_id(self, dataId:VARCHAR):
        new_crawlerId = CrawlerIdsModel(dataId=dataId)
        db.session.add(new_crawlerId)
        db.session.commit()
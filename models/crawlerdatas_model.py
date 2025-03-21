from sqlalchemy import VARCHAR, JSON, INT
from sqlalchemy.orm import Mapped, mapped_column, relationship

from resources import db

class CrawlerDatasModel(db.Model):
    __tablename__ = 'crawler_datas'
    Id: Mapped[INT] = mapped_column(INT, primary_key=True, nullable=False)
    dataId: Mapped[VARCHAR] = mapped_column(VARCHAR(255), nullable=False)
    crawlerData: Mapped[JSON] = mapped_column(JSON, nullable=False)

    # 构建 CrawlerDatasModel 到 CrawlerIdsModel 的外键映射反向关系
    ids = relationship("CrawlerIdsModel", back_populates="datas")

    # 序列化方法，需要序列化为json数据后再传输给前端
    def serialize(self):
        return self.crawlerData
from sqlalchemy import VARCHAR, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column, relationship

from resources import db

class CrawlerIdsModel(db.Model):
    __tablename__ = 'crawler_ids'
    dataId: Mapped[VARCHAR] = mapped_column(VARCHAR(255), ForeignKey('crawler_datas.dataId'), primary_key=True, nullable=False)

    # 构建 CrawlerIdsModel 到 CrawlerDatasModel 的外键映射关系
    datas = relationship("CrawlerDatasModel", back_populates="ids")
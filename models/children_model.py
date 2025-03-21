from sqlalchemy import VARCHAR, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column,relationship
from resources import db

class ChildrenModel(db.Model):
    __tablename__ = 'children'
    messageId: Mapped[VARCHAR] = mapped_column(VARCHAR(255), ForeignKey('messages.messageId'), nullable=False)
    childrenId: Mapped[VARCHAR] = mapped_column(VARCHAR(255), primary_key=True, nullable=False)

    # 在 ChildrenModel 中添加 messages 反向关系
    messages = relationship("MessagesModel", back_populates="children")

    # 序列化方法，需要序列化为json数据后再传输给前端
    def serialize(self):
        return self.childrenId
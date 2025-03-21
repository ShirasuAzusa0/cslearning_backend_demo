from sqlalchemy import VARCHAR, Enum, DATETIME, INT
from sqlalchemy.orm import Mapped, mapped_column,relationship
from resources import db

# 数据库mysql映射类，该类的对象用来暂存从数据库中读取出来的数据，通过格式化为json数据后返回给前端
class UsersModel(db.Model):
    __tablename__ = 'users'
    id: Mapped[VARCHAR] = mapped_column(VARCHAR(255),primary_key=True, nullable=False)
    avatarUrl: Mapped[VARCHAR] = mapped_column(VARCHAR(255), nullable=False)
    userName: Mapped[VARCHAR] = mapped_column(VARCHAR(255), nullable=False)
    email: Mapped[VARCHAR] = mapped_column(VARCHAR(255), nullable=False)
    password: Mapped[VARCHAR] = mapped_column(VARCHAR(255), nullable=False)
    type: Mapped[Enum] = mapped_column(Enum('admin','user'), nullable=False)
    selfDescription: Mapped[VARCHAR] = mapped_column(VARCHAR(255), nullable=False)
    registerDate: Mapped[DATETIME] = mapped_column(DATETIME, nullable=False)
    reply: Mapped[INT] = mapped_column(INT, nullable=False)
    topics: Mapped[INT] = mapped_column(INT, nullable=False)
    follower: Mapped[INT] = mapped_column(INT, nullable=False)
    following: Mapped[INT] = mapped_column(INT, nullable=False)

    # 在 UsersModel 中添加 posts 反向关系
    posts = relationship("PostsModel", back_populates="author")

    # 在 UsersModel 中添加 comments 反向关系
    comments = relationship("CommentsModel", back_populates="author")

    # 在 UserModel 中添加 conversations 反向关系
    conversations = relationship("ConversationsModel", back_populates="author")

    # 序列化方法，需要序列化为json数据后再传输给前端
    def serialize_mode1(self):
        return {
            'id': str(self.id),
            'attributes': {
                'avatarUrl': str(self.avatarUrl),
                'userName': str(self.userName),
                'email': str(self.email),
                'type': str(self.type)
            }
        }

    def serialize_mode2(self):
        return {
            'status': 'success',
            'message': '成功获取用户信息',
            'data': {
                'userId': str(self.id),
                'userName': str(self.userName),
                'email': str(self.email),
                'avatar': str(self.avatarUrl),
                'selfDescription': str(self.selfDescription),
                'registerDate': self.registerDate.isoformat().replace('T',' '),
                'counts': {
                    'reply': self.reply,
                    'topics': self.topics,
                    'follower': self.follower,
                    'following': self.following
                }
            }
        }

    def serialize_mode3(self):
        return {
            'status': 'success',
            'msg': '用户信息更新成功',
            'data': {
                'userName': str(self.userName),
                'email': str(self.email),
                'avatar':str(self.avatarUrl),
                'selfDescription': str(self.selfDescription),
            }
        }

    def serialize_mode4(self):
        return {
            'id': str(self.id),
            'userName': str(self.userName)
        }
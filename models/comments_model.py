from sqlalchemy import VARCHAR, INT, BIGINT, DATETIME,ForeignKey
from sqlalchemy.dialects.mysql import  LONGTEXT
from sqlalchemy.orm import Mapped, mapped_column, relationship
from resources import db

from models.comment_like_model import comment_like

class CommentsModel(db.Model):
    __tablename__ = 'comments'
    commentId: Mapped[VARCHAR] = mapped_column(VARCHAR(255), primary_key=True, nullable=False)
    content: Mapped[LONGTEXT] = mapped_column(LONGTEXT, nullable=False)
    authorId: Mapped[VARCHAR] = mapped_column(VARCHAR(255), ForeignKey('users.id'), nullable=False)
    createdAt: Mapped[DATETIME] = mapped_column(DATETIME, nullable=False)
    likesCount: Mapped[INT] = mapped_column(INT, nullable=False)
    repliedID: Mapped[VARCHAR] = mapped_column(VARCHAR(255), nullable=True)
    postId: Mapped[VARCHAR] = mapped_column(VARCHAR(255), ForeignKey('posts.postId'), nullable=False)

    # 定义comments成员变量与UsersModel映射类的关系（多对多关系）
    userCommentLiked = relationship("UsersModel", back_populates="commentLiked")

    # 定义author成员变量与UsersModel映射类的关系
    author = relationship("UsersModel", back_populates="comments")

    # 定义posts成员变量与PostsModel映射类的关系
    posts = relationship("PostsModel", back_populates="comments")

    def serialize(self):
        return {
            'commentId': str(self.commentId),
            'author': self.author.serialize_mode1() if self.author else None,
            'content': str(self.content),
            'createdAt': self.createdAt.isoformat().replace('T',' '),
            'likesCount': self.likesCount,
            'repliedID': str(self.repliedID)
        }
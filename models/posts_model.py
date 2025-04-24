from sqlalchemy import VARCHAR, DATETIME, INT, ForeignKey
from sqlalchemy.dialects.mysql import  LONGTEXT
from sqlalchemy.orm import Mapped, mapped_column, relationship

from models.post_categories_model import post_categories
from models.post_like_model import post_like
from models.favorites_model import favorites
from resources import db


class PostsModel(db.Model):
    __tablename__ = 'posts'
    postId: Mapped[VARCHAR] = mapped_column(VARCHAR(255), primary_key=True, nullable=False)
    title: Mapped[VARCHAR] = mapped_column(VARCHAR(255), nullable=False)
    authorId: Mapped[VARCHAR] = mapped_column(VARCHAR(255), ForeignKey('users.id'), nullable=False)
    createdAt: Mapped[DATETIME] = mapped_column(DATETIME, nullable=False)
    lastCommentedAt: Mapped[DATETIME] = mapped_column(DATETIME, nullable=True)
    lastCommentedUserId: Mapped[VARCHAR] = mapped_column(VARCHAR(255), ForeignKey('users.id'), nullable=True)
    commentsCount: Mapped[INT] = mapped_column(INT, nullable=False)
    likesCount: Mapped[INT] = mapped_column(INT, nullable=False)
    content: Mapped[LONGTEXT] = mapped_column(LONGTEXT, nullable=False)

    # 定义 author 成员变量与 UsersModel 映射类的关系，明确指定 author 关系使用 authorId 外键
    author = relationship("UsersModel", foreign_keys=[authorId], back_populates="posts")

    # 定义categories成员变量与CategoriesModel映射类的关系(多对多关系)，表明了categories是一个列表，列内元素是categories表映射实例
    #categories: Mapped[list["CategoriesModel"]] = relationship("CategoriesModel", secondary=post_categories, back_populates="posts")
    categories = relationship("CategoriesModel", secondary=post_categories, back_populates="posts")

    # 定义like成员变量与userModel映射类的关系(多对多关系)
    userLiked = relationship("UsersModel", secondary=post_like, back_populates="postLiked")

    # 定义favorite成员变量与userModel映射类的关系(多对多关系)
    userFavorite = relationship("UsersModel", secondary=favorites, back_populates="postFavorite")

    # 定义comments成员变量与CommentsModel映射类的关系
    comments = relationship("CommentsModel", back_populates="posts")

    # 序列化方法，需要序列化为json数据后再传输给前端
    def serialize_mode1(self):
        return {
            'postId': str(self.postId),
            'title': str(self.title),
            'author': self.author.serialize_mode1() if self.author else None,
            'tags': [category.serialize_mode1() for category in self.categories],
            'createdAt': self.createdAt.isoformat().replace('T',' ') if self.createdAt else None,
            'lastCommentedAt': self.lastCommentedAt.isoformat().replace('T',' ') if self.lastCommentedAt else None,
            'lastCommentedUser': self.author.serialize_mode4() if self.author else None,
            "commentsCount": self.commentsCount
        }

    # 序列化方法，需要序列化为json数据后在传输给前端
    def serialize_mode2(self):
        return {
            'postId': str(self.postId),
            'title': str(self.title),
            'author': self.author.serialize_mode1() if self.author else None,
            'tags': [category.serialize_mode1() for category in self.categories],
            'createdAt': self.createdAt.isoformat().replace('T',' ') if self.createdAt else None,
            'lastCommentedAt': self.lastCommentedAt.isoformat().replace('T',' ') if self.lastCommentedAt else None,
            "commentsCount": self.commentsCount,
            "likesCount": self.likesCount,
            "content": str(self.content),
            "comments": [comment.serialize() for comment in self.comments]
        }
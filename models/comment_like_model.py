from sqlalchemy import ForeignKey, Table, Column
from resources import db

# 构建并引用关系表comment_like
comment_like = Table(
    "comment_like",
    db.metadata,
    Column("commentId", ForeignKey("comments.commentId"), primary_key=True, nullable=False),
    Column("userId", ForeignKey("users.id"), primary_key=True, nullable=False)
)
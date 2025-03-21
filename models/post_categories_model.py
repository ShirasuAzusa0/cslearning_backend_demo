from sqlalchemy import ForeignKey, Table, Column
from resources import db

# 构建并引用关系表post_categories
post_categories = Table(
    "post_categories",
    db.metadata,
    Column("postId", ForeignKey("posts.postId"), primary_key=True, nullable=False),
    Column("tagId", ForeignKey("categories.tagId"), primary_key=True, nullable=False)
)
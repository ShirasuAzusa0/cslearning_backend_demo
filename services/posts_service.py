from datetime import datetime

from sqlalchemy import VARCHAR, Select, select, asc, desc, func, exists

from models import CategoriesModel
from models.favorites_model import favorites
from models.posts_model import PostsModel
from models.post_like_model import post_like
from models.post_categories_model import post_categories
from resources import db
from services.categories_service import CategoriesService


# 启用数据库服务的类，定义了对数据库的增、删、改、查等各类操作，并将结果返回
class PostsService:
    # 获取帖子的具体内容
    def get_post_by_id(self, postId:VARCHAR):
        # 使用db.session这一与数据库交互的对象，调用get方法根据主键（primary_key）从数据库中查找单一数据记录，并将其返回
        return db.session.get(PostsModel, postId)

    # 获取目前的帖子总数量
    def get_total_posts(self):
        return db.session.query(func.count(PostsModel.postId)).scalar()

    # 生成帖子编号
    def generate_postId(self):
        num = self.get_total_posts()
        postId = 'post_' + str(num + 1)
        return postId

    # 保存帖子到数据库中
    def save_post(self, new_post:PostsModel, tags:list):
        db.session.add(new_post)
        db.session.commit()
        # 保存帖子的标签分类
        for tag in tags:
            insert_stmt = post_categories.insert().values(
                postId=new_post.postId,
                tagId=tag["tagId"]
            )
            db.session.execute(insert_stmt)
            db.session.commit()
            tag_model = CategoriesService().get_tag_by_id(tag["tagId"])
            tag_model.lastPostTime = func.now()
            tag_model.postsCount += 1
            db.session.commit()
        return new_post

    # 获取所有帖子的方法
    def get_all_posts(self):
        query = Select(PostsModel).order_by(asc(PostsModel.postId))
        return db.session.scalars(query).all()

    # 获取指定数量的帖子的方法
    def get_limited_posts(self, limit:int = 20, start:int = 0, method:int = 0):
        if method == 0:
            query = Select(PostsModel).order_by(asc(PostsModel.lastCommentedAt))
        elif method == 1:
            query = Select(PostsModel).order_by(asc(PostsModel.likesCount))
        elif method == 2:
            query = Select(PostsModel).order_by(asc(PostsModel.createdAt))
        else:
            return None

        if limit is not None:
            query = query.limit(limit).offset(start)
        return db.session.scalars(query).all()

    # 评论数据处理
    def set_comment_count(self, post_id:VARCHAR, user_id:VARCHAR):
        post_model = self.get_post_by_id(post_id)
        post_model.commentsCount += 1
        post_model.lastCommentedAt = func.now()
        post_model.lastCommentedUserId = user_id
        db.session.commit()
        return True

    # 点赞数据处理
    def like_data_update(self, is_liked:bool, user_id:VARCHAR, post_id:VARCHAR):
        if is_liked:
            # 删除点赞记录
            stmt = post_like.delete().where(
                (post_like.c.userId == user_id) &
                (post_like.c.postId == post_id)
            )
        else:
            # 添加点赞记录
            stmt = post_like.insert().values(
                userId=user_id,
                postId=post_id
            )
        db.session.execute(stmt)
        db.session.commit()

    # 检查是否已点赞
    def like_status_check(self, post_id:VARCHAR, user_id:VARCHAR):
        query = db.session.query(
            exists().where(
                (post_like.c.userId == user_id) &
                (post_like.c.postId == post_id)
            )
        ).scalar()
        self.like_data_update(query, user_id, post_id)
        return query

    # 点赞帖子
    def set_likes_to_post(self, postId:VARCHAR, is_liked:bool):
        post_model = self.get_post_by_id(postId)
        if post_model:
            # 点赞
            if is_liked:
                post_model.likesCount -= 1
            # 取消点赞
            else:
                post_model.likesCount += 1
            db.session.commit()
            return post_model.likesCount
        else:
            return None

    # 处理收藏帖子数据
    def favorite_data_update(self, is_favorite:bool, user_id:VARCHAR, post_id:VARCHAR):
        if is_favorite:
            # 删除收藏记录
            stmt = favorites.delete().where(
                (favorites.c.userId == user_id) &
                (favorites.c.postId == post_id)
            )
        else:
            # 添加收藏记录
            stmt = favorites.insert().values(
                userId=user_id,
                postId=post_id
            )
        db.session.execute(stmt)
        db.session.commit()

    # 检查是否已收藏
    def favorite_post_check(self, postId:VARCHAR, userId:VARCHAR,):
        query = db.session.query(
            exists().where(
                (favorites.c.userId == userId) &
                (favorites.c.postId == postId)
            )
        ).scalar()
        self.favorite_data_update(query, userId, postId)
        return query

    # 获取用户收藏的帖子
    def get_favorite_posts(self, user_id:VARCHAR):
        query = (
            db.session.query(PostsModel)
            .join(favorites, PostsModel.postId == favorites.c.postId)
            .filter(favorites.c.userId == user_id)
        )
        return query.all()

    # 获取用户发布的帖子
    def get_released_posts(self, user_id:VARCHAR):
        return PostsModel.query.filter_by(authorId=user_id).all()

    # 获取热门帖子列表
    def get_popular_posts(self):
        # 获取当前时间的Unix时间戳
        current_time = func.unix_timestamp(func.now())

        # 计算时间衰减系数（小时）
        time_decay = db.cast(
            db.func.coalesce(
                current_time - func.unix_timestamp(PostsModel.lastCommentedAt),
                current_time - func.unix_timestamp(PostsModel.createdAt)
            ) / 3600,   # 转换为小时
            db.Float
        )

        # 综合评分公式
        popular_score = (
            (PostsModel.commentsCount * 0.4) +
            (PostsModel.likesCount * 0.35) +
            (db.func.exp(-time_decay / 24 * db.literal(0.6931)) * 0.25) # ln(2) ≈ 0.6931
        ).label('popular_score')

        # 构建Core查询语句，获取前3个热门帖子
        stmt = (select(PostsModel, popular_score).order_by(desc(popular_score)).limit(3))

        # 执行查询
        result = db.session.execute(stmt)
        popular_posts = result.all()

        return popular_posts


from sqlalchemy import VARCHAR, Select, asc, func, exists

from models.comments_model import CommentsModel
from models.comment_like_model import comment_like
from resources import db

# 启用数据库服务的类，定义了对数据库的增、删、改、查等各类操作，并将结果返回
class CommentsService:
    # 根据创建时间搜索
    pass

    # 通过Id查询评论
    def get_comment_by_id(self, commentId:VARCHAR):
        return db.session.get(CommentsModel, commentId)

    # 获取数据库中评论总数
    def get_total_comments(self):
        return db.session.query(func.count(CommentsModel.commentId)).scalar()

    # 产生评论编号
    def generate_commentId(self):
        num = self.get_total_comments()
        commentId = 'comment_' + str(num + 1)
        return commentId

    # 保存评论到数据库中
    def save_comment(self, new_comment:CommentsModel):
        db.session.add(new_comment)
        db.session.commit()
        return new_comment

    # 处理评论点赞数据
    def like_data_update(self, is_liked:bool, user_id:VARCHAR, post_id:VARCHAR):
        if is_liked:
            # 删除点赞记录
            delete_stmt = comment_like.delete.where(
                (comment_like.c.userId == user_id) &
                (comment_like.c.postId == post_id)
            )
            db.session.execute(delete_stmt)
            db.session.commit()
        else:
            # 添加点赞记录
            insert_stmt = comment_like.insert().values(
                userId=user_id,
                postId=post_id
            )
            db.session.execute(insert_stmt)
            db.session.commit()

    # 检查评论是否被当前用户点赞
    def like_status_check(self, comment_id:VARCHAR, user_id:VARCHAR):
        query = db.session.query(
            exists().where(
                (comment_like.c.userId == user_id) &
                (comment_like.c.commentId == comment_id)
            )
        ).scalar()
        return query

    # 点赞评论
    def set_likes_to_comment(self, commentId:VARCHAR, is_liked:bool):
        comment_model = self.get_comment_by_id(commentId)
        if comment_model:
            if is_liked:
                comment_model.likesCount += 1
            else:
                comment_model.likesCount -= 1
            for key, value in comment_model.items():
                if value is not None:
                    setattr(comment_model, key, value)
            db.session.commit()
            return comment_model.likesCount
        else:
            return None
import jwt
from flask import request
from flask_restful import Resource
from sqlalchemy import VARCHAR

from resources import api
from datetime import datetime

from models import CommentsModel
from models.favorites_model import favorites
from models.posts_model import PostsModel
from services.comments_service import CommentsService
from services.posts_service import PostsService
from commons.token_check import token_required
from commons.token_decode import token_decode


class PostResource(Resource):
    # 获取具体帖子内容
    def get(self, postId: VARCHAR):
        userId = request.args.get("user_id", None)
        post = PostsService().get_post_by_id(postId=postId)
        if post is not None:
            res = {
                'status': 'success',
                'data': {
                    'posts': post.serialize_mode2()
                }
            }
            if userId == '':
                return res
            else:
                res['data']['posts']['isFavorite'] = PostsService().favorite_post_check(postId, userId)
                res['data']['posts']['isLiked'] = PostsService().like_status_check(postId, userId)
                for comment in res['data']['posts']['comments']:
                    comment['isLiked'] = CommentsService().like_status_check(comment['commentId'], userId)
                return res
        else:
            return {
                'status': 'fail',
                'data': None
            },404

class PostListResource(Resource):
    # 获取帖子列表
    def get(self):
        limit =request.args.get('limit', type=int)
        start = request.args.get('start', type=int)
        method = request.args.get('method', type=int)
        post_list = PostsService().get_limited_posts(limit=limit,start=start,method=method)
        if post_list is None:
            return {'error': 'Invalid method value'}, 412
        else:
            return {
                'status': 'success',
                'data': {
                    'posts': [post_model.serialize_mode1() for post_model in post_list]
                }
            }

class PopularPostListResource(Resource):
    # 获取热门帖子列表
    def get(self):
        post_list = PostsService().get_popular_posts()
        if post_list is None:
            return {'status': 'fail', 'msg': '获取热门帖子列表失败'}, 404
        else:
            # 从元组中提取 PostsModel 实例
            popular_posts = [post_model for post_model, _ in post_list]
            return {
                'status': 'success',
                'msg': '热门帖子列表获取成功',
                'data': {
                    'posts':[post.serialize_mode1() for post in popular_posts]
                }
            }

class NewPostResource(Resource):
    # 发布新帖子
    @token_required
    def post(self):
        data = request.json.get('data', None)
        title = data.get('title', None)
        tags = data.get('tags', None)
        if not tags:
            tags.append({"tagId": 0, "tagName": "通用"})
        content = data.get('content', None)

        postId = PostsService().generate_postId()
        user_info = token_decode()
        authorId = user_info.get('id', None)
        createdAt = datetime.now()

        new_post = PostsModel(postId=postId,
                              title=title,
                              authorId=authorId,
                              createdAt=createdAt,
                              lastCommentedAt=None,
                              lastCommentedUserId=None,
                              commentsCount=0,
                              likesCount=0,
                              content=content)

        new_post = PostsService().save_post(new_post, tags)

        if new_post:
            return {
                "status": "success",
                "msg": "发布成功",
                "postMeta": {
                    "postId": new_post.postId,
                    "title": new_post.title
                }
            }
        else:
            return {
                "status": "fail",
                "msg": "发布失败",
                "postMeta": None
            },404

class NewCommentResource(Resource):
    # 发布新评论
    @token_required
    def post(self, postId:VARCHAR):
        repliedId = request.json.get('repliedId', None)
        content = request.json.get('comment', None)
        commentId = CommentsService().generate_commentId()
        user_info = token_decode()
        authorId = user_info.get('id', None)
        createAt = datetime.now()

        new_comment = CommentsModel(commentId=commentId,
                                    content=content,
                                    authorId=authorId,
                                    createdAt=createAt,
                                    likesCount=0,
                                    repliedID=repliedId,
                                    postId=postId)

        new_comment = CommentsService().save_comment(new_comment)
        is_set = PostsService().set_comment_count(postId, authorId)
        if new_comment and is_set:
            return {
                "status": "success",
                "msg": "评论已发布",
                "data": new_comment.serialize()
            }
        else:
            return {"status": "fail", "msg": "发布失败", "data": None},404

class LikesResource(Resource):
    # 点赞/取消点赞
    @token_required
    def put(self, postId:VARCHAR):
        type = request.json.get('type', None)
        commentId = request.json.get('commentId', None)
        user_info = token_decode()
        user_id = user_info.get('id', None)
        if type == 'post':
            is_liked = PostsService().like_status_check(postId, user_id)
            PostsService().like_data_update(is_liked, user_id, postId)
            likesCount = PostsService().set_likes_to_post(postId, is_liked)
        elif type == 'comment':
            is_liked = CommentsService().like_status_check(user_id, postId)
            CommentsService().like_data_update(is_liked, user_id, postId)
            likesCount = CommentsService().set_likes_to_comment(commentId, is_liked)
        else:
            return { "status": "fail", "msg": "type的类型错误" },412

        res = {"status": "success"}
        if is_liked:
            res["msg"] = "取消点赞成功"
        else:
            res["msg"] = "点赞成功"
        res["type"] = type
        if commentId:
            res["commentId"] = commentId
        res["count"] = likesCount

        if likesCount is not None:
            return res
        else:
            return {
                "status": "fail",
                "msg": "点赞失败"
            },404


class FavoritePostResource(Resource):
    # 收藏帖子/取消收藏帖子
    @token_required
    def put(self, postId:VARCHAR):
        user_info = token_decode()
        userId = user_info.get('id', None)
        is_favorite = PostsService().favorite_post_check(postId, userId)
        PostsService().favorite_data_update(is_favorite, userId, postId)
        post_id = postId
        res = {"status": "success"}
        if post_id:
            if is_favorite:
                res["msg"] = "取消收藏成功"
            else:
                res["msg"] = "收藏成功"
            res["postId"] = post_id
            return res
        else:
            return {
                "status": "fail",
                "msg": "收藏失败"
            },404

class UserFavoritePostListResource(Resource):
    # 获取用户收藏的帖子
    @token_required
    def get(self):
        user_id = request.args.get('user_id', type=str)
        favorite_post_list = PostsService().get_favorite_posts(user_id)
        res = {
            "status": "success",
            "msg": "用户收藏帖子列表获取成功",
            "favorites": []
        }
        for post in favorite_post_list:
            res["favorites"].append(post.serialize_mode1())

        return res

class UserReleasedPostListResource(Resource):
    # 获取用户发表过的帖子
    @token_required
    def get(self, user_id:VARCHAR):
        released_post_list = PostsService().get_released_posts(user_id)
        if released_post_list:
            res = {
                "status": "success",
                "msg": "获取用户发表过的帖子列表成功",
                "data": {"posts": []}
            }
            for post in released_post_list:
                res["data"]["posts"].append(post.serialize_mode1())
            return res
        else:
            return {
                "status": "fail",
                "msg": "获取用户发表过的帖子列表失败"
            },404


api.add_resource(PostResource,'/forum/posts/<string:postId>')
# api.add_resource(PostResource,'/admin/post/<string:postId>') 管理系统的接口，等队友把json需求发过来再写
api.add_resource(PostListResource,'/forum/posts')
api.add_resource(PopularPostListResource, '/popular/posts')
api.add_resource(NewPostResource, '/newpost')
api.add_resource(NewCommentResource, '/forum/<string:postId>/comment')
api.add_resource(LikesResource, '/forum/<string:postId>/likes')
api.add_resource(FavoritePostResource, '/forum/<string:postId>/favorite')
api.add_resource(UserFavoritePostListResource, '/forum/favorite')
api.add_resource(UserReleasedPostListResource, '/user/profile/<string:user_id>/posts')
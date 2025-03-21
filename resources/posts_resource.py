import jwt
from flask import request
from flask_restful import Resource
from sqlalchemy import VARCHAR
from resources import api

from models.posts_model import PostsModel
from services.posts_service import PostsService
from commons.token_check import token_required

class PostResource(Resource):
    # 获取具体帖子内容
    def get(self, postId: VARCHAR):
        post = PostsService().get_post_by_id(postId=postId)
        if post is not None:
            return {
                'status': 'success',
                'data': {
                    'posts': post.serialize_mode2()
                }
            }
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


api.add_resource(PostResource,'/forum/posts/<string:postId>')
# api.add_resource(PostResource,'/admin/post/<string:postId>') 管理系统的接口，等队友把json需求发过来再写
api.add_resource(PostListResource,'/forum/posts')
# api.add_resource('/forum/tags/list')
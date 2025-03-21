from flask import request
from flask_restful import Resource

from models.categories_model import CategoriesModel
from resources import api
from services.categories_service import CategoriesService

class TagsResource(Resource):
    # 处理获取所有标签要求
    def get(self):
        tag_list = CategoriesService().get_all_tags()
        if tag_list is None:
            return {'message': 'The tags list is empty'}
        else:
            return {
                'data': [tag_model.serialize_mode2() for tag_model in tag_list]
            }

api.add_resource(TagsResource, '/forum/tags/list')
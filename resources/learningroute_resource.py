from flask_restful import Resource
from resources import api

class LearningRouteResource(Resource):
    def get(self):
        return {"categories": ["前端","后端"]},200

api.add_resource(LearningRouteResource, '/roadmap/category')
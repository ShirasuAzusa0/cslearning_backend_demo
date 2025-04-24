from flask import request
from flask_restful import Resource

from commons.token_check import token_required
from resources import api

from commons.learning_path_planning_model import generate_learning_path

from services.neo4j_service import Neo4jService


class Neo4jAllResource(Resource):
    # 获取所有的节点
    def get(self):
        node_list = Neo4jService().get_all_nodes()
        return node_list

class Neo4jCategoryResource(Resource):
    # 根据类型和id获取对应的节点及关系
    def get(self):
        type = request.args.get('type', None)
        id = request.args.get('id', None)
        node_list = Neo4jService().get_rel_by_type(id, type)
        if node_list is None:
            return {'error':f'wrong type {type}'},400
        return node_list

class Neo4jSameClassResource(Resource):
    # 获取指定节点的同级关系
    def get(self):
        id = request.args.get('id', None)
        node_list = Neo4jService().get_same_class_rel(id)
        if node_list is None:
            return {'status': 'fail', 'msg': '获取同级节点关系失败'},404
        return node_list

class Neo4jLearningPathResource(Resource):
    # 根据做题结果生成学习路径
    def post(self):
        result = request.json
        res_list = generate_learning_path(result, use_enhanced=True)
        node_list = Neo4jService().get_learning_path(res_list[0]['topic'])
        if node_list is None:
            return {'status': 'fail', 'msg': 'fail to generate the learning path'}, 400
        return node_list

api.add_resource(Neo4jAllResource, '/learn/KnowledgeGraph')
api.add_resource(Neo4jCategoryResource, '/learn/KnowledgeGraph/category')
api.add_resource(Neo4jSameClassResource, '/learn/KnowledgeGraph/relationship')
api.add_resource(Neo4jLearningPathResource, '/quiz/LearningPath')
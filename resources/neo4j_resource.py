from flask import request
from flask_restful import Resource

from commons.token_check import token_required
from resources import api

from services.neo4j_service import Neo4jService


class Neo4jAllResource(Resource):
    # 获取所有的节点
    @token_required
    def get(self):
        node_list = Neo4jService().get_all_nodes()
        return node_list

class Neo4jCategoryResource(Resource):
    # 根据类型和id获取对应的节点及关系
    @token_required
    def get(self):
        type = request.args.get('type', None)
        id = request.args.get('id', None)
        node_list = Neo4jService().get_rel_by_type(id, type)
        if node_list is None:
            return {'error':f'wrong type {type}'},400
        return node_list



api.add_resource(Neo4jAllResource, '/forum/KnowledgeGraph')
api.add_resource(Neo4jCategoryResource, '/forum/KnowledgeGraph/category')
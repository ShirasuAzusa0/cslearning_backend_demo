from flask import jsonify
from resources import app

class Neo4jService:
    # 获取所有节点json数据方法
    def get_all_nodes(self):
        graph = app.config['NEO4J_GRAPH']
        # query = graph.nodes.match()
        query = """
            MATCH (all:All {id:'技术改变生活'})-[:总路线*]->(category)
            WITH all, category
            OPTIONAL MATCH (category)-[:板块分类*]->(part)
            OPTIONAL MATCH (part)-[:板块细分*]->(depart)
            OPTIONAL MATCH (depart)-[:知识分类*]->(course2)
            OPTIONAL MATCH (part)-[:知识分类*]->(course1)
            RETURN all, category, part, depart, course1, course2
        """
        result = graph.run(query)
        node_list = []
        for record in result:
            node_data = {}
            for key in record.keys():
                node_data[key] = dict(record[key]) if record[key] else None
            node_list.append(node_data)
        return node_list

    # 根据id查找出category类节点的所有层子节点
    def get_rel_by_type(self, id:str, type:str):
        graph = app.config['NEO4J_GRAPH']
        nodes = self.get_all_subnodes(id, type, graph)
        if nodes is None:
            return None
        return jsonify(nodes)

    # 通过id获取该id对应的节点下的多层关系的多层子节点
    def get_all_subnodes(self, id, type, graph):
        queries = {
            'Category':"""
                MATCH (category:Category {id: $id})-[:板块分类*]->(part)
                WITH category, part
                OPTIONAL MATCH (part)-[:板块细分*]->(depart)
                OPTIONAL MATCH (depart)-[:知识分类*]->(course2)
                OPTIONAL MATCH (part)-[:知识分类*]->(course1)
                RETURN category, part, depart, course1, course2
            """,
            'Part':"""
                MATCH (part:Part {id: $id})
                OPTIONAL MATCH (part)-[:板块细分*]->(depart)
                WITH part, depart
                OPTIONAL MATCH (depart)-[:知识分类*]->(course2)
                OPTIONAL MATCH (part)-[:知识分类*]->(course1)
                RETURN part, depart, course1, course2
            """,
            'de_Part':"""
                MATCH (depart:de_Part {id: $id})-[:知识分类*]->(course)
                RETURN depart, course
            """,
            'Course':"""
                MATCH (course:Course {id: $id})
                RETURN course
            """
        }
        if type not in queries:
            return None

        query = queries[type]
        result = graph.run(query, id=id)
        node_list = []

        for record in result:
            node_data = {}
            for key in record.keys():
                node_data[key] = dict(record[key]) if record[key] else None
            node_list.append(node_data)
        return node_list


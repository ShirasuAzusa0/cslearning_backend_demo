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

    # 根据id获取与当前id对应的节点和与之关联的同级节点
    def get_same_class_rel(self, id:str):
        graph = app.config['NEO4J_GRAPH']
        query = """
            MATCH p=(c1:Course {id:$id})-[*]->(c2)
            WITH p, 
                 nodes(p) AS pathNodes, 
                 relationships(p) AS rels
            UNWIND range(0, size(rels)-1) AS index
            WITH 
                pathNodes[index].id AS startId,
                pathNodes[index+1].id AS endId,
                type(rels[index]) AS relationType,
                index+1 AS stepOrder
            // 按起始节点、结束节点和关系类型分组，取最小步序
            WITH startId, endId, relationType, min(stepOrder) AS stepOrder
            // 再次按起始节点和结束节点分组，确保关系类型唯一
            WITH startId, endId, collect([relationType, stepOrder]) AS typeSteps
            UNWIND typeSteps AS ts
            WITH startId, endId, ts[0] AS relationType, ts[1] AS stepOrder
            // 最终返回唯一的关系组合
            RETURN startId, endId, relationType, stepOrder
            ORDER BY stepOrder
        """
        node_list = graph.run(query, id=id).data()
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

    # 给出学习路径
    def get_learning_path(self, start):
        graph = app.config['NEO4J_GRAPH']
        # 路径步长索引法，通过nodes(p)和relationships(p)获取路径中的节点与关系列表，结合索引标记步长顺序
        query = '''
            MATCH p=(c1:Course {id:$id})-[:学习路线*]->(c2)
            WITH p, 
                 nodes(p) AS pathNodes, 
                 relationships(p) AS rels
            UNWIND range(0, size(rels)-1) AS index
            RETURN 
              pathNodes[index].id AS startId,
              pathNodes[index+1].id AS endId,
              type(rels[index]) AS relationType,
              index+1 AS stepOrder
            ORDER BY stepOrder
        '''
        node_list = graph.run(query, id=start).data()
        return node_list
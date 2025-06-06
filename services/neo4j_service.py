import json
import shutil
from pathlib import Path
from flask import jsonify
from resources import app
from flask_restful import reqparse
from werkzeug.datastructures import FileStorage

from commons.utils import get_learning_road_path

class Neo4jService:
    def __init__(self):
        # 定义一个语法分析器parser，其值为RequestParser函数，用于处理请求参数的输入
        self.parser = reqparse.RequestParser()
        # 通过add_argument方法定义需要解析的参数
        self.parser.add_argument("avatar",  # 参数名称
                                 type=FileStorage,  # 文件存储类型
                                 location="files",  # 提取参数的位置，将数据转换成文件存储
                                 help="Please private avatar file")  # 请求中没有参数则报错的内容

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
                pathNodes[index].content AS startContent,
                pathNodes[index].level AS startLevel,
                pathNodes[index+1].id AS endId,
                pathNodes[index+1].content AS endContent,
                pathNodes[index+1].level AS endLevel,
                type(rels[index]) AS relationType,
                index+1 AS stepOrder
            // 按起始节点、结束节点和关系类型分组，取最小步序及属性
            WITH startId, endId, relationType, 
                 min(stepOrder) AS stepOrder,
                 min(startContent) AS startContent,  // 同一id属性值相同，min/max均可
                 min(startLevel) AS startLevel,
                 min(endContent) AS endContent,
                 min(endLevel) AS endLevel
            // 按起始结束节点分组，收集关系类型与步序组合
            WITH startId, endId, 
                 collect([relationType, stepOrder]) AS typeSteps,
                 startContent, startLevel, endContent, endLevel
            UNWIND typeSteps AS ts
            // 展开获取最终关系类型和步序
            WITH startId, endId, 
                 ts[0] AS relationType, 
                 ts[1] AS stepOrder,
                 startContent, startLevel, endContent, endLevel
            // 返回结果并排序
            RETURN startId, endId, relationType, stepOrder,
                   startContent, startLevel, endContent, endLevel
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
              pathNodes[index].level AS startLV,
              pathNodes[index].content AS startContent,
              pathNodes[index+1].id AS endId,
              pathNodes[index+1].level AS endLV,
              pathNodes[index+1].content AS endContent,
              type(rels[index]) AS relationType,
              index+1 AS stepOrder
            ORDER BY stepOrder
        '''
        node_list = graph.run(query, id=start).data()
        return node_list

    # 保存学习路径json数据
    def save_json_data(self, filename:str, data):
        with open(filename, 'w', encoding='utf-8') as json_file:
            #json_file.write(data)
            json.dump(data, json_file, ensure_ascii=False)
            json_file.close()
        save_path = get_learning_road_path()
        save_path = save_path / filename
        #attachment_file = self.parser.parse_args().get("LearningPath")
        #attachment_file.save(save_path)
        # 1. 确定当前脚本所在目录
        current_dir = Path(__file__).resolve().parent

        # 2. 构造目标目录：上一级/attachments/LearningPath
        target_dir = current_dir.parent / "attachments" / "LearningPath"

        # 3. 如果目标目录不存在，就创建（包括所有父级）
        target_dir.mkdir(parents=True, exist_ok=True)

        # 4. 目标文件全路径
        target_file = target_dir / filename

        # 5. 将刚才写入的本地文件移动到目标目录
        shutil.move(str(filename), str(target_file))
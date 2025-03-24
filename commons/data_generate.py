import pandas as pd
from py2neo import Graph, Relationship
from contants import username, password

# 连接Neo4j数据库
username = username
password = password
graph = Graph("bolt://120.76.138.103:7687", auth=(username, password), name='neo4j')

def create_node_1(label, id):
    query = f"MERGE (n:{label} {{id: $id}}) RETURN n"
    graph.run(query, id=id)

def create_node_2(label, id, level):
    query = f"MERGE (n:{label} {{id: $id}}) SET n.level = $level RETURN n"
    graph.run(query, id=id, level=level)

def create_node_3(label, id, level, content):
    query = f"MERGE (n:{label} {{id: $id}}) SET n.level = $level, n.content = $content RETURN n"
    graph.run(query, id=id, level=level, content=content)

def create_relationship(start_node_label, start_node_id, end_node_label, end_node_id, relationship_type, attributes):
    start_node = graph.nodes.match(start_node_label, id=start_node_id).first()
    end_node = graph.nodes.match(end_node_label, id=end_node_id).first()

    if not start_node or not end_node:
        return

    rel = Relationship(start_node, relationship_type, end_node, **attributes)
    graph.create(rel)

def excute_excel(file_path):
    df = pd.read_excel(file_path)

    create_node_1('All', '技术改变生活')

    # 构建一级分类节点
    create_node_1('Category', '前端')
    create_node_1('Category', '后端')

    for index, row in df.iterrows():
        start_node_label = 'Category'
        middle_node_label = 'Part'
        end_node_label = 'Course'
        start_node_id = row['rel1']
        middle_node_id = row['rel2']
        end_node_id = row['name']
        end_node_level = row['level']
        end_node_content = row['info']

        # 构建底级分类节点
        if not pd.isna(end_node_label) and not pd.isna(end_node_id) and not pd.isna(end_node_level) and not pd.isna(end_node_content):
            create_node_3(end_node_label, end_node_id, end_node_level, end_node_content)

        # 构建二级分类节点，以及构建二级分类下分节点（如果有）
        if end_node_id == middle_node_id:
            create_node_2(middle_node_label, middle_node_id, end_node_level)
        else:
            if pd.isna(end_node_content):
                create_node_2('de_Part', end_node_id, end_node_level)

    relationship_type0 = '总路线'
    relationship_type1 = '板块分类'
    relationship_type2 = '板块细分'
    relationship_type3 = '知识分类'

    attributes_01 = {
        'outline': '技术改变生活',
        'category': '前端'
    }

    attributes_02 = {
        'outline': '技术改变生活',
        'category': '后端'
    }

    attributes_21 = {
        'outline': '构建工程化',
        'category': '打包工具'
    }

    attributes_22 = {
        'outline': '构建工程化',
        'category': '包管理'
    }

    attributes_23 = {
        'outline': '构建工程化',
        'category': '代码规范'
    }

    # 构建顶层关系
    create_relationship('All', '技术改变生活', 'Category', '前端', relationship_type0, attributes_01)
    create_relationship('All', '技术改变生活', 'Category', '后端', relationship_type0, attributes_02)

    # 构建一级关系下分关系
    create_relationship('Part', '构建工程化', 'de_Part', '打包工具', relationship_type2, attributes_21)
    create_relationship('Part', '构建工程化', 'de_Part', '包管理', relationship_type2, attributes_22)
    create_relationship('Part', '构建工程化', 'de_Part', '代码规范', relationship_type2, attributes_23)

    for index, row in df.iterrows():
        relationship_type4 = row['rel3']

        attributes_1 = {
            'outline': '构建工程化',
            'category': row['rel2']
        }

        attributes_3 = {
            'outline': row['rel2'],
            'category': row['name']
        }

        attributes_4 = {
            'outline': row['rel2'],
            'category': row['name']
        }

        attributes_5 = {
            'outline': row['next'],
            'category': row['name']
        }

        # 构建一级关系
        create_relationship('Category', row['rel1'], 'Part', row['rel2'], relationship_type1, attributes_1)

        # 构建二级关系
        create_relationship('Part', row['rel2'], 'Course', row['name'], relationship_type3, attributes_3)

        # 构建下分关系下的二级关系
        create_relationship('de_Part', row['rel2'], 'Course', row['name'], relationship_type3, attributes_4)

        # 构建二级级内关系
        if not pd.isna(row['next']) and not pd.isna(row['rel3']):
            create_relationship('Course', row['next'], 'Course', row['name'], relationship_type4, attributes_5)

excute_excel('../attachments/DataSets.xlsx')
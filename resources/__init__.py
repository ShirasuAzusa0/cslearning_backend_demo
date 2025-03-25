# 初始化配置文件
from flask import Flask
from flask_restful import Api
from flask_sqlalchemy import SQLAlchemy
from py2neo import Graph

from commons.contants import MySQL_Config, Neo4j_Config

# 创建一个Flask应用实例app，在name为main时运行启动后端服务器
app = Flask(__name__)
# 创建一个Flask-RESTful API实例与Flask应用关联，设置了前端调用的后端接口的入口
#api = Api(app)
api = Api(app)
# 配置SQLAlchemy数据库的链接信息，指定了链接的数据库
app.config['SQLALCHEMY_DATABASE_URI'] = MySQL_Config
# 创建一个SQLAlchemy实例并与Flask应用关联，以便使用映射方式获取数据库中的数据
db = SQLAlchemy(app)
# 配置Neo4j数据库的链接信息
graph = Graph(Neo4j_Config[0], auth=Neo4j_Config[1], name=Neo4j_Config[2])
# 将Neo4j驱动实例添加到Flask应用的配置中，以便在其他地方使用
app.config['NEO4J_GRAPH'] = graph

from resources import (learningroute_resource,
attachment_resource,
posts_resource,
conversations_resource,
users_resource,
neo4j_resource,
categories_resource)
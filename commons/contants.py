# 密钥（由管理者自定义）
LOGIN_SECRET = "f3raesfdkncf34209r3tj"

# MySQL配置
# MySQL_Config = 'mysql+mysqldb://root:Azusa1226!@127.0.0.1/postslist'
MySQL_Config = 'mysql+pymysql://webdata:THDJeyNMW7PNAj3F@120.76.138.103:3306/webdata'


# Neo4j配置
'''
uri = "http://localhost:7474"
username = "neo4j"
password ="AzusaNeo4j"
auth = (username, password)
name = "azusa"
'''
uri = "bolt://120.76.138.103:7687"
username = "neo4j"
password ="AzusaNeo4j"
auth = (username, password)
name = "neo4j"

Neo4j_Config = [uri, auth, name]
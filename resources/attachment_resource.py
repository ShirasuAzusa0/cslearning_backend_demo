from flask import request
from flask_restful import Resource
from flask import send_file
from commons import utils, crawler
from resources import api

# 继承自flask-restful的Resource的类，定义了处理前端HTTP请求的方法（如GET、POST、PUT、DELETE）
class Attachment_resource(Resource):
    # 获取知识图谱的json格式数据
    def get(self, filename):
        file_path = utils.get_attachment_path().joinpath(filename)
        if not file_path or not file_path.is_file():
            return {"error": f"{filename} no found"},404
        return send_file(file_path)

class ArticleResource(Resource):
    # 获取指定知识点的相关文章和视频
    def get(self):
        type = request.args.get('type', None)
        id = request.args.get('id', None)
        articles = utils.get_article_path(type, id)
        video = crawler.WebDriverCrawler().video_crawler(id)
        return {
            "status": "success",
            "msg": "相关资源查找完成",
            "resources": {
                "article": list(articles),
                "video": list(video)
            }
        }

class PublicKeyResource(Resource):
    # 获取公钥
    def get(self):
        public_key_path = utils.get_key_path().joinpath("public_key.pem")
        with open(public_key_path, "r") as f:
            public_key = f.read()
            f.close()
        return {
            "msg": "success",
            "key": public_key,
        }

api.add_resource(Attachment_resource, '/roadmap-attachments/<filename>')
api.add_resource(ArticleResource, '/learn/KnowledgeGraph/Resources')
api.add_resource(PublicKeyResource, '/login/publicKey')
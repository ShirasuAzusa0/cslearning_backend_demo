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
    # 获取指定知识点的相关文章
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

api.add_resource(Attachment_resource, '/roadmap-attachments/<filename>')
api.add_resource(ArticleResource, '/forum/KnowledgeGraph/Resources')
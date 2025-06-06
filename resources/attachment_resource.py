from flask import request
from flask_restful import Resource
from flask import send_file
from commons import utils, crawler
from resources import api
import random

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

class PopularArticleResource(Resource):
    # 获取热门文章
    def get(self):
        # 前端待选文章列表
        front_end = [
            {
                "name": "学习HTML",
                "article": "node_article\\结点文章\\前端\\前端基础核心入门\\HTML5\\0.HTML介绍文档"
            },
            {
                "name": "学习CSS",
                "article": "node_article\\结点文章\\前端\\前端基础核心入门\\CSS3\\0.CSS3介绍文档"
            },
            {
                "name": "学习Java Script",
                "article": "node_article\\结点文章\\前端\\前端基础核心入门\\Java Script ES6\\0.JavaScript (ES6+) 介绍文档"
            },
            {
                "name": "学习TypeScript",
                "article": "node_article\\结点文章\\前端\\前端基础核心入门\\TypeScript\\0.TypeScript介绍文档"
            }
        ]
        # 后端待选文章列表
        back_end = [
            {
                "name": "学习Go语言",
                "article": "node_article\\结点文章\\后端\\后端基础语言入门\\Go\\0.Go 介绍文档"
            },
            {
                "name": "学习Java",
                "article": "node_article\\结点文章\\后端\\后端基础语言入门\\Java\\0.Java 介绍文档"
            },
            {
                "name": "学习Node.js",
                "article": "node_article\\结点文章\\后端\\后端基础语言入门\\Node.js\\0.Node.js 介绍文档"
            },
            {
                "name": "学习Python",
                "article": "node_article\\结点文章\\后端\\后端基础语言入门\\Python\\0.Python 介绍文档"
            },
            {
                "name": "学习Rust",
                "article": "node_article\\结点文章\\后端\\后端基础语言入门\\Rust\\0.Rust 介绍文档"
            }
        ]

        # 从上面的 front_end 和 back_end 中各随机抽取两个元素
        selected_front = random.sample(front_end, 2)
        selected_back = random.sample(back_end, 2)
        popular_articles = selected_front + selected_back
        return {
            "status": "success",
            "msg": "热门文章获取完成",
            "articles": popular_articles
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
            "key": public_key
        }

api.add_resource(Attachment_resource, '/roadmap-attachments/<filename>')
api.add_resource(ArticleResource, '/learn/KnowledgeGraph/Resources')
api.add_resource(PopularArticleResource, '/popular/articles')
api.add_resource(PublicKeyResource, '/login/publicKey')
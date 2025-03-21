from flask_restful import Resource
from sqlalchemy import VARCHAR
from resources import api
from services.conversations_service import ConversationsService

class ConversationResource(Resource):
    # 获取指定聊天的内容
    def get(self, conversationId:VARCHAR):
        conservation = ConversationsService().get_conversation_messages_by_id(conversationId=conversationId)
        return conservation.serialize_mode2()

class ConversationListResource(Resource):
    # 查询用户在线聊天的历次聊天记录
    def get(self, id:VARCHAR, limit:int, offset:int):
        conversation_list = ConversationsService().get_conversations_by_user_id(id=id, limit=limit, offset=offset)
        if conversation_list is None:
            return {'error': 'Invalid method value'}, 412
        else:
            total = ConversationsService().get_total_conversations_by_user_id(id=id)
            return {
                'items': [conversation_model.serialize_mode1() for conversation_model in conversation_list],
                'offset': str(offset),
                'limit': str(limit),
                'total': str(total)
            }

api.add_resource(ConversationResource, '/conversation/conversations/<string:conversationId>')
api.add_resource(ConversationListResource, '/conversation/conversations/<string:id>$<int:limit>$<int:offset>')
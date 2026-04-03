from typing import List, Dict, Any, Optional
from pydantic import BaseModel, Field


class JavaServiceDataItem(BaseModel):
    # Java微服务传入的单个数据项
    kbId: int = Field(..., description="知识库ID")
    documentId: int = Field(..., description="文档ID")
    context: str = Field(..., description="需要向量化的文本内容")

    class Config:
        json_schema_extra = {
            "example": {
                "kbId": 1,
                "documentId": 1,
                "context": "这是需要向量化的文本内容..."
            }
        }


class JavaServiceRequest(BaseModel):
    # Java微服务请求格式
    status: str = Field(..., description="状态")
    msg: str = Field(..., description="消息")
    data: List[JavaServiceDataItem] = Field(..., description="数据列表")

    class Config:
        json_schema_extra = {
            "example": {
                "status": "success",
                "msg": "数据处理成功",
                "data": [
                    {
                        "kbId": 1,
                        "documentId": 1,
                        "context": "这是第一个文档内容"
                    },
                    {
                        "kbId": 2,
                        "documentId": 2,
                        "context": "这是第二个文档内容"
                    }
                ]
            }
        }


class VectorizationResponse(BaseModel):
    # 向量化接口响应格式
    status: str = Field(..., description="状态：success/error")
    msg: str = Field(..., description="响应消息")
    code: Optional[int] = Field(200, description="状态码")
    total_processed: Optional[int] = Field(0, description="处理总数")
    failed_items: Optional[List[Dict]] = Field([], description="失败项")
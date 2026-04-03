from flask import jsonify
from typing import Any, Optional, Dict

def api_response(data: Any = None,
                message: str = "Success",
                code: int = 200,
                status: str = "success") -> tuple:
    """
    统一成功响应格式
    """
    response = {
        "status": status,
        "code": code,
        "message": message,
        "data": data
    }
    return jsonify(response), code


def api_error(message: str = "Error",
              code: int = 400,
              status: str = "error",
              errors: Optional[Dict] = None) -> tuple:
    """
    统一错误响应格式
    """
    response = {
        "status": status,
        "code": code,
        "message": message,
        "errors": errors or {}
    }
    return jsonify(response), code
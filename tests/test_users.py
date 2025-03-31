'''
import unittest
from unittest.mock import patch, MagicMock
from commons.RSAkey_load import load_private_key
from resources.users_resource import LoginResource


class TestUsersLogin(unittest.TestCase):
    # 测试类下的每个测试方法都需要以test_开头，否则unittest会检测不到
    def test_init(self):
        self.resource = LoginResource()
        # 模拟私钥解密方法
        self.resource.private_key = MagicMock()
        self.resource.private_key.decrypt = MagicMock()

    def test_login(self):
        self.resource = LoginResource()
        pass
'''
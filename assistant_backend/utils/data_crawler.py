import time
from selenium.webdriver.edge.options import Options
from selenium.webdriver.edge.service import Service
from selenium import webdriver
from lxml import etree

import os
import uuid

class WebDriverCrawler:
    def __init__(self):
        #user_data_dir = f"/tmp/edge_{uuid.uuid4().hex}"
        #os.makedirs(user_data_dir, exist_ok=True)

        # 创建EdgeOptions对象
        options = Options()

        # 指定唯一目录
        #options.add_argument(f"--user-data-dir={user_data_dir}")

        # 设置headless模式（隐藏浏览器）
        options.add_argument('--headless')
        # 隐藏监听信息
        options.add_experimental_option('excludeSwitches', ['enable-logging'])

        options.add_argument('--no-sandbox')  # 禁用沙盒模式，规避权限问题
        options.add_argument('--disable-dev-shm-usage')  # 避免共享内存不足导致崩溃

        # 禁止图片加载
        prefs = {"profile.managed_default_content_settings.images": 2}
        options.add_experimental_option('prefs', prefs)
        # 创建一个Edge WebDriver实例
        self.driver = webdriver.Edge(options=options)

    def get_search_html(self, keyword:str) -> str:
        # 打开网页
        self.driver.get(f'https://search.bilibili.com/all?&tid=0&page=1&keyword={keyword}&order=click')
        # 刷新网页
        # driver.refresh()
        # 添加请求延迟
        time.sleep(0.5)
        # 获取网页的HTML
        html_str = self.driver.page_source
        # 关闭WebDriver
        self.driver.quit()
        # 返回html源码
        return html_str

    @staticmethod
    def parse_search_html(html_str:str) -> list:
        # 创建lxml解析对象
        parse_html = etree.HTML(html_str)
        # 提取目标元素（此处作用是获取b站页面搜索按播放量排列下的前5个视频的链接和标题）
        url_list = parse_html.xpath(
            '//*[@id="i_cecream"]/div/div[2]/div[2]/div/div/div[1]/div[position() >= 1 and position() <= 5]/div/div[2]/a/@href')
        title_list = parse_html.xpath(
            '//*[@id="i_cecream"]/div/div[2]/div[2]/div/div/div[1]/div[position() >= 1 and position() <= 5]/div/div[2]/div[2]/div/a/h3/@title')
        video_list = []
        for i in range(0, len(url_list)):
            video_list.append({
                "name": str(title_list[i]),
                "url": str("https:" + url_list[i])
            })
        return video_list

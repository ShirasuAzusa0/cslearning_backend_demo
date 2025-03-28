from pathlib import Path

def get_attachment_path():
    # 获取当前脚本文件（utils.py）的路径，并找到其父目录的父母录（在当前项目中是项目根目录，根据不同项目来调整）
    home_path = Path(__file__).parent.parent

    # 在项目根目录后拼接上attachments文件夹子目录的路径
    attachment_path = home_path.joinpath("attachments/avatar")

    # 父目录（attachments目录）不存在时自动创建
    if not attachment_path.exists():
        attachment_path.mkdir(parents=True)

    # 返回attachments目录的路径
    return attachment_path

def get_key_path():
    home_path = Path(__file__).parent.parent
    key_path = home_path.joinpath("attachments/key")
    if not key_path.exists():
        key_path.mkdir(parents=True)
    return key_path

# 获取节点文章的路径
def get_article_path(type, filename):
    home_path = Path(__file__).parent.parent
    article_path = home_path.joinpath("attachments/node_article")

    # 遍历node_article目录下的所有子目录（直接+间接）
    for subdir in article_path.rglob('*'):
        if subdir.is_dir() and subdir.name == filename:
            articles = []
            for file in subdir.rglob('*'):
                if file.is_file() and ((type == 'Course' and file.name[0] != '0') or type in ['Part','de_Part'] and file.name[0] == '0'):
                    articles.append(
                        {
                            "name": str(file.name),
                            "article": str(file)
                        }
                    )
            return articles
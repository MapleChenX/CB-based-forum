import pymysql
from elasticsearch import Elasticsearch, helpers
import json

# 连接 MySQL
mysql_conn = pymysql.connect(
    host="localhost",
    user="root",
    password="111111",
    database="forum",
    cursorclass=pymysql.cursors.DictCursor
)

# 连接 Elasticsearch
es = Elasticsearch(
    "http://localhost:9200",
    basic_auth=("elastic", "111111")  # 这里替换成你的用户名和密码
)

# 创建索引（可选）
index_name = "forum_posts"
if es.indices.exists(index=index_name):
    es.indices.delete(index=index_name)
if not es.indices.exists(index=index_name):
    es.indices.create(index=index_name, body={
        "mappings": {
            "properties": {
                "title": {"type": "text"},
                "content": {"type": "text"},
                "id": {"type": "integer"},
                "is_deleted": {"type": "boolean", "null_value": False}  # 添加删除标识
            }
        }
    })

# 从 MySQL 查询数据
def fetch_data():
    with mysql_conn.cursor() as cursor:
        cursor.execute("SELECT id, title, content FROM db_topic")
        return cursor.fetchall()

# 提取纯文本内容
def extract_text(content):
    # 如果 content 是一个 JSON 字符串，先将其转为字典
    if isinstance(content, str):
        try:
            content = json.loads(content)  # 转换为 JSON 对象
        except json.JSONDecodeError:
            # 如果转换失败，直接返回空字符串
            print("Failed to decode JSON content")
            return ""

    # 假设 content 是一个字典，并且有 "ops" 键
    text = ""
    if isinstance(content, dict) and "ops" in content:
        for op in content["ops"]:
            if isinstance(op["insert"], str):  # 如果是字符串，直接添加
                text += op["insert"]
            # 如果是图片，忽略
    return text.strip()

# 构造 Bulk 数据
def bulk_insert():
    actions = []
    for row in fetch_data():
        action = {
            "_index": index_name,
            "_id": row["id"],  # 使用 MySQL 的 id 作为 ES 的 _id，保证一致性
            "_source": {
                "title": row["title"],
                "content": extract_text(row["content"])
            }
        }
        actions.append(action)

    if actions:
        helpers.bulk(es, actions)
        print("数据导入完成")

# 执行导入
bulk_insert()

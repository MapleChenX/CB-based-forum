import pymysql
from elasticsearch import Elasticsearch, helpers

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
if not es.indices.exists(index=index_name):
    es.indices.create(index=index_name, body={
        "mappings": {
            "properties": {
                "title": {"type": "text"},
                "content": {"type": "text"},
                "id": {"type": "integer"}
            }
        }
    })

# 从 MySQL 查询数据
def fetch_data():
    with mysql_conn.cursor() as cursor:
        cursor.execute("SELECT id, title, content FROM db_topic")
        return cursor.fetchall()

# 构造 Bulk 数据
def bulk_insert():
    actions = []
    for row in fetch_data():
        action = {
            "_index": index_name,
            "_id": row["id"],  # 使用 MySQL 的 id 作为 ES 的 _id，保证一致性
            "_source": {
                "title": row["title"],
                "content": row["content"]
            }
        }
        actions.append(action)

    if actions:
        helpers.bulk(es, actions)
        print("数据导入完成")

# 执行导入
bulk_insert()

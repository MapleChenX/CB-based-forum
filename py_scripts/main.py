from fastapi import FastAPI
from pydantic import BaseModel
import torch
from transformers import AutoTokenizer, AutoModel

"""
功能：将文本转为向量
输入：文本
输出：768维的向量
"""

app = FastAPI()


class TextInput(BaseModel):
    title: str
    content: str

@app.get("/")
async def root():
    return {"message": "Hello World"}


@app.get("/hello/{name}")
async def say_hello(name: str):
    return {"message": f"Hello {name}"}


# 定义一个 API 路由
@app.post("/text2vector")
async def get_text_vector(input_data: TextInput):
    # 从请求中获取标题和内容
    title = input_data.title
    content = input_data.content

    # 计算向量
    vector = text_to_vector(title + " " + content)

    # 返回向量
    return {"vector": vector}

# 指定本地模型路径
model_path = "C:/Users/25056/Desktop/bert-base-chinese"

# 加载模型和分词器
tokenizer = AutoTokenizer.from_pretrained(model_path)
model = AutoModel.from_pretrained(model_path)


def text_to_vector(text):
    inputs = tokenizer(text, return_tensors="pt", padding=True, truncation=True, max_length=512)

    with torch.no_grad():
        outputs = model(**inputs)

    # 获取 [CLS] 位置的向量表示（768 维）
    vector = outputs.last_hidden_state[:, 0, :].squeeze().numpy()
    return vector.tolist()
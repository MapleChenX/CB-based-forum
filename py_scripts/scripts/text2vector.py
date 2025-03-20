import torch
from transformers import AutoModel, AutoTokenizer

"""
功能：将文本转为向量
输入：文本
输出：768维的向量
"""

# 指定本地模型路径
model_path = "C:/Users/25056/Desktop/bert-base-chinese"
# model_path = "../bert-base-chinese"

# 加载模型和分词器
tokenizer = AutoTokenizer.from_pretrained(model_path)
model = AutoModel.from_pretrained(model_path)

def text_to_vector(title, content):
    text = title + " " + content  # 合并 title 和 content
    inputs = tokenizer(text, return_tensors="pt", padding=True, truncation=True, max_length=512)

    with torch.no_grad():
        outputs = model(**inputs)

    # 获取 [CLS] 位置的向量表示（768 维）
    vector = outputs.last_hidden_state[:, 0, :].squeeze().numpy()
    return vector


# 测试
title = "Elasticsearch 向量搜索"
content = "如何使用 Elasticsearch 进行向量搜索？"
vector = text_to_vector(title, content)
print("向量维度:", vector.shape)
print("向量:", vector)

import torch
from transformers import AutoModel

"""
将.bin类型的模型转为.onxx类型
"""


# 指定本地模型路径
model_path = "C:/Users/25056/Desktop/bert-base-chinese"

# 加载模型
model = AutoModel.from_pretrained(model_path)
model.eval()

# 定义输入
dummy_input = torch.zeros(1, 512, dtype=torch.long)

# 导出为 ONNX 格式
onnx_model_path = "C:/Users/25056/Desktop/bert-base-chinese.onnx"
torch.onnx.export(
    model,
    dummy_input,
    onnx_model_path,
    input_names=["input_ids"],
    output_names=["output"],
    dynamic_axes={"input_ids": {0: "batch_size", 1: "sequence_length"}, "output": {0: "batch_size", 1: "sequence_length"}},
    opset_version=14
    # opset_version=11
)

print(f"ONNX 模型已保存到 {onnx_model_path}")
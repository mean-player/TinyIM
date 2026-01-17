import os

# 输出文件
output_file = "im.txt"

# 获取当前脚本所在目录
base_dir = os.path.dirname(os.path.abspath(__file__))

with open(output_file, "w", encoding="utf-8") as out_f:
    # 遍历目录及子目录
    for root, dirs, files in os.walk(base_dir):
        for file in files:
            if file.endswith(".java"):
                file_path = os.path.join(root, file)
                out_f.write(f"==== {file_path} ====\n")  # 写入文件名作为分隔
                try:
                    with open(file_path, "r", encoding="utf-8") as f:
                        out_f.write(f.read())
                        out_f.write("\n\n")  # 文件间空行
                except Exception as e:
                    out_f.write(f"读取失败: {e}\n\n")

print(f"所有 Java 文件内容已输出到 {output_file}")
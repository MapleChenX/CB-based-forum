import os

def count_java_lines(directory):
    total_lines = 0
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith(".java"):
                file_path = os.path.join(root, file)
                with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
                    lines = f.readlines()
                    code_lines = sum(1 for line in lines if line.strip() and not line.strip().startswith("//"))
                    total_lines += code_lines
    return total_lines


def count_code_lines(directory, extensions):
    total_lines = 0
    for root, _, files in os.walk(directory):
        for file in files:
            if any(file.endswith(ext) for ext in extensions):
                file_path = os.path.join(root, file)
                with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
                    lines = f.readlines()
                    code_lines = sum(1 for line in lines if line.strip() and not line.strip().startswith("//"))
                    total_lines += code_lines
    return total_lines

if __name__ == "__main__":
    # base_dir = os.path.abspath(os.path.dirname(__file__))
    base_java_dir = "C:\\Users\\25056\\Desktop\\Over\\forum\\my-project-backend"
    main_java_dir = os.path.join(base_java_dir, "src", "main", "java")
    main_lines = count_java_lines(main_java_dir) if os.path.exists(main_java_dir) else 0
    print(f"Main Java Code Lines: {main_lines}")
    
    base_vue_dir = "C:\\Users\\25056\\Desktop\\Over\\forum\\my-project-frontend"
    src_dir = os.path.join(base_vue_dir, "src")
    total_lines = count_code_lines(src_dir, [".js", ".vue"]) if os.path.exists(src_dir) else 0
    print(f"Main vue Code Lines: {total_lines}")

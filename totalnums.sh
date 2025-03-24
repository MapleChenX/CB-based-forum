#!/bin/bash

echo "开始统计代码行数..."

# 统计 Java 文件行数
java_lines=$(find . -type d -name "node_modules" -prune -o -type f -name "*.java" -print0 | xargs -0 cat | wc -l)

# 统计 Python 文件行数
py_lines=$(find . -type d -name "node_modules" -prune -o -type f -name "*.py" -print0 | xargs -0 cat | wc -l)


# 统计 JavaScript 文件行数
js_lines=$(find . -type d -name "node_modules" -prune -o -type f -name "*.js" -print0 | xargs -0 cat | wc -l)


vue_lines=$(find . -type d -name "node_modules" -prune -o -type f -name "*.vue" -print0 | xargs -0 cat | wc -l)

# 计算总行数
total_lines=$((java_lines + py_lines + js_lines + $vue_lines))

# 表头
echo "+-----------------------------+"
printf "| %-12s | %12s |\n" "file type" "total lines"
echo "+-----------------------------+"
# 各文件类型的统计行数
printf "| %-12s | %12d |\n" "Java" "$java_lines"
printf "| %-12s | %12d |\n" "Vue" "$vue_lines"
printf "| %-12s | %12d |\n" "JavaScript" "$js_lines"
printf "| %-12s | %12d |\n" "Python" "$py_lines"
echo "+-----------------------------+"
# 总行数
printf "| %-12s | %12d |\n" "total lines" "$total_lines"
echo "+-----------------------------+"
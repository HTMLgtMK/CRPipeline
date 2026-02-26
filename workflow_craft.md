我来帮你搭建这个自动化代码审查系统。这个方案使用 GitHub Actions 监听 PR 事件，调用 Coze API 进行智能审查，并自动回评结果。

## 完整实现方案

### 1. Coze 工作流配置

首先在 Coze 平台创建工作流：

**工作流输入参数：**
- `pr_title`: PR 标题
- `pr_description`: PR 描述
- `diff_content`: 代码变更内容（diff）
- `commit_messages`: 提交信息列表

**工作流节点设计：**
1. **代码分析节点**：解析 diff，识别变更的文件和代码逻辑
2. **规则检查节点**：检查代码规范、潜在 bug、安全问题
3. **建议生成节点**：生成具体的修改建议和优化方案
4. **输出格式化节点**：输出结构化 JSON 结果

**工作流输出格式：**
```json
{
  "version": "1.0",
  "summary": "审查摘要，整体评价本次PR的质量",
  "score": 85,
  "conclusion": "pass|fail|warning",
  "issues": [
    {
      "id": "issue-001",
      "severity": "error|warning|info",
      "category": "security|performance|bug|style|maintainability|best_practice",
      "file": "src/components/UserForm.jsx",
      "line": 42,
      "end_line": 45,
      "column": 8,
      "message": "具体问题描述，说明这里有什么错误或不规范",
      "suggestion": "具体的修改建议代码或说明",
      "code_snippet": "const password = req.body.password;",
      "fixed_code": "const password = validator.escape(req.body.password);",
      "documentation_link": "https://example.com/security-guide"
    }
  ],
  "metrics": {
    "total_files_changed": 5,
    "lines_added": 120,
    "lines_removed": 45,
    "complexity_score": 7.5,
    "test_coverage_impact": "+2%"
  },
  "statistics": {
    "error_count": 2,
    "warning_count": 5,
    "info_count": 3
  },
  "time_cost": {
    "analysis_time_ms": 1500,
    "total_time_ms": 2300
  }
}
```

 我来详细说明 Coze 工作流需要输出的 JSON 字段结构，以及 GitHub Actions 如何解析这些字段。

## Coze 工作流输出 JSON 规范

### 完整字段定义

```json
{
  "version": "1.0",
  "summary": "审查摘要，整体评价本次PR的质量",
  "score": 85,
  "conclusion": "pass|fail|warning",
  "issues": [
    {
      "id": "issue-001",
      "severity": "error|warning|info",
      "category": "security|performance|bug|style|maintainability|best_practice",
      "file": "src/components/UserForm.jsx",
      "line": 42,
      "end_line": 45,
      "column": 8,
      "message": "具体问题描述，说明这里有什么错误或不规范",
      "suggestion": "具体的修改建议代码或说明",
      "code_snippet": "const password = req.body.password;",
      "fixed_code": "const password = validator.escape(req.body.password);",
      "documentation_link": "https://example.com/security-guide"
    }
  ],
  "metrics": {
    "total_files_changed": 5,
    "lines_added": 120,
    "lines_removed": 45,
    "complexity_score": 7.5,
    "test_coverage_impact": "+2%"
  },
  "statistics": {
    "error_count": 2,
    "warning_count": 5,
    "info_count": 3
  },
  "time_cost": {
    "analysis_time_ms": 1500,
    "total_time_ms": 2300
  }
}
```

### 字段详细说明

| 字段路径 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| `version` | string | 否 | 版本号，用于兼容性处理 |
| `summary` | string | **是** | 整体审查摘要，支持 Markdown |
| `score` | number | **是** | 代码质量评分 0-100 |
| `conclusion` | string | **是** | 总体结论：`pass`(通过)/`fail`(失败)/`warning`(警告) |
| `issues` | array | **是** | 发现的问题列表 |
| `issues[].id` | string | 否 | 问题唯一标识 |
| `issues[].severity` | string | **是** | 严重程度：`error`/`warning`/`info` |
| `issues[].category` | string | **是** | 问题分类 |
| `issues[].file` | string | **是** | 文件路径 |
| `issues[].line` | number | **是** | 开始行号 |
| `issues[].end_line` | number | 否 | 结束行号（多行问题时） |
| `issues[].column` | number | 否 | 列号 |
| `issues[].message` | string | **是** | 问题描述 |
| `issues[].suggestion` | string | **是** | 修改建议 |
| `issues[].code_snippet` | string | 否 | 原始代码片段 |
| `issues[].fixed_code` | string | 否 | 修复后的代码示例 |
| `issues[].documentation_link` | string | 否 | 相关文档链接 |
| `metrics` | object | 否 | 代码变更指标 |
| `statistics` | object | 否 | 问题统计 |
| `time_cost` | object | 否 | 性能指标 |

### Coze 工作流配置示例

在 Coze 工作流的**结束节点**中，配置输出格式：

**输出变量设置：**

```javascript
// 在结束节点的输出部分，使用 Code 节点或 LLM 节点构造 JSON
const output = {
  version: "1.0",
  summary: input.analysis_summary || "审查完成",
  score: calculateScore(input.issues), // 自定义函数计算分数
  conclusion: determineConclusion(input.issues), // 根据问题数量判断
  issues: input.issues_list || [],
  metrics: {
    total_files_changed: input.files_changed,
    lines_added: input.additions,
    lines_removed: input.deletions
  },
  statistics: {
    error_count: input.issues_list.filter(i => i.severity === 'error').length,
    warning_count: input.issues_list.filter(i => i.severity === 'warning').length,
    info_count: input.issues_list.filter(i => i.severity === 'info').length
  }
};

// 必须输出为 JSON 字符串
output;
```

**结束节点配置：**
- 输出参数名：`review_result`
- 参数值：`{{output}}` （引用 Code 节点的输出）


### 2. GitHub Actions 工作流

在项目根目录创建 `.github/workflows/coze-code-review.yml`：

```yaml
name: Coze Code Review

on:
  pull_request:
    types: [opened, synchronize, reopened]
    paths:
      - '**.js'
      - '**.ts'
      - '**.jsx'
      - '**.tsx'
      - '**.py'
      - '**.java'
      - '**.go'
      # 添加你需要审查的文件类型

jobs:
  coze-review:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      pull-requests: write
      issues: write
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Get PR diff
        id: get-diff
        run: |
          # 获取 PR 的 diff 内容
          DIFF_CONTENT=$(curl -s -H "Authorization: token ${{ secrets.GITHUB_TOKEN }}" \
            -H "Accept: application/vnd.github.v3.diff" \
            "${{ github.event.pull_request.diff_url }}")
          
          # 转义特殊字符以便在 JSON 中使用
          DIFF_CONTENT=$(echo "$DIFF_CONTENT" | jq -sR .)
          echo "diff=$DIFF_CONTENT" >> $GITHUB_OUTPUT
          
          # 获取提交信息
          COMMITS=$(curl -s -H "Authorization: token ${{ secrets.GITHUB_TOKEN }}" \
            "${{ github.event.pull_request.commits_url }}")
          echo "commits=$COMMITS" >> $GITHUB_OUTPUT

      - name: Call Coze Workflow
        id: coze-review
        run: |
          # 构造请求体
          PAYLOAD=$(jq -n \
            --arg pr_title "${{ github.event.pull_request.title }}" \
            --arg pr_body "${{ github.event.pull_request.body }}" \
            --arg diff "${{ steps.get-diff.outputs.diff }}" \
            --arg commits "${{ steps.get-diff.outputs.commits }}" \
            --arg pr_number "${{ github.event.pull_request.number }}" \
            --arg repo "${{ github.repository }}" \
            '{
              workflow_id: "你的_coze_工作流_id",
              parameters: {
                pr_title: $pr_title,
                pr_description: $pr_body,
                diff_content: $diff,
                commit_messages: $commits,
                pr_number: $pr_number,
                repository: $repo
              }
            }')
          
          # 调用 Coze API
          RESPONSE=$(curl -s -X POST "https://api.coze.cn/v1/workflow/run" \
            -H "Authorization: Bearer ${{ secrets.COZE_API_TOKEN }}" \
            -H "Content-Type: application/json" \
            -d "$PAYLOAD")
          
          echo "result=$RESPONSE" >> $GITHUB_OUTPUT
          echo "Coze Response: $RESPONSE"

      - name: Parse and Comment Review Results
        uses: actions/github-script@v7
        with:
          github-token: ${{ secrets.GITHUB_TOKEN }}
          script: |
            const cozeResult = ${{ steps.coze-review.outputs.result }};
            
            // 解析 Coze 返回的结果
            let reviewData;
            try {
              reviewData = JSON.parse(cozeResult);
            } catch (e) {
              console.error('Failed to parse Coze result:', e);
              return;
            }
            
            // 构建评论内容
            let commentBody = `## 🤖 Coze 代码审查报告\n\n`;
            commentBody += `**评分**: ${reviewData.score || 'N/A'}/100\n\n`;
            commentBody += `### 审查摘要\n${reviewData.summary || '暂无摘要'}\n\n`;
            
            if (reviewData.issues && reviewData.issues.length > 0) {
              commentBody += `### 发现的问题\n\n`;
              
              // 按严重程度分组
              const errors = reviewData.issues.filter(i => i.severity === 'error');
              const warnings = reviewData.issues.filter(i => i.severity === 'warning');
              const infos = reviewData.issues.filter(i => i.severity === 'info');
              
              if (errors.length > 0) {
                commentBody += `#### ❌ 错误 (${errors.length})\n`;
                errors.forEach(issue => {
                  commentBody += `- **${issue.file}:${issue.line}** ${issue.message}\n`;
                  commentBody += `  - 💡 建议: ${issue.suggestion}\n\n`;
                });
              }
              
              if (warnings.length > 0) {
                commentBody += `#### ⚠️ 警告 (${warnings.length})\n`;
                warnings.forEach(issue => {
                  commentBody += `- **${issue.file}:${issue.line}** ${issue.message}\n`;
                  commentBody += `  - 💡 建议: ${issue.suggestion}\n\n`;
                });
              }
              
              if (infos.length > 0) {
                commentBody += `#### ℹ️ 提示 (${infos.length})\n`;
                infos.forEach(issue => {
                  commentBody += `- **${issue.file}:${issue.line}** ${issue.message}\n`;
                });
              }
            } else {
              commentBody += `✅ 未发现明显问题，代码看起来很棒！\n`;
            }
            
            commentBody += `\n---\n*由 Coze AI 自动审查*`;
            
            // 查找是否已有评论
            const { data: comments } = await github.rest.issues.listComments({
              owner: context.repo.owner,
              repo: context.repo.repo,
              issue_number: context.issue.number
            });
            
            const botComment = comments.find(comment => 
              comment.user.login === 'github-actions[bot]' && 
              comment.body.includes('Coze 代码审查报告')
            );
            
            if (botComment) {
              // 更新已有评论
              await github.rest.issues.updateComment({
                owner: context.repo.owner,
                repo: context.repo.repo,
                comment_id: botComment.id,
                body: commentBody
              });
            } else {
              // 创建新评论
              await github.rest.issues.createComment({
                owner: context.repo.owner,
                repo: context.repo.repo,
                issue_number: context.issue.number,
                body: commentBody
              });
            }
            
            // 如果有严重错误，可以设置失败状态
            const hasErrors = reviewData.issues?.some(i => i.severity === 'error');
            if (hasErrors) {
              core.setFailed('发现严重代码问题，请修复后重新提交');
            }
```

更新后的 `.github/workflows/coze-code-review.yml`：

```yaml
name: Coze Code Review

on:
  pull_request:
    types: [opened, synchronize, reopened]

jobs:
  coze-review:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      pull-requests: write
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Get PR Info
        id: pr-info
        run: |
          # 获取 PR 基本信息
          echo "title=${{ github.event.pull_request.title }}" >> $GITHUB_OUTPUT
          echo "body=${{ github.event.pull_request.body }}" >> $GITHUB_OUTPUT
          
          # 获取 diff（限制大小）
          DIFF=$(curl -s -H "Authorization: token ${{ secrets.GITHUB_TOKEN }}" \
            -H "Accept: application/vnd.github.v3.diff" \
            "${{ github.event.pull_request.diff_url }}" | head -c 50000)
          echo "diff<<EOF" >> $GITHUB_OUTPUT
          echo "$DIFF" >> $GITHUB_OUTPUT
          echo "EOF" >> $GITHUB_OUTPUT

      - name: Call Coze API
        id: coze
        run: |
          # 构造请求
          JSON_PAYLOAD=$(jq -n \
            --arg workflow_id "${{ secrets.COZE_WORKFLOW_ID }}" \
            --arg title "${{ steps.pr-info.outputs.title }}" \
            --arg body "${{ steps.pr-info.outputs.body }}" \
            --arg diff "${{ steps.pr-info.outputs.diff }}" \
            --arg pr_number "${{ github.event.number }}" \
            '{
              workflow_id: $workflow_id,
              parameters: {
                pr_title: $title,
                pr_description: $body,
                diff_content: $diff,
                pr_number: $pr_number
              }
            }')
          
          # 调用 API
          RESPONSE=$(curl -s -X POST "https://api.coze.cn/v1/workflow/run" \
            -H "Authorization: Bearer ${{ secrets.COZE_API_TOKEN }}" \
            -H "Content-Type: application/json" \
            -d "$JSON_PAYLOAD")
          
          # 提取工作流返回的数据（Coze 的响应结构）
          REVIEW_DATA=$(echo "$RESPONSE" | jq -r '.data' 2>/dev/null || echo "$RESPONSE")
          
          echo "result<<COZE_EOF" >> $GITHUB_OUTPUT
          echo "$REVIEW_DATA" >> $GITHUB_OUTPUT
          echo "COZE_EOF" >> $GITHUB_OUTPUT

      - name: Process Review Result
        id: process
        run: |
          RESULT='${{ steps.coze.outputs.result }}'
          
          # 解析关键字段用于后续步骤
          SCORE=$(echo "$RESULT" | jq -r '.score // 0')
          CONCLUSION=$(echo "$RESULT" | jq -r '.conclusion // "warning"')
          ERROR_COUNT=$(echo "$RESULT" | jq -r '.statistics.error_count // 0')
          
          echo "score=$SCORE" >> $GITHUB_OUTPUT
          echo "conclusion=$CONCLUSION" >> $GITHUB_OUTPUT
          echo "error_count=$ERROR_COUNT" >> $GITHUB_OUTPUT

      - name: Post Review Comment
        uses: actions/github-script@v7
        with:
          github-token: ${{ secrets.GITHUB_TOKEN }}
          script: |
            const fs = require('fs');
            
            // 读取 Coze 输出
            const reviewResult = ${{ steps.coze.outputs.result }};
            
            // 验证必要字段
            if (!reviewResult.issues || !Array.isArray(reviewResult.issues)) {
              core.setFailed('Coze 输出格式错误：缺少 issues 字段');
              return;
            }
            
            // 构建 Markdown 评论
            let body = `## 🔍 Coze AI 代码审查报告\n\n`;
            
            // 头部信息
            const score = reviewResult.score || 0;
            const scoreEmoji = score >= 90 ? '🟢' : score >= 70 ? '🟡' : '🔴';
            body += `### ${scoreEmoji} 综合评分：${score}/100\n\n`;
            
            // 结论标签
            const conclusion = reviewResult.conclusion || 'warning';
            const conclusionBadge = {
              'pass': '✅ **通过** - 代码质量良好，可以合并',
              'fail': '❌ **失败** - 存在严重问题，必须修复',
              'warning': '⚠️ **警告** - 存在问题，建议修复'
            }[conclusion];
            body += `**审查结论**：${conclusionBadge}\n\n`;
            
            // 摘要
            if (reviewResult.summary) {
              body += `### 📋 审查摘要\n${reviewResult.summary}\n\n`;
            }
            
            // 统计信息
            const stats = reviewResult.statistics || {};
            body += `### 📊 问题统计\n`;
            body += `- ❌ 错误：${stats.error_count || 0} 个\n`;
            body += `- ⚠️ 警告：${stats.warning_count || 0} 个\n`;
            body += `- ℹ️ 提示：${stats.info_count || 0} 个\n\n`;
            
            // 详细问题列表
            if (reviewResult.issues.length > 0) {
              body += `### 📝 详细问题\n\n`;
              
              // 按文件分组
              const grouped = reviewResult.issues.reduce((acc, issue) => {
                const file = issue.file || '未知文件';
                if (!acc[file]) acc[file] = [];
                acc[file].push(issue);
                return acc;
              }, {});
              
              for (const [file, issues] of Object.entries(grouped)) {
                body += `<details>\n<summary><b>${file}</b> (${issues.length} 个问题)</summary>\n\n`;
                
                issues.forEach(issue => {
                  const severityEmoji = {
                    'error': '❌',
                    'warning': '⚠️',
                    'info': 'ℹ️'
                  }[issue.severity] || '•';
                  
                  const lineInfo = issue.line ? `:${issue.line}${issue.end_line ? `-${issue.end_line}` : ''}` : '';
                  
                  body += `${severityEmoji} **${issue.category || '通用'}** \`${file}${lineInfo}\`\n\n`;
                  body += `> ${issue.message}\n\n`;
                  
                  if (issue.suggestion) {
                    body += `**💡 建议**：${issue.suggestion}\n\n`;
                  }
                  
                  if (issue.code_snippet) {
                    body += '```\n' + issue.code_snippet + '\n```\n\n';
                  }
                  
                  if (issue.fixed_code) {
                    body += '**✅ 修复后**：\n```\n' + issue.fixed_code + '\n```\n\n';
                  }
                  
                  if (issue.documentation_link) {
                    body += `📚 [查看文档](${issue.documentation_link})\n\n`;
                  }
                  
                  body += '---\n\n';
                });
                
                body += '</details>\n\n';
              }
            }
            
            // 指标信息（可选）
            if (reviewResult.metrics) {
              const m = reviewResult.metrics;
              body += `### 📈 变更指标\n`;
              body += `- 变更文件：${m.total_files_changed || '-'} 个\n`;
              body += `- 新增代码：${m.lines_added || '-'} 行\n`;
              body += `- 删除代码：${m.lines_removed || '-'} 行\n`;
              if (m.complexity_score) body += `- 复杂度评分：${m.complexity_score}\n`;
              body += `\n`;
            }
            
            body += `\n---\n*⏱️ 分析耗时：${reviewResult.time_cost?.total_time_ms || '-'}ms | 由 Coze AI 自动生成*`;
            
            // 查找现有评论
            const { data: comments } = await github.rest.issues.listComments({
              owner: context.repo.owner,
              repo: context.repo.repo,
              issue_number: context.issue.number
            });
            
            const botComment = comments.find(c => 
              c.user.type === 'Bot' && 
              c.body.includes('Coze AI 代码审查报告')
            );
            
            if (botComment) {
              await github.rest.issues.updateComment({
                owner: context.repo.owner,
                repo: context.repo.repo,
                comment_id: botComment.id,
                body: body
              });
            } else {
              await github.rest.issues.createComment({
                owner: context.repo.owner,
                repo: context.repo.repo,
                issue_number: context.issue.number,
                body: body
              });
            }

      - name: Check Conclusion
        if: ${{ steps.process.outputs.conclusion == 'fail' }}
        run: |
          echo "::error::代码审查未通过，存在严重问题"
          exit 1
```

### 3. 配置 GitHub Secrets

在仓库 Settings → Secrets and variables → Actions 中添加：

- `COZE_API_TOKEN`: 你的 Coze API 访问令牌

### 4. Coze 工作流详细配置建议

**提示词模板（用于代码分析节点）：**

```
你是一位资深代码审查专家。请审查以下 Pull Request 的代码变更：

PR标题: {{pr_title}}
PR描述: {{pr_description}}

代码变更:
{{diff_content}}

请从以下维度进行审查：
1. 代码规范性（命名、格式、注释）
2. 潜在 Bug（空指针、逻辑错误、边界条件）
3. 性能问题（算法复杂度、资源泄漏）
4. 安全隐患（SQL注入、XSS、敏感信息泄露）
5. 可维护性（代码重复、圈复杂度）

输出要求：
- 只关注新增或修改的代码
- 每个问题必须包含：文件路径、行号、问题描述、修改建议
- 严重级别分为：error（必须修复）、warning（建议修复）、info（仅供参考）
- 最后给出整体评分（0-100）
```

### 5. 高级功能扩展

**增量审查**（只审查最新变更）：

```yaml
- name: Get changed files
  id: changed-files
  uses: tj-actions/changed-files@v42
  with:
    since_last_remote_commit: true
```

**多语言支持**：

```yaml
strategy:
  matrix:
    language: [javascript, python, java]
  fail-fast: false
```

**审查结果持久化**：

```yaml
- name: Upload review results
  uses: actions/upload-artifact@v4
  with:
    name: review-results
    path: review-report.json
```

### 6. 效果预览

当开发者提交 PR 后，会自动收到类似这样的评论：

---

## 🤖 Coze 代码审查报告

**评分**: 78/100

### 审查摘要
本次提交引入了 2 个潜在错误和 3 个警告，建议修复后再合并。

### 发现的问题

#### ❌ 错误 (2)
- **src/utils/auth.js:45** 存在 SQL 注入风险，用户输入未经验证直接拼接到 SQL 语句
  - 💡 建议: 使用参数化查询或 ORM 的预编译语句

- **src/api/user.js:12** 异步函数缺少错误处理，可能导致未捕获的 Promise 拒绝
  - 💡 建议: 添加 try-catch 块或 .catch() 处理

#### ⚠️ 警告 (3)
- **src/components/Form.jsx:23** 组件 re-render 性能问题，建议在 useEffect 中添加依赖项数组
  - 💡 建议: 使用 `useMemo` 缓存计算结果

---

### 7. 故障排查

如果工作流运行失败，检查：
1. Coze API Token 是否有效且未过期
2. 工作流 ID 是否正确
3. 代码 diff 是否过大（建议限制在 500 行以内）
4. 查看 Actions 日志中的详细错误信息

这个方案实现了全自动的 AI 代码审查，可以显著提升代码质量和审查效率。需要我详细解释某个部分或根据你的具体技术栈调整配置吗？
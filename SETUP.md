# Coze Code Review Setup Guide

## 配置步骤

### 1. Coze 工作流配置

在 Coze 平台创建工作流，配置如下：

**输入参数：**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| `pr_title` | string | PR 标题 |
| `pr_description` | string | PR 描述 |
| `diff_content` | string | 代码变更 diff |
| `pr_number` | string | PR 编号 |

**输出格式（JSON）：**
```json
{
  "version": "1.0",
  "summary": "审查摘要",
  "score": 85,
  "conclusion": "pass|fail|warning",
  "issues": [
    {
      "severity": "error|warning|info",
      "category": "security|performance|bug|style|maintainability|best_practice",
      "file": "src/example.kt",
      "line": 42,
      "end_line": 45,
      "message": "问题描述",
      "suggestion": "修改建议",
      "code_snippet": "原始代码",
      "fixed_code": "修复后代码",
      "documentation_link": "https://..."
    }
  ],
  "metrics": {
    "total_files_changed": 5,
    "lines_added": 120,
    "lines_removed": 45,
    "complexity_score": 7.5
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

### 2. GitHub Secrets 配置

在仓库 **Settings → Secrets and variables → Actions** 中添加：

| Secret 名称 | 说明 |
|------------|------|
| `COZE_API_TOKEN` | Coze API 访问令牌 |
| `COZE_WORKFLOW_ID` | Coze 工作流 ID |

### 3. 获取 Coze API Token

1. 访问 [Coze API](https://www.coze.cn/open/oauth/pats)
2. 创建 Personal Access Token
3. 复制 Token 并添加到 GitHub Secrets

### 4. 获取工作流 ID

1. 在 Coze 平台打开你的工作流
2. 点击 **发布** → **API 调用**
3. 复制 Workflow ID（格式如：`7xxxxxxxxxxxxxxx`）

## 使用方法

配置完成后，每次创建或更新 PR 时会自动触发代码审查，并在 PR 页面添加评论。

## 自定义审查规则

在 Coze 工作流的提示词中配置审查维度：

```
你是一位资深代码审查专家。请审查以下 Pull Request 的代码变更：

PR 标题：{{pr_title}}
PR 描述：{{pr_description}}

代码变更:
{{diff_content}}

请从以下维度进行审查：
1. 代码规范性（命名、格式、注释）
2. 潜在 Bug（空指针、逻辑错误、边界条件）
3. 性能问题（算法复杂度、资源泄漏）
4. 安全隐患（SQL 注入、XSS、敏感信息泄露）
5. 可维护性（代码重复、圈复杂度）

输出要求：
- 只关注新增或修改的代码
- 每个问题必须包含：文件路径、行号、问题描述、修改建议
- 严重级别分为：error（必须修复）、warning（建议修复）、info（仅供参考）
- 最后给出整体评分（0-100）
```

## 故障排查

| 问题 | 解决方案 |
|------|----------|
| 工作流不触发 | 检查 PR 是否来自 fork，fork 的 PR 需要手动授权 |
| API 调用失败 | 检查 Token 是否有效，工作流 ID 是否正确 |
| 输出格式错误 | 确保 Coze 工作流输出符合 JSON 规范 |
| 评论未发布 | 检查 workflow 的 `pull-requests: write` 权限 |

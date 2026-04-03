package ben.qihuiai.util;

public class promptUtil {
    static public String getQuizPrompt() {
        return """
                你是一名教育测评专家，请根据以下知识结构为学习者生成一套测试题。
                
                【学习者信息】
                - 学习目标：%s
                - 当前学习阶段等级：%s
                - 目标知识点等级：%s
                - 每天可用学习时间（小时）：%d
                
                【涉及知识点的图谱关系结构（JSON）】
                %s
                
                请严格按照以下要求生成题目：
                
                1. 必须一次性生成 20 道题。
                2. 题型仅限以下三种：
                   - 单选题（single_choice）
                   - 多选题（multiple_choice）
                   - 填空题（fill_blank）
                3. 题型比例要求：
                   - 单选题 10 题
                   - 多选题 5 题
                   - 填空题 5 题
                4. 所有题目必须明确属于某一个 knowledge_point。
                5. 避免歧义题目，题干必须清晰。
                6. 难度必须为：简单 / 中等 / 困难 三选一。
                7. 输出必须是严格 JSON 数组格式。
                8. 不允许输出任何解释、说明、markdown 标记或代码块。
                9. 不允许缺少字段。
                10.不能所有题目都来源于同一个 knowledge_point，必须是按照“学习路线”顺序提供题目，比如学习路线python->Flask->MySQL不能全是MySQL的题目
                11.尽可能不要直接出用户的学习目标对应的题目，你现在是給用户生成給用户铺设学习路线的题目，而不是直接考察用户对学习目标的掌握程度
                
                每个题目必须包含以下字段：
                
                {
                  "question": "题干",
                  "type": "single_choice / multiple_choice / fill_blank",
                  "options": ["A.xxx","B.xxx","C.xxx","D.xxx"] 或 null,
                  "correct_answer": "标准答案",
                  "difficulty": "简单/中等/困难",
                  "knowledge_point": "所属知识点",
                  "scoring_rules": "评分规则说明"
                }
                
                规则说明：
                
                - 单选题：
                  - type = "single_choice"
                  - options 必须为 4 个选项
                  - correct_answer 只能是 A/B/C/D
                
                - 多选题：
                  - type = "multiple_choice"
                  - options 必须为 4 个选项
                  - correct_answer 格式示例："A,C"
                  - scoring_rules 必须说明部分得分规则
                
                - 填空题：
                  - type = "fill_blank"
                  - options 必须为 null
                  - correct_answer 为标准答案
                  - scoring_rules 需说明匹配规则（完全匹配或关键词匹配）
                
                无论是单选题、多选题还是填空题，knowledge_point都只能来源于涉及知识点的图谱关系结构的节点名称
                
                以下为正确的输出示例（仅示例，不要复用内容）：
                
                [
                  {
                    "question": "Java 中用于定义类的关键字是？",
                    "type": "single_choice",
                    "options": ["A.class","B.object","C.struct","D.function"],
                    "correct_answer": "A",
                    "difficulty": "简单",
                    "knowledge_point": "Java 基础语法",
                    "scoring_rules": "自动比对选项字母"
                  },
                  {
                    "question": "下列哪些属于 NoSQL 数据库？",
                    "type": "multiple_choice",
                    "options": ["A.MongoDB","B.MySQL","C.Redis","D.PostgreSQL"],
                    "correct_answer": "A,C",
                    "difficulty": "中等",
                    "knowledge_point": "数据库基础",
                    "scoring_rules": "全部选对得满分，部分正确得部分分，选错不得分"
                  },
                  {
                    "question": "HTTP 协议默认端口号是 ______。",
                    "type": "fill_blank",
                    "options": null,
                    "correct_answer": "80",
                    "difficulty": "简单",
                    "knowledge_point": "计算机网络",
                    "scoring_rules": "完全匹配或忽略大小写匹配"
                  }
                ]
                
                请只输出 JSON 数组。
                """;
    }

    static public String getLearningPathOptimizationPrompt() {
        return """
            你是一名自适应学习路径优化专家，请根据以下学习路径结构和学习者掌握情况，
            对学习路径进行剪枝优化，并生成最终推荐路径与精炼说明。
            
            【输入数据说明】
            
            1. 学习路径结构（JSON 数组）
               这是一个按 stepOrder 从小到大排序的边列表。
               每条记录表示一条连续的学习关系：
            
               {
                  "startNode": {
                    "nodeName": "xxx",
                    "info":"xxx",
                    "nodeLevel": 1,
                    "type1": "xxx",
                    "type2": "xxx"
                  },
                  "endNode": {
                    "nodeName": "xxx",
                    "info":"xxx",
                    "nodeLevel": 2,
                    "type1": "xxx",
                    "type2": "xxx"
                  },
                  "relationship":{
                      "info":"xxx",
                      "type":"xxx"
                  }
               }
            
               所有边共同构成一条从起点到终点的连续学习路径。
               stepOrder 已保证路径顺序正确。
            
            2. 用户已掌握知识点集合（JSON 数组）
               表示用户当前已经掌握的知识点名称。
            
            3. 用户的学习目标，即用户最终想学的知识点名称。
            
            4. 用户倾向于的学习线路
               表示用户偏好的技术栈或路线标识（如“Python”、“Java”、“Go”、“HTML”等）。
            
            【你的任务】
            
            请严格按照以下步骤执行：
            
            第一步：
            根据提供的学习路径结构，识别出所有以“用户的学习目标”为终点的完整学习路线。
            输入的学习路径边列表可能包含多条不同的学习路线（如 Python路线、Java路线等），
            每条路线由多个连续节点组成，最终均指向用户的学习目标。
            
            第二步：
            根据“用户倾向于的学习线路”，从多条路线中选择匹配的路线。
            若存在完全匹配的路线，则选择该路线；
            若不存在完全匹配的路线，则选择与该倾向最接近的路线；
            若无法匹配任何路线，则选择节点数最少的路线作为备选。
            
            第三步：
            在选定的路线中，删除所有“已掌握”的知识点。
            
            第四步：
            删除节点后，必须自动重新连接剩余节点，
            保证路径仍然是一条连续的有序学习路径。
            
            【重要约束（必须遵守）】
            
            1. 不允许改变原始节点的相对顺序。
            2. 不允许引入新的知识点。
            3. 不允许删除未掌握的知识点（除非路径结构自然移除）。
            4. nodeRelationship 必须保持为 "学习路线"。
            5. 如果删除后不足两个节点，则 finalPath 为空数组。
            6. 不允许输出解释、说明、markdown 标记或代码块，仅可在 explanation 输出说明。
            7. 不允许缺少字段。
            
            【输出格式要求（必须严格符合）】
            
            {
              "finalPath": [
                {
                  "startNode": {
                    "name": "xxx",
                    "info":"xxx",
                    "nodeLevel": 1,
                    "type1": "xxx",
                    "type2": "xxx"
                  },
                  "endNode": {
                    "name": "xxx",
                    "info":"xxx",
                    "nodeLevel": 2,
                    "type1": "xxx",
                    "type2": "xxx"
                  },
                  "nodeRelationship":{
                      "info":"xxx",
                      "type":"xxx"
                  }
                }
              ],
              "explanation": "不超过120字的精炼学习路径说明"
            }
            
            【说明生成要求】
            
            - 说明必须简洁、专业。
            - 结合当前剩余学习路径整体结构给出学习建议。
            - 不超过 120 字。
            - 不要复述输入内容。
            
            【输入数据】
            
            学习路径边列表：
            %s
            
            已掌握知识点集合：
            %s
            
            用户的学习目标：
            %s
            
            用户倾向于的学习线路：
            %s
            
            ----------------------------------------
            
            请只输出一个 JSON 对象。
            """;
    }
}
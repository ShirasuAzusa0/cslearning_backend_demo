# 题目数据定义（题号、知识点、难度）
questions = [
    {'id': 1, 'topic': 'HTML5', 'difficulty': '基础'},
    {'id': 2, 'topic': 'CSS3', 'difficulty': '基础'},
    {'id': 3, 'topic': 'JavaScript', 'difficulty': '基础'},
    {'id': 4, 'topic': 'TypeScript', 'difficulty': '基础'},
    {'id': 5, 'topic': 'Vue3', 'difficulty': '中等'},
    {'id': 6, 'topic': 'React', 'difficulty': '中等'},
    {'id': 7, 'topic': 'Angular', 'difficulty': '中等'},
    {'id': 8, 'topic': 'CSS3', 'difficulty': '中等'},
    {'id': 9, 'topic': 'Vue3', 'difficulty': '进阶'},
    {'id': 10, 'topic': 'React', 'difficulty': '进阶'},
    {'id': 11, 'topic': 'Angular', 'difficulty': '进阶'},
    {'id': 12, 'topic': 'TypeScript', 'difficulty': '进阶'},
    {'id': 13, 'topic': 'React', 'difficulty': '进阶'},
    {'id': 14, 'topic': 'Vue3', 'difficulty': '高阶'},
    {'id': 15, 'topic': 'CSS3', 'difficulty': '高阶'},
    {'id': 16, 'topic': 'JavaScript', 'difficulty': '高阶'},
    {'id': 17, 'topic': '框架对比', 'difficulty': '综合应用'},
    {'id': 18, 'topic': 'TypeScript', 'difficulty': '综合应用'},
    {'id': 19, 'topic': '性能优化', 'difficulty': '综合应用'},
    {'id': 20, 'topic': '架构设计', 'difficulty': '综合应用'}
]

# 模拟用户答题数据（正确：True，错误：False）
'''
user_answers = {
    1: True, 2: False, 3: True, 4: False, 5: True,
    6: False, 7: True, 8: True, 9: False, 10: True,
    11: False, 12: True, 13: False, 14: True, 15: False,
    16: True, 17: True, 18: False, 19: True, 20: False
}
'''

# 新增外部数据权重（模拟数据）
external_weights = {
    'HTML5': {'trend': 0.1, 'resource': 0.8, 'demand': 0.3},
    'CSS3': {'trend': 0.2, 'resource': 0.9, 'demand': 0.7},
    'JavaScript': {'trend': 0.3, 'resource': 0.95, 'demand': 0.8},
    'TypeScript': {'trend': 0.9, 'resource': 0.7, 'demand': 0.95},
    'Vue3': {'trend': 0.6, 'resource': 0.6, 'demand': 0.4},
    'React': {'trend': 0.8, 'resource': 0.95, 'demand': 0.85},
    'Angular': {'trend': 0.3, 'resource': 0.5, 'demand': 0.35},
    '框架对比': {'trend': 0.6, 'resource': 0.3, 'demand': 0.7},
    '性能优化': {'trend': 0.7, 'resource': 0.4, 'demand': 0.75},
    '架构设计': {'trend': 0.65, 'resource': 0.3, 'demand': 0.6}
}


# ================== 核心算法 ==================
def generate_learning_path(user_answers, use_enhanced=True):
    # 知识点统计（基础模型逻辑）
    statistics = {}
    for q in questions:
        topic = q['topic']
        diff = q['difficulty']
        if topic not in statistics:
            statistics[topic] = {}
        if diff not in statistics[topic]:
            statistics[topic][diff] = {'correct': 0, 'total': 0}
        statistics[topic][diff]['total'] += 1
        if user_answers['result'][q['id'] - 1].get('answer', False):
            statistics[topic][diff]['correct'] += 1

    # 计算正确率
    for topic in statistics:
        for diff in statistics[topic]:
            stats = statistics[topic][diff]
            stats['rate'] = (stats['correct'] / stats['total']) * 100 if stats['total'] > 0 else 0

    # 生成初始学习路径
    threshold = 60
    difficulty_order = ['基础', '中等', '进阶', '高阶', '综合应用']
    learning_path = []
    for topic in statistics:
        for diff in difficulty_order:
            if diff in statistics[topic]:
                rate = statistics[topic][diff]['rate']
                if rate < threshold:
                    learning_path.append({'topic': topic, 'difficulty': diff, 'rate': rate})

    # 增强模型逻辑
    if use_enhanced:
        def calculate_priority(topic, difficulty, correct_rate):
            difficulty_weights = {'基础': 1.2, '中等': 1.0, '进阶': 0.9, '高阶': 0.8, '综合应用': 0.7}
            knowledge_gap = (100 - correct_rate) / 100

            trend = external_weights[topic]['trend']
            resource = external_weights[topic]['resource']
            demand = external_weights[topic]['demand']
            external_score = 0.3 * trend + 0.25 * resource + 0.45 * demand

            return knowledge_gap * difficulty_weights[difficulty] * (1 + external_score)

        # 计算优先级并重新排序
        enhanced_path = []
        for item in learning_path:
            priority = calculate_priority(item['topic'], item['difficulty'], item['rate'])
            enhanced_path.append({**item, 'priority': priority})

        # 添加趋势标识
        trend_symbols = {0.9: '🚀', 0.7: '↑', 0.5: '→'}
        for item in enhanced_path:
            trend = external_weights[item['topic']]['trend']
            item['symbol'] = next((v for k, v in trend_symbols.items() if trend >= k), '')

        return sorted(enhanced_path, key=lambda x: -x['priority'])
    else:
        # 基础排序逻辑
        difficulty_weight = {d: idx for idx, d in enumerate(difficulty_order)}
        return sorted(learning_path, key=lambda x: (difficulty_weight[x['difficulty']], x['rate']))

'''
# ================== 结果输出 ==================
def print_results(path, model_name):
    print(f"\n【{model_name}模型输出】")
    symbols = {'🚀': '高趋势', '↑': '中趋势', '→': '稳定'}
    for idx, item in enumerate(path, 1):
        if 'priority' in item:
            info = f"{item['symbol']} 优先级:{item['priority']:.2f}"
        else:
            info = f"正确率:{item['rate']:.1f}%"
        print(f"{idx}. [{item['difficulty']}] {item['topic']} | {info}")


# ================== 使用示例 ==================
if __name__ == "__main__":
    # 生成两种模型结果
    base_path = generate_learning_path(use_enhanced=False)
    enhanced_path = generate_learning_path(use_enhanced=True)

    # 对比输出
    print_results(enhanced_path, "增强优化")
'''
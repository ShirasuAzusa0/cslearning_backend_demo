启慧智学agent项目整体架构如下：
```
qihui-agent/
├── api/                     ← api层，实现对前端/主后端的接口对接
├── configs/                 ← 配置项
├── model/                   ← 本地部署的模型
├── models/                  ← models层，实现数据库表映射
├── repositories/            ← repositories层，对接数据库增删改查接口
├── services/                ← services层，实现服务事务处理
├── utils/                   ← 工具函数、模型载入、响应模板等
├── main.py                  ← 项目入口
└── README.md                ← 项目结构说明
```
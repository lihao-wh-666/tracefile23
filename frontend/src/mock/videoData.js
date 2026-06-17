const teachers = [
  {
    id: 1,
    name: '张明远',
    avatar: '',
    title: '高级讲师',
    description: '计算机科学硕士，10年软件开发经验，专注于Java架构设计与微服务领域',
    courses: 15,
    students: 12580
  },
  {
    id: 2,
    name: '李婉清',
    avatar: '',
    title: '资深前端工程师',
    description: '前阿里高级前端工程师，8年Vue/React开发经验，擅长性能优化与架构设计',
    courses: 12,
    students: 9860
  },
  {
    id: 3,
    name: '王建国',
    avatar: '',
    title: '数据库专家',
    description: 'Oracle认证专家，深耕数据库领域15年，精通MySQL、PostgreSQL调优',
    courses: 8,
    students: 7650
  },
  {
    id: 4,
    name: '陈思雨',
    avatar: '',
    title: '全栈开发工程师',
    description: '全栈技术专家，擅长Node.js、Python、React等技术栈，多次参与大型项目架构设计',
    courses: 20,
    students: 15320
  },
  {
    id: 5,
    name: '刘天翔',
    avatar: '',
    title: '算法专家',
    description: '清华大学计算机博士，ACM金牌教练，专注算法教学与竞赛培训',
    courses: 10,
    students: 8900
  }
]

const categories = [
  { id: 1, name: '前端开发' },
  { id: 2, name: '后端开发' },
  { id: 3, name: '数据库' },
  { id: 4, name: '算法与数据结构' },
  { id: 5, name: '系统架构' },
  { id: 6, name: '运维与DevOps' },
  { id: 7, name: '人工智能' },
  { id: 8, name: '项目管理' }
]

const courseOutlines = [
  [
    { chapter: 1, title: '课程介绍与环境搭建', duration: '15:30', free: true },
    { chapter: 2, title: '基础语法与变量', duration: '28:45', free: true },
    { chapter: 3, title: '数据类型详解', duration: '32:10', free: false },
    { chapter: 4, title: '流程控制语句', duration: '25:00', free: false },
    { chapter: 5, title: '函数与作用域', duration: '35:20', free: false },
    { chapter: 6, title: '数组与对象操作', duration: '28:55', free: false },
    { chapter: 7, title: 'DOM操作基础', duration: '30:15', free: false },
    { chapter: 8, title: '事件处理机制', duration: '27:40', free: false },
    { chapter: 9, title: '异步编程入门', duration: '33:50', free: false },
    { chapter: 10, title: '项目实战与总结', duration: '45:00', free: false }
  ],
  [
    { chapter: 1, title: 'Vue3 核心概念', duration: '20:15', free: true },
    { chapter: 2, title: 'Composition API 详解', duration: '35:30', free: false },
    { chapter: 3, title: '响应式原理深入', duration: '42:00', free: false },
    { chapter: 4, title: '组件设计模式', duration: '38:45', free: false },
    { chapter: 5, title: '状态管理 Pinia', duration: '30:20', free: false },
    { chapter: 6, title: '路由 Vue Router', duration: '28:10', free: false },
    { chapter: 7, title: '性能优化策略', duration: '36:55', free: false },
    { chapter: 8, title: '企业级项目实战', duration: '55:30', free: false }
  ],
  [
    { chapter: 1, title: 'Java 语言基础', duration: '25:00', free: true },
    { chapter: 2, title: '面向对象编程', duration: '40:15', free: false },
    { chapter: 3, title: '集合框架详解', duration: '35:40', free: false },
    { chapter: 4, title: '多线程编程', duration: '45:20', free: false },
    { chapter: 5, title: 'IO 与 NIO', duration: '38:10', free: false },
    { chapter: 6, title: '反射与注解', duration: '32:55', free: false },
    { chapter: 7, title: 'Java 8 新特性', duration: '42:30', free: false },
    { chapter: 8, title: 'Spring 框架入门', duration: '50:00', free: false },
    { chapter: 9, title: 'Spring Boot 实战', duration: '60:15', free: false },
    { chapter: 10, title: '微服务架构实践', duration: '55:40', free: false }
  ],
  [
    { chapter: 1, title: '数据库基础概念', duration: '18:20', free: true },
    { chapter: 2, title: 'SQL 语法详解', duration: '45:30', free: false },
    { chapter: 3, title: '表设计与范式', duration: '35:15', free: false },
    { chapter: 4, title: '索引优化原理', duration: '40:50', free: false },
    { chapter: 5, title: '事务与锁机制', duration: '38:25', free: false },
    { chapter: 6, title: '存储过程与函数', duration: '32:10', free: false },
    { chapter: 7, title: '性能调优实战', duration: '55:40', free: false },
    { chapter: 8, title: '主从复制与集群', duration: '48:55', free: false }
  ],
  [
    { chapter: 1, title: '算法复杂度分析', duration: '22:15', free: true },
    { chapter: 2, title: '数组与链表', duration: '35:40', free: false },
    { chapter: 3, title: '栈与队列', duration: '28:30', free: false },
    { chapter: 4, title: '树与二叉树', duration: '45:20', free: false },
    { chapter: 5, title: '哈希表', duration: '30:15', free: false },
    { chapter: 6, title: '排序算法', duration: '42:50', free: false },
    { chapter: 7, title: '动态规划', duration: '55:30', free: false },
    { chapter: 8, title: '贪心算法', duration: '38:45', free: false },
    { chapter: 9, title: '图论基础', duration: '50:10', free: false },
    { chapter: 10, title: '经典算法题精讲', duration: '65:20', free: false }
  ]
]

const videos = [
  {
    id: 1,
    title: 'Vue3 从入门到精通完整教程',
    description: '本课程全面讲解Vue3的核心概念与实战应用，包括Composition API、响应式原理、组件设计、状态管理等内容，帮助你快速掌握Vue3开发技能。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Vue3%20programming%20course%20thumbnail%20with%20blue%20theme%20modern%20tech%20style&image_size=landscape_16_9',
    duration: '12小时35分',
    durationSeconds: 45300,
    publishDate: '2024-03-15',
    viewCount: 12580,
    likeCount: 3560,
    collectCount: 2890,
    categoryId: 1,
    categoryName: '前端开发',
    teacherId: 2,
    difficulty: '入门',
    tags: ['Vue3', '前端', 'JavaScript'],
    chapters: 10,
    rating: 4.8,
    ratingCount: 1256
  },
  {
    id: 2,
    title: 'Java 高级开发与架构设计',
    description: '深入学习Java高级特性与架构设计思想，涵盖多线程、JVM调优、设计模式、微服务架构等核心内容，助你成为Java架构师。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Java%20programming%20advanced%20course%20thumbnail%20coffee%20cup%20tech%20style&image_size=landscape_16_9',
    duration: '18小时20分',
    durationSeconds: 66000,
    publishDate: '2024-02-20',
    viewCount: 18960,
    likeCount: 5230,
    collectCount: 4120,
    categoryId: 2,
    categoryName: '后端开发',
    teacherId: 1,
    difficulty: '高级',
    tags: ['Java', '架构', '微服务'],
    chapters: 15,
    rating: 4.9,
    ratingCount: 2130
  },
  {
    id: 3,
    title: 'MySQL 数据库深度优化',
    description: '从底层原理到实战调优，全面掌握MySQL数据库性能优化技巧，包括索引优化、SQL优化、配置优化、架构优化等。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=MySQL%20database%20optimization%20course%20thumbnail%20database%20icon%20dark%20theme&image_size=landscape_16_9',
    duration: '9小时45分',
    durationSeconds: 35100,
    publishDate: '2024-04-10',
    viewCount: 8750,
    likeCount: 2340,
    collectCount: 1980,
    categoryId: 3,
    categoryName: '数据库',
    teacherId: 3,
    difficulty: '进阶',
    tags: ['MySQL', '数据库', '性能优化'],
    chapters: 8,
    rating: 4.7,
    ratingCount: 890
  },
  {
    id: 4,
    title: 'JavaScript 核心原理精讲',
    description: '深入理解JavaScript核心机制，包括作用域、闭包、原型链、事件循环、异步编程等底层原理，夯实你的JS基础。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=JavaScript%20core%20concepts%20course%20thumbnail%20yellow%20black%20theme&image_size=landscape_16_9',
    duration: '15小时10分',
    durationSeconds: 54600,
    publishDate: '2024-01-25',
    viewCount: 25680,
    likeCount: 7890,
    collectCount: 6540,
    categoryId: 1,
    categoryName: '前端开发',
    teacherId: 4,
    difficulty: '进阶',
    tags: ['JavaScript', '前端', '原理'],
    chapters: 12,
    rating: 4.9,
    ratingCount: 3250
  },
  {
    id: 5,
    title: '数据结构与算法实战',
    description: '系统学习常用数据结构与经典算法，配合大量LeetCode真题讲解，提升你的编程思维与算法能力。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=data%20structure%20algorithm%20course%20thumbnail%20geometric%20shapes%20tech&image_size=landscape_16_9',
    duration: '20小时30分',
    durationSeconds: 73800,
    publishDate: '2024-03-05',
    viewCount: 15420,
    likeCount: 4560,
    collectCount: 3890,
    categoryId: 4,
    categoryName: '算法与数据结构',
    teacherId: 5,
    difficulty: '进阶',
    tags: ['算法', '数据结构', 'LeetCode'],
    chapters: 18,
    rating: 4.8,
    ratingCount: 1680
  },
  {
    id: 6,
    title: 'React 全家桶开发实战',
    description: '从零开始学习React生态系统，包括Hooks、Redux、React Router、Next.js等，打造企业级React应用。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=React%20full%20stack%20course%20thumbnail%20react%20atom%20logo%20blue%20theme&image_size=landscape_16_9',
    duration: '16小时45分',
    durationSeconds: 60300,
    publishDate: '2024-04-20',
    viewCount: 11250,
    likeCount: 3120,
    collectCount: 2670,
    categoryId: 1,
    categoryName: '前端开发',
    teacherId: 2,
    difficulty: '进阶',
    tags: ['React', '前端', 'Hooks'],
    chapters: 14,
    rating: 4.7,
    ratingCount: 1120
  },
  {
    id: 7,
    title: 'Spring Cloud 微服务架构',
    description: '全面掌握Spring Cloud微服务技术栈，包括服务注册、配置中心、网关、熔断、链路追踪等核心组件。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Spring%20Cloud%20microservices%20course%20thumbnail%20green%20leaf%20architecture&image_size=landscape_16_9',
    duration: '22小时15分',
    durationSeconds: 80100,
    publishDate: '2024-02-10',
    viewCount: 13680,
    likeCount: 4230,
    collectCount: 3560,
    categoryId: 5,
    categoryName: '系统架构',
    teacherId: 1,
    difficulty: '高级',
    tags: ['Spring Cloud', '微服务', 'Java'],
    chapters: 16,
    rating: 4.8,
    ratingCount: 1450
  },
  {
    id: 8,
    title: 'Docker 容器化与K8s编排',
    description: '从零开始学习Docker容器技术与Kubernetes编排，掌握云原生应用部署与管理的核心技能。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Docker%20Kubernetes%20course%20thumbnail%20container%20ship%20blue%20tech&image_size=landscape_16_9',
    duration: '14小时20分',
    durationSeconds: 51600,
    publishDate: '2024-03-28',
    viewCount: 9870,
    likeCount: 2780,
    collectCount: 2340,
    categoryId: 6,
    categoryName: '运维与DevOps',
    teacherId: 4,
    difficulty: '进阶',
    tags: ['Docker', 'K8s', '云原生'],
    chapters: 12,
    rating: 4.6,
    ratingCount: 920
  },
  {
    id: 9,
    title: 'Python 数据分析与可视化',
    description: '使用Python进行数据分析，掌握NumPy、Pandas、Matplotlib、Seaborn等工具，让数据说话。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Python%20data%20analysis%20course%20thumbnail%20charts%20graphs%20colorful&image_size=landscape_16_9',
    duration: '11小时50分',
    durationSeconds: 42600,
    publishDate: '2024-04-05',
    viewCount: 14320,
    likeCount: 3890,
    collectCount: 3210,
    categoryId: 7,
    categoryName: '人工智能',
    teacherId: 4,
    difficulty: '入门',
    tags: ['Python', '数据分析', '可视化'],
    chapters: 10,
    rating: 4.7,
    ratingCount: 1340
  },
  {
    id: 10,
    title: 'TypeScript 高级编程',
    description: '深入学习TypeScript类型系统，掌握高级类型、泛型、装饰器等特性，提升代码质量与开发效率。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=TypeScript%20advanced%20course%20thumbnail%20TS%20logo%20blue%20modern&image_size=landscape_16_9',
    duration: '8小时30分',
    durationSeconds: 30600,
    publishDate: '2024-05-01',
    viewCount: 7650,
    likeCount: 2130,
    collectCount: 1780,
    categoryId: 1,
    categoryName: '前端开发',
    teacherId: 2,
    difficulty: '进阶',
    tags: ['TypeScript', '前端', '类型系统'],
    chapters: 8,
    rating: 4.8,
    ratingCount: 780
  },
  {
    id: 11,
    title: 'Node.js 后端开发实战',
    description: '从零搭建Node.js后端服务，学习Express/Koa框架、数据库操作、身份认证、性能优化等核心技能。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Nodejs%20backend%20development%20course%20thumbnail%20green%20server%20code&image_size=landscape_16_9',
    duration: '13小时40分',
    durationSeconds: 49200,
    publishDate: '2024-03-18',
    viewCount: 10560,
    likeCount: 2980,
    collectCount: 2450,
    categoryId: 2,
    categoryName: '后端开发',
    teacherId: 4,
    difficulty: '入门',
    tags: ['Node.js', '后端', 'Express'],
    chapters: 11,
    rating: 4.6,
    ratingCount: 1050
  },
  {
    id: 12,
    title: 'Redis 深度历险与实战',
    description: '深入理解Redis核心原理与应用场景，包括数据结构、持久化、集群、缓存设计与优化等。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Redis%20course%20thumbnail%20red%20cube%20database%20tech&image_size=landscape_16_9',
    duration: '10小时15分',
    durationSeconds: 36900,
    publishDate: '2024-04-15',
    viewCount: 8920,
    likeCount: 2560,
    collectCount: 2100,
    categoryId: 3,
    categoryName: '数据库',
    teacherId: 3,
    difficulty: '进阶',
    tags: ['Redis', '缓存', '数据库'],
    chapters: 9,
    rating: 4.7,
    ratingCount: 850
  },
  {
    id: 13,
    title: 'Git 版本控制与团队协作',
    description: '系统学习Git版本控制工具，掌握分支管理、冲突解决、代码审查等团队协作必备技能。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Git%20version%20control%20course%20thumbnail%20git%20branch%20tree%20orange&image_size=landscape_16_9',
    duration: '5小时40分',
    durationSeconds: 20400,
    publishDate: '2024-02-28',
    viewCount: 19850,
    likeCount: 5620,
    collectCount: 4890,
    categoryId: 6,
    categoryName: '运维与DevOps',
    teacherId: 4,
    difficulty: '入门',
    tags: ['Git', '版本控制', '团队协作'],
    chapters: 7,
    rating: 4.9,
    ratingCount: 2180
  },
  {
    id: 14,
    title: 'Linux 系统运维实战',
    description: '从零开始学习Linux系统管理，包括命令行操作、Shell脚本、服务配置、性能监控等。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Linux%20system%20admin%20course%20thumbnail%20terminal%20black%20green%20text&image_size=landscape_16_9',
    duration: '12小时25分',
    durationSeconds: 44700,
    publishDate: '2024-01-30',
    viewCount: 11280,
    likeCount: 3240,
    collectCount: 2780,
    categoryId: 6,
    categoryName: '运维与DevOps',
    teacherId: 4,
    difficulty: '入门',
    tags: ['Linux', '运维', 'Shell'],
    chapters: 10,
    rating: 4.7,
    ratingCount: 1080
  },
  {
    id: 15,
    title: '机器学习入门与Python实现',
    description: '通俗易懂讲解机器学习基础算法，使用Python从零实现，包括监督学习、无监督学习等核心内容。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=machine%20learning%20course%20thumbnail%20neural%20network%20brain%20purple&image_size=landscape_16_9',
    duration: '16小时50分',
    durationSeconds: 60600,
    publishDate: '2024-03-22',
    viewCount: 16780,
    likeCount: 4890,
    collectCount: 4120,
    categoryId: 7,
    categoryName: '人工智能',
    teacherId: 5,
    difficulty: '入门',
    tags: ['机器学习', 'Python', 'AI'],
    chapters: 13,
    rating: 4.8,
    ratingCount: 1560
  },
  {
    id: 16,
    title: '敏捷开发与Scrum实战',
    description: '深入理解敏捷开发理念，掌握Scrum框架实践，包括用户故事、冲刺、站会、回顾等核心实践。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=agile%20scrum%20project%20management%20course%20thumbnail%20sprint%20board%20colorful&image_size=landscape_16_9',
    duration: '6小时30分',
    durationSeconds: 23400,
    publishDate: '2024-04-08',
    viewCount: 5680,
    likeCount: 1560,
    collectCount: 1280,
    categoryId: 8,
    categoryName: '项目管理',
    teacherId: 1,
    difficulty: '入门',
    tags: ['敏捷', 'Scrum', '项目管理'],
    chapters: 6,
    rating: 4.5,
    ratingCount: 520
  },
  {
    id: 17,
    title: 'Vue3 + TypeScript 企业级项目',
    description: '基于Vue3和TypeScript从零搭建企业级中后台管理系统，涵盖组件设计、权限管理、性能优化等。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Vue3%20TypeScript%20enterprise%20project%20course%20thumbnail%20dashboard%20UI%20blue&image_size=landscape_16_9',
    duration: '24小时15分',
    durationSeconds: 87300,
    publishDate: '2024-05-10',
    viewCount: 13560,
    likeCount: 4120,
    collectCount: 3560,
    categoryId: 1,
    categoryName: '前端开发',
    teacherId: 2,
    difficulty: '高级',
    tags: ['Vue3', 'TypeScript', '企业级'],
    chapters: 20,
    rating: 4.9,
    ratingCount: 1420
  },
  {
    id: 18,
    title: 'Spring Boot 3 核心技术',
    description: '全面学习Spring Boot 3.x核心技术，包括自动配置、Starter、Actuator、安全认证等企业级开发技能。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Spring%20Boot%203%20course%20thumbnail%20green%20leaf%20spring%20logo&image_size=landscape_16_9',
    duration: '17小时40分',
    durationSeconds: 63600,
    publishDate: '2024-02-15',
    viewCount: 14890,
    likeCount: 4350,
    collectCount: 3780,
    categoryId: 2,
    categoryName: '后端开发',
    teacherId: 1,
    difficulty: '进阶',
    tags: ['Spring Boot', 'Java', '后端'],
    chapters: 14,
    rating: 4.8,
    ratingCount: 1580
  },
  {
    id: 19,
    title: 'MongoDB 数据库实战',
    description: '全面掌握MongoDB文档型数据库，包括数据建模、查询优化、索引、聚合管道、分片集群等。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=MongoDB%20database%20course%20thumbnail%20green%20leaf%20document%20database&image_size=landscape_16_9',
    duration: '9小时20分',
    durationSeconds: 33600,
    publishDate: '2024-04-25',
    viewCount: 6780,
    likeCount: 1890,
    collectCount: 1560,
    categoryId: 3,
    categoryName: '数据库',
    teacherId: 3,
    difficulty: '进阶',
    tags: ['MongoDB', 'NoSQL', '数据库'],
    chapters: 9,
    rating: 4.6,
    ratingCount: 650
  },
  {
    id: 20,
    title: '前端工程化与构建优化',
    description: '深入学习前端工程化实践，包括Webpack、Vite构建工具、CI/CD、代码规范、单元测试等。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=frontend%20engineering%20build%20optimization%20course%20thumbnail%20gears%20code%20blue&image_size=landscape_16_9',
    duration: '10小时50分',
    durationSeconds: 39000,
    publishDate: '2024-03-30',
    viewCount: 8560,
    likeCount: 2340,
    collectCount: 1980,
    categoryId: 1,
    categoryName: '前端开发',
    teacherId: 2,
    difficulty: '高级',
    tags: ['工程化', 'Webpack', 'Vite'],
    chapters: 10,
    rating: 4.7,
    ratingCount: 820
  },
  {
    id: 21,
    title: 'Go 语言编程入门到精通',
    description: '从零开始学习Go语言，掌握并发编程、网络编程、微服务开发等核心技能，成为Gopher。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Go%20language%20programming%20course%20thumbnail%20gopher%20mascot%20blue%20theme&image_size=landscape_16_9',
    duration: '18小时15分',
    durationSeconds: 65700,
    publishDate: '2024-05-05',
    viewCount: 12340,
    likeCount: 3670,
    collectCount: 3120,
    categoryId: 2,
    categoryName: '后端开发',
    teacherId: 4,
    difficulty: '入门',
    tags: ['Go', '后端', '并发'],
    chapters: 15,
    rating: 4.8,
    ratingCount: 1230
  },
  {
    id: 22,
    title: '网络安全与渗透测试基础',
    description: '系统学习网络安全基础知识，包括常见漏洞原理、渗透测试流程、安全防护措施等。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=cybersecurity%20ethical%20hacking%20course%20thumbnail%20shield%20lock%20dark%20green&image_size=landscape_16_9',
    duration: '11小时30分',
    durationSeconds: 41400,
    publishDate: '2024-04-12',
    viewCount: 9120,
    likeCount: 2670,
    collectCount: 2230,
    categoryId: 6,
    categoryName: '运维与DevOps',
    teacherId: 5,
    difficulty: '入门',
    tags: ['安全', '渗透测试', '网络安全'],
    chapters: 9,
    rating: 4.6,
    ratingCount: 890
  },
  {
    id: 23,
    title: 'Nginx 高性能Web服务器',
    description: '深入理解Nginx核心原理与配置，掌握反向代理、负载均衡、静态资源优化、安全加固等技能。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Nginx%20web%20server%20course%20thumbnail%20server%20logo%20green%20white&image_size=landscape_16_9',
    duration: '7小时45分',
    durationSeconds: 27900,
    publishDate: '2024-03-08',
    viewCount: 7890,
    likeCount: 2180,
    collectCount: 1850,
    categoryId: 6,
    categoryName: '运维与DevOps',
    teacherId: 3,
    difficulty: '进阶',
    tags: ['Nginx', 'Web服务器', '负载均衡'],
    chapters: 7,
    rating: 4.7,
    ratingCount: 720
  },
  {
    id: 24,
    title: '深度学习与神经网络',
    description: '从零开始学习深度学习基础知识，理解神经网络原理，掌握TensorFlow/PyTorch实战技能。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=deep%20learning%20neural%20network%20course%20thumbnail%20brain%20network%20purple%20blue&image_size=landscape_16_9',
    duration: '20小时40分',
    durationSeconds: 74400,
    publishDate: '2024-02-05',
    viewCount: 15680,
    likeCount: 4560,
    collectCount: 3890,
    categoryId: 7,
    categoryName: '人工智能',
    teacherId: 5,
    difficulty: '高级',
    tags: ['深度学习', '神经网络', 'AI'],
    chapters: 16,
    rating: 4.9,
    ratingCount: 1670
  },
  {
    id: 25,
    title: '软件架构设计模式',
    description: '系统学习软件架构设计原则与经典模式，掌握分层架构、事件驱动、CQRS、微服务等架构模式。',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=software%20architecture%20design%20patterns%20course%20thumbnail%20building%20blocks%20blueprint&image_size=landscape_16_9',
    duration: '14小时10分',
    durationSeconds: 51000,
    publishDate: '2024-04-30',
    viewCount: 10230,
    likeCount: 2980,
    collectCount: 2560,
    categoryId: 5,
    categoryName: '系统架构',
    teacherId: 1,
    difficulty: '高级',
    tags: ['架构', '设计模式', '系统设计'],
    chapters: 12,
    rating: 4.8,
    ratingCount: 980
  }
]

export const getTeachers = () => teachers
export const getCategories = () => categories
export const getCourseOutlines = () => courseOutlines
export const getVideos = () => videos

export const getTeacherById = (id) => teachers.find(t => t.id === id)

export const getVideoById = (id) => {
  const video = videos.find(v => v.id === id)
  if (!video) return null
  const teacher = getTeacherById(video.teacherId)
  const outlineIndex = (video.id - 1) % courseOutlines.length
  return {
    ...video,
    teacher,
    outline: courseOutlines[outlineIndex]
  }
}

export const getVideoList = (params = {}) => {
  const { current = 1, size = 12, keyword = '', categoryId = null, sort = 'latest' } = params
  
  let filtered = [...videos]
  
  if (keyword && keyword.trim()) {
    const kw = keyword.trim().toLowerCase()
    filtered = filtered.filter(v => 
      v.title.toLowerCase().includes(kw) ||
      v.description.toLowerCase().includes(kw) ||
      v.tags.some(t => t.toLowerCase().includes(kw))
    )
  }
  
  if (categoryId) {
    filtered = filtered.filter(v => v.categoryId === categoryId)
  }
  
  if (sort === 'hot') {
    filtered.sort((a, b) => b.viewCount - a.viewCount)
  } else if (sort === 'rating') {
    filtered.sort((a, b) => b.rating - a.rating)
  } else {
    filtered.sort((a, b) => new Date(b.publishDate) - new Date(a.publishDate))
  }
  
  const total = filtered.length
  const start = (current - 1) * size
  const records = filtered.slice(start, start + size)
  
  return {
    records,
    total,
    current,
    size,
    pages: Math.ceil(total / size)
  }
}

export const getRelatedVideos = (videoId, limit = 6) => {
  const video = videos.find(v => v.id === videoId)
  if (!video) return []
  
  const related = videos
    .filter(v => v.id !== videoId && (v.categoryId === video.categoryId || v.tags.some(t => video.tags.includes(t))))
    .sort((a, b) => b.viewCount - a.viewCount)
    .slice(0, limit)
  
  return related
}

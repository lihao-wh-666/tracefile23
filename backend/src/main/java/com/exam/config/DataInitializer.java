package com.exam.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.entity.*;
import com.exam.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private PaperQuestionMapper paperQuestionMapper;

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private ExamAnswerMapper examAnswerMapper;

    private final Random random = new Random();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        initAdminUser();
        initSubjects();
        initTeacherUsers();
        initStudentUsers();
        initQuestions();
        initPapers();
        initExams();
        initExamRecordsAndAnswers();
    }

    private void initAdminUser() {
        User admin = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"));
        if (admin == null) {
            User user = new User();
            user.setUsername("admin");
            user.setPassword(encoder.encode("admin123"));
            user.setRealName("系统管理员");
            user.setRole(1);
            user.setEmail("admin@example.com");
            user.setPhone("13800138000");
            userMapper.insert(user);
        }
    }

    private void initSubjects() {
        String[] subjectNames = {
                "高等数学", "大学英语", "线性代数", "概率论与数理统计",
                "计算机基础", "C语言程序设计", "数据结构", "Java程序设计"
        };
        String[] subjectDescriptions = {
                "大学高等数学课程，涵盖微积分、极限、导数、积分等核心内容",
                "大学英语四级考试相关课程，包含听说读写综合训练",
                "线性代数课程，涵盖矩阵、行列式、向量空间、线性变换等内容",
                "概率论与数理统计课程，包含随机事件、随机变量、统计推断等",
                "计算机基础知识入门，涵盖计算机组成、操作系统、网络基础等",
                "C语言程序设计基础课程，学习结构化编程和算法实现",
                "数据结构与算法课程，涵盖链表、栈、队列、树、图等数据结构",
                "Java面向对象程序设计，学习Java语法、集合框架、多线程等"
        };

        for (int i = 0; i < subjectNames.length; i++) {
            Long count = subjectMapper.selectCount(
                    new LambdaQueryWrapper<Subject>().eq(Subject::getName, subjectNames[i]));
            if (count == null || count == 0) {
                Subject subject = new Subject();
                subject.setName(subjectNames[i]);
                subject.setDescription(subjectDescriptions[i]);
                subjectMapper.insert(subject);
            }
        }
    }

    private void initTeacherUsers() {
        Long teacherCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, 2));
        if (teacherCount != null && teacherCount > 0) return;

        String[] teacherNames = {"张老师", "李老师", "王老师", "刘老师", "陈老师", "杨老师", "赵老师", "黄老师"};
        for (int i = 0; i < teacherNames.length; i++) {
            User user = new User();
            user.setUsername("teacher" + (i + 1));
            user.setPassword(encoder.encode("teacher123"));
            user.setRealName(teacherNames[i]);
            user.setRole(2);
            user.setEmail("teacher" + (i + 1) + "@example.com");
            user.setPhone("1390013800" + String.format("%02d", i));
            userMapper.insert(user);
        }
    }

    private void initStudentUsers() {
        Long studentCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, 3));
        if (studentCount != null && studentCount > 20) return;

        String[] studentNames = {
                "小明", "小红", "小刚", "小丽", "小强", "小芳", "小军", "小燕",
                "小亮", "小梅", "小华", "小娟", "小伟", "小敏", "小磊", "小娜",
                "小鹏", "小婷", "小飞", "小霞", "小龙", "小倩", "小虎", "小英",
                "小杰", "小琳", "小涛", "小晶", "小超", "小莹"
        };

        for (int i = 0; i < studentNames.length; i++) {
            User user = new User();
            user.setUsername("student" + (i + 1));
            user.setPassword(encoder.encode("student123"));
            user.setRealName(studentNames[i]);
            user.setRole(3);
            user.setEmail("student" + (i + 1) + "@example.com");
            user.setPhone("1370013800" + String.format("%02d", i));
            userMapper.insert(user);
        }
    }

    private void initQuestions() {
        Long questionCount = questionMapper.selectCount(null);
        if (questionCount != null && questionCount > 100) return;

        List<Subject> subjects = subjectMapper.selectList(null);
        Long teacherId = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "teacher1")).getId();

        Map<String, Object[]> questionBank = buildQuestionBank();

        for (Subject subject : subjects) {
            Object[] subjectQuestions = questionBank.get(subject.getName());
            if (subjectQuestions == null) continue;

            for (Object qObj : subjectQuestions) {
                Question q = (Question) qObj;
                Question question = new Question();
                question.setSubjectId(subject.getId());
                question.setType(q.getType());
                question.setContent(q.getContent());
                question.setOptionA(q.getOptionA());
                question.setOptionB(q.getOptionB());
                question.setOptionC(q.getOptionC());
                question.setOptionD(q.getOptionD());
                question.setAnswer(q.getAnswer());
                question.setAnalysis(q.getAnalysis());
                question.setScore(q.getScore());
                question.setDifficulty(q.getDifficulty());
                question.setCreateBy(teacherId);
                questionMapper.insert(question);
            }
        }
    }

    private Map<String, Object[]> buildQuestionBank() {
        Map<String, Object[]> bank = new HashMap<>();

        bank.put("高等数学", new Question[]{
                createQuestion(1, "函数f(x) = x²在x = 0处的导数是？", "A. 0", "B. 1", "C. 2", "D. 不存在", "A", "根据导数定义，f'(0) = lim(h→0) (h² - 0)/h = 0", 5, 1),
                createQuestion(1, "∫x dx = ?", "A. x²", "B. x²/2 + C", "C. 2x + C", "D. x² + C", "B", "基本积分公式：∫x^n dx = x^(n+1)/(n+1) + C", 5, 1),
                createQuestion(1, "lim(x→0) sinx/x = ?", "A. 0", "B. 1", "C. ∞", "D. 不存在", "B", "这是重要极限之一，结果为1", 5, 2),
                createQuestion(1, "函数y = e^x的导数是？", "A. e^x", "B. xe^(x-1)", "C. e^(x-1)", "D. ln x", "A", "指数函数e^x的导数等于自身", 5, 1),
                createQuestion(1, "定积分∫[0,1] x dx = ?", "A. 0", "B. 1/2", "C. 1", "D. 2", "B", "∫[0,1] x dx = [x²/2][0,1] = 1/2", 10, 2),
                createQuestion(1, "曲线y = x³在点(1,1)处的切线斜率是？", "A. 1", "B. 2", "C. 3", "D. 4", "C", "y' = 3x²，当x=1时，y' = 3", 10, 2),
                createQuestion(2, "以下哪些是基本初等函数？", "A. 幂函数", "B. 指数函数", "C. 对数函数", "D. 三角函数", "ABCD", "基本初等函数包括：幂函数、指数函数、对数函数、三角函数、反三角函数、常数函数", 10, 2),
                createQuestion(2, "以下关于连续函数的说法正确的有？", "A. 初等函数在其定义域内连续", "B. 连续函数的和差积商仍连续", "C. 闭区间上的连续函数必有最大值和最小值", "D. 连续函数必可导", "ABC", "连续函数不一定可导，如f(x) = |x|在x=0处连续但不可导", 10, 3),
                createQuestion(3, "函数f(x) = |x|在x = 0处可导。", "", "", "", "", "错", "f(x) = |x|在x=0处左右导数不相等，故不可导", 5, 2),
                createQuestion(3, "如果函数在某点可导，则在该点必连续。", "", "", "", "", "对", "可导必连续，但连续不一定可导", 5, 1),
                createQuestion(1, "二阶导数大于0的点是函数的？", "A. 极大值点", "B. 极小值点", "C. 拐点", "D. 零点", "B", "二阶导数大于0说明函数在该点凹向，可能是极小值点", 10, 3),
                createQuestion(1, "∫(1/x) dx = ?", "A. x", "B. ln|x| + C", "C. -1/x² + C", "D. x ln x - x + C", "B", "这是基本积分公式，注意加绝对值和常数C", 5, 1)
        });

        bank.put("大学英语", new Question[]{
                createQuestion(1, "Choose the correct form: He ___ to school every day.", "A. go", "B. goes", "C. going", "D. went", "B", "第三人称单数主语He，一般现在时动词用goes", 5, 1),
                createQuestion(1, "What is the past tense of 'eat'?", "A. eated", "B. ate", "C. eaten", "D. eating", "B", "eat的过去式是ate，过去分词是eaten", 5, 1),
                createQuestion(1, "The book ___ on the table.", "A. is", "B. are", "C. am", "D. be", "A", "book是单数名词，用is", 5, 1),
                createQuestion(1, "She is ___ than her sister.", "A. tall", "B. taller", "C. tallest", "D. more tall", "B", "两者比较用比较级taller", 5, 2),
                createQuestion(1, "I have ___ finished my homework.", "A. yet", "B. already", "C. still", "D. ever", "B", "肯定句中表示'已经'用already", 10, 2),
                createQuestion(2, "Which of the following are correct sentences?", "A. She speaks English fluently.", "B. He don't like coffee.", "C. They are playing football.", "D. I am agree with you.", "AC", "B选项应为doesn't，D选项应去掉am", 10, 2),
                createQuestion(2, "Which words are adjectives?", "A. beautiful", "B. quickly", "C. happy", "D. run", "AC", "beautiful和happy是形容词，quickly是副词，run是动词", 10, 2),
                createQuestion(3, "'I have been to Beijing' is in present perfect tense.", "", "", "", "", "对", "have/has + 过去分词是现在完成时结构", 5, 1),
                createQuestion(3, "'News' is a plural noun.", "", "", "", "", "错", "news是不可数名词，作单数用", 5, 2),
                createQuestion(1, "The movie was so ___ that I fell asleep.", "A. bored", "B. boring", "C. bores", "D. bore", "B", "修饰物用boring，修饰人用bored", 10, 2),
                createQuestion(1, "He asked me ___ I had seen the film.", "A. that", "B. if", "C. what", "D. which", "B", "if引导宾语从句，表示'是否'", 10, 3),
                createQuestion(4, "The opposite of 'hot' is ___.", "", "", "", "", "cold", "hot的反义词是cold", 5, 1)
        });

        bank.put("线性代数", new Question[]{
                createQuestion(1, "2×2单位矩阵的行列式值是？", "A. 0", "B. 1", "C. 2", "D. 4", "B", "单位矩阵的行列式等于1", 5, 1),
                createQuestion(1, "矩阵A的秩为r，则A的所有r+1阶子式？", "A. 全为0", "B. 不全为0", "C. 至少有一个不为0", "D. 都不为0", "A", "矩阵秩的定义：最高阶非零子式的阶数", 10, 2),
                createQuestion(1, "两个矩阵乘积AB = 0，则？", "A. A = 0或B = 0", "B. A ≠ 0且B ≠ 0", "C. A和B至少有一个不可逆", "D. 以上都不对", "D", "矩阵乘法不满足消去律，AB=0不能推出A=0或B=0", 10, 3),
                createQuestion(1, "可逆矩阵的行列式值？", "A. 等于0", "B. 不等于0", "C. 大于0", "D. 小于0", "B", "矩阵可逆的充要条件是行列式不为0", 5, 2),
                createQuestion(2, "以下关于矩阵的说法正确的有？", "A. 对称矩阵的转置等于自身", "B. 对角矩阵一定是对称矩阵", "C. 上三角矩阵的乘积仍是上三角矩阵", "D. 矩阵乘法满足交换律", "ABC", "矩阵乘法一般不满足交换律", 10, 2),
                createQuestion(2, "齐次线性方程组Ax = 0有非零解的条件是？", "A. |A| = 0", "B. r(A) < n", "C. A的列向量线性相关", "D. A的行向量线性相关", "ABC", "D不是必要条件", 10, 3),
                createQuestion(3, "行列式的两行交换，行列式值不变。", "", "", "", "", "错", "行列式两行交换，行列式变号", 5, 1),
                createQuestion(3, "如果矩阵A可逆，则A的转置也可逆。", "", "", "", "", "对", "(A^T)^(-1) = (A^(-1))^T", 5, 2),
                createQuestion(1, "特征值之和等于矩阵的？", "A. 行列式", "B. 秩", "C. 迹", "D. 行数", "C", "矩阵的迹等于对角线元素之和，也等于所有特征值之和", 10, 3),
                createQuestion(1, "n阶方阵有n个不同的特征值是可对角化的？", "A. 充分必要条件", "B. 充分但不必要条件", "C. 必要但不充分条件", "D. 既不充分也不必要条件", "B", "有n个不同特征值一定可对角化，但可对角化不一定有n个不同特征值", 10, 3)
        });

        bank.put("概率论与数理统计", new Question[]{
                createQuestion(1, "抛一枚均匀硬币，正面朝上的概率是？", "A. 0", "B. 1/4", "C. 1/2", "D. 1", "C", "均匀硬币两面等可能，P(正面) = 1/2", 5, 1),
                createQuestion(1, "设A、B互斥，则P(A∪B) = ?", "A. P(A) + P(B)", "B. P(A)P(B)", "C. P(A) + P(B) - P(AB)", "D. P(A|B)P(B)", "A", "互斥事件P(AB)=0，故P(A∪B) = P(A) + P(B)", 5, 2),
                createQuestion(1, "正态分布的两个参数是？", "A. 均值和方差", "B. 中位数和标准差", "C. 期望和概率", "D. 方差和频率", "A", "正态分布N(μ, σ²)的参数是均值μ和方差σ²", 5, 1),
                createQuestion(1, "期望E(X) = 3，则E(2X + 1) = ?", "A. 6", "B. 7", "C. 4", "D. 5", "B", "E(2X + 1) = 2E(X) + 1 = 2×3 + 1 = 7", 10, 2),
                createQuestion(2, "以下哪些是概率的基本性质？", "A. 0 ≤ P(A) ≤ 1", "B. P(Ω) = 1", "C. P(A∪B) = P(A) + P(B)", "D. P(∅) = 0", "ABD", "C只在互斥时成立，不是基本性质", 10, 2),
                createQuestion(2, "关于方差，正确的说法有？", "A. 方差非负", "B. Var(c) = 0（c为常数）", "C. Var(aX) = aVar(X)", "D. Var(X + Y) = Var(X) + Var(Y)（X,Y独立）", "ABD", "Var(aX) = a²Var(X)", 10, 2),
                createQuestion(3, "如果P(A) = 0，则A是不可能事件。", "", "", "", "", "错", "概率为0的事件不一定是不可能事件，如连续型随机变量取某特定值", 5, 3),
                createQuestion(3, "独立事件一定互斥。", "", "", "", "", "错", "独立事件和互斥事件是不同概念，独立一般不互斥", 5, 2),
                createQuestion(1, "设X~B(n,p)，则E(X) = ?", "A. np", "B. np(1-p)", "C. n(1-p)", "D. p", "A", "二项分布的期望是np", 5, 2),
                createQuestion(1, "设P(A) = 0.6, P(B) = 0.5, P(AB) = 0.3，则P(A|B) = ?", "A. 0.3", "B. 0.5", "C. 0.6", "D. 0.8", "C", "P(A|B) = P(AB)/P(B) = 0.3/0.5 = 0.6", 10, 3)
        });

        bank.put("计算机基础", new Question[]{
                createQuestion(1, "计算机的CPU主要由什么组成？", "A. 内存和外存", "B. 运算器和控制器", "C. 硬盘和内存", "D. 输入设备和输出设备", "B", "CPU由运算器和控制器组成", 5, 1),
                createQuestion(1, "1GB等于多少MB？", "A. 100", "B. 512", "C. 1000", "D. 1024", "D", "1GB = 1024MB = 2^10 MB", 5, 1),
                createQuestion(1, "以下哪个是输入设备？", "A. 打印机", "B. 显示器", "C. 键盘", "D. 音箱", "C", "键盘是输入设备，其他都是输出设备", 5, 1),
                createQuestion(1, "操作系统的主要功能是？", "A. 编译高级语言程序", "B. 管理计算机的硬件和软件资源", "C. 接入互联网", "D. 编辑文档", "B", "操作系统是管理计算机硬件与软件资源的系统软件", 5, 2),
                createQuestion(2, "以下属于计算机存储设备的有？", "A. RAM", "B. ROM", "C. 硬盘", "D. CPU", "ABC", "CPU是中央处理器，不是存储设备", 10, 1),
                createQuestion(2, "关于计算机病毒，正确的说法有？", "A. 是一种特殊的计算机程序", "B. 可以自我复制", "C. 只会破坏软件，不会破坏硬件", "D. 可以通过网络传播", "ABD", "某些病毒也可能破坏硬件，如CIH病毒", 10, 2),
                createQuestion(3, "ROM是随机存取存储器。", "", "", "", "", "错", "ROM是只读存储器，RAM才是随机存取存储器", 5, 1),
                createQuestion(3, "计算机的机器语言是用二进制代码表示的。", "", "", "", "", "对", "机器语言是计算机能直接识别和执行的二进制代码", 5, 1),
                createQuestion(1, "十进制数15转换为二进制是？", "A. 111", "B. 1111", "C. 1110", "D. 1011", "B", "15 = 8 + 4 + 2 + 1 = 1111(2)", 10, 2),
                createQuestion(1, "HTTP协议默认使用的端口号是？", "A. 21", "B. 23", "C. 80", "D. 443", "C", "HTTP默认端口80，HTTPS默认443，FTP默认21", 5, 2)
        });

        bank.put("C语言程序设计", new Question[]{
                createQuestion(1, "C语言中，以下哪个是正确的主函数声明？", "A. main()", "B. void main()", "C. int main()", "D. function main()", "C", "标准C语言主函数返回值为int类型", 5, 1),
                createQuestion(1, "int a[5] = {1, 2, 3}; 则a[2]的值是？", "A. 0", "B. 1", "C. 2", "D. 3", "D", "数组下标从0开始，a[2]是第三个元素，值为3", 5, 2),
                createQuestion(1, "以下哪个运算符优先级最高？", "A. +", "B. *", "C. =", "D. &&", "B", "乘法优先级高于加法、逻辑与和赋值", 10, 2),
                createQuestion(1, "int *p; 中p是？", "A. 整型变量", "B. 指针变量", "C. 数组变量", "D. 函数变量", "B", "*表示这是一个指针变量，指向int类型", 5, 2),
                createQuestion(2, "以下哪些是C语言的基本数据类型？", "A. int", "B. float", "C. char", "D. string", "ABC", "C语言没有string基本类型，字符串用char数组表示", 10, 1),
                createQuestion(2, "关于C语言指针，正确的说法有？", "A. 指针可以进行加减运算", "B. 两个指针可以相减", "C. 两个指针可以相加", "D. 指针可以比较大小", "ABD", "两个指针相加没有意义，是不允许的", 10, 3),
                createQuestion(3, "C语言中，数组名是一个常量指针。", "", "", "", "", "对", "数组名代表数组首元素地址，是常量，不能被修改", 5, 2),
                createQuestion(3, "break语句只能用于循环中。", "", "", "", "", "错", "break语句也可以用于switch语句中", 5, 2),
                createQuestion(1, "for(i = 0; i < 10; i++) 循环会执行多少次？", "A. 9", "B. 10", "C. 11", "D. 无限次", "B", "i从0到9，共执行10次", 5, 1),
                createQuestion(1, "表达式3 + 2 * 4 - 1的值是？", "A. 19", "B. 10", "C. 11", "D. 12", "B", "先乘后加减：3 + 8 - 1 = 10", 10, 1)
        });

        bank.put("数据结构", new Question[]{
                createQuestion(1, "栈的特点是？", "A. 先进先出", "B. 后进先出", "C. 随机存取", "D. 顺序存取", "B", "栈是后进先出(LIFO)的线性表", 5, 1),
                createQuestion(1, "队列的特点是？", "A. 先进先出", "B. 后进先出", "C. 随机存取", "D. 顺序存取", "A", "队列是先进先出(FIFO)的线性表", 5, 1),
                createQuestion(1, "在一个具有n个结点的二叉树中，所有结点的度数之和为？", "A. n", "B. n-1", "C. n+1", "D. 2n-1", "B", "除根结点外，每个结点都有一条边与其父结点相连，共n-1条边，即n-1个度", 10, 3),
                createQuestion(1, "快速排序在最坏情况下的时间复杂度是？", "A. O(n)", "B. O(nlogn)", "C. O(n²)", "D. O(logn)", "C", "最坏情况（如已排序）下，快速排序退化为冒泡排序，时间复杂度O(n²)", 10, 3),
                createQuestion(2, "以下哪些是线性结构？", "A. 数组", "B. 链表", "C. 树", "D. 图", "AB", "树和图是非线性结构", 10, 1),
                createQuestion(2, "关于二叉树，正确的说法有？", "A. 满二叉树一定是完全二叉树", "B. 完全二叉树一定是满二叉树", "C. 二叉树的第i层最多有2^(i-1)个结点", "D. 深度为k的二叉树最多有2^k - 1个结点", "ACD", "完全二叉树不一定是满二叉树", 10, 2),
                createQuestion(3, "线性表的顺序存储结构中，插入和删除操作的时间复杂度都是O(n)。", "", "", "", "", "对", "需要移动大量元素，平均移动n/2个元素", 5, 2),
                createQuestion(3, "二分查找只能用于有序的顺序表。", "", "", "", "", "对", "二分查找要求元素有序且能随机访问，链表不能随机访问", 5, 2),
                createQuestion(1, "有向图的拓扑排序可以判断图中是否存在？", "A. 环", "B. 回路", "C. 环或回路", "D. 路径", "C", "拓扑排序可以检测有向图中是否存在环（回路）", 10, 3),
                createQuestion(1, "哈希表查找的平均时间复杂度是？", "A. O(1)", "B. O(logn)", "C. O(n)", "D. O(n²)", "A", "哈希表通过哈希函数直接定位，理想情况下时间复杂度O(1)", 10, 2)
        });

        bank.put("Java程序设计", new Question[]{
                createQuestion(1, "Java中，以下哪个是正确的类声明？", "A. class MyClass {}", "B. Class MyClass {}", "C. myClass {}", "D. public class {}", "A", "Java中class关键字小写，类名首字母大写，必须有类名", 5, 1),
                createQuestion(1, "String str = null; str.length(); 会抛出什么异常？", "A. IOException", "B. NullPointerException", "C. ArrayIndexOutOfBoundsException", "D. ClassCastException", "B", "调用null对象的方法会抛出NullPointerException", 5, 2),
                createQuestion(1, "Java中，以下哪个访问修饰符的访问范围最大？", "A. private", "B. protected", "C. default", "D. public", "D", "访问范围：public > protected > default > private", 5, 1),
                createQuestion(1, "ArrayList的底层实现是？", "A. 数组", "B. 链表", "C. 哈希表", "D. 二叉树", "A", "ArrayList底层是动态数组，LinkedList底层是链表", 5, 2),
                createQuestion(2, "以下哪些是Java的关键字？", "A. int", "B. String", "C. static", "D. new", "ACD", "String是Java的类名，不是关键字", 10, 2),
                createQuestion(2, "关于Java异常，正确的说法有？", "A. RuntimeException是非受检异常", "B. Exception是所有异常的父类", "C. finally块中的代码一定会执行", "D. catch块可以有多个", "ABD", "特殊情况下finally也可能不执行（如JVM退出）", 10, 3),
                createQuestion(3, "Java中，子类可以继承父类的私有成员。", "", "", "", "", "错", "私有成员只能在本类中访问，子类不能继承和访问", 5, 2),
                createQuestion(3, "Java的垃圾回收器可以自动回收不再使用的内存。", "", "", "", "", "对", "JVM的垃圾回收机制自动管理内存，回收无用对象", 5, 1),
                createQuestion(1, "实现多线程的方式有？", "A. 继承Thread类", "B. 实现Runnable接口", "C. 以上都是", "D. 以上都不是", "C", "Java实现多线程有两种基本方式：继承Thread或实现Runnable", 10, 2),
                createQuestion(1, "int a = 10; int b = 20; System.out.println(a + b + \"test\"); 输出是？", "A. 30test", "B. 1020test", "C. test30", "D. 编译错误", "A", "从左到右运算：10+20=30，30+\"test\"=\"30test\"", 10, 2)
        });

        return bank;
    }

    private Question createQuestion(int type, String content, String optionA, String optionB,
                                    String optionC, String optionD, String answer,
                                    String analysis, int score, int difficulty) {
        Question q = new Question();
        q.setType(type);
        q.setContent(content);
        q.setOptionA(optionA);
        q.setOptionB(optionB);
        q.setOptionC(optionC);
        q.setOptionD(optionD);
        q.setAnswer(answer);
        q.setAnalysis(analysis);
        q.setScore(score);
        q.setDifficulty(difficulty);
        return q;
    }

    private void initPapers() {
        Long paperCount = paperMapper.selectCount(null);
        if (paperCount != null && paperCount > 0) return;

        List<Subject> subjects = subjectMapper.selectList(null);
        Long teacherId = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "teacher1")).getId();

        String[] paperNames = {
                "期中考试", "期末考试", "单元测试", "模拟考试"
        };

        for (Subject subject : subjects) {
            List<Question> questions = questionMapper.selectList(
                    new LambdaQueryWrapper<Question>().eq(Question::getSubjectId, subject.getId()));
            if (questions.size() < 5) continue;

            for (String paperName : paperNames) {
                Paper paper = new Paper();
                paper.setName(subject.getName() + " - " + paperName);
                paper.setSubjectId(subject.getId());
                paper.setDuration(60);
                paper.setStatus(1);
                paper.setCreateBy(teacherId);

                List<Question> paperQuestions = questions.size() <= 10
                        ? questions
                        : questions.subList(0, 10);

                int totalScore = paperQuestions.stream().mapToInt(Question::getScore).sum();
                paper.setTotalScore(totalScore);
                paper.setPassScore(totalScore * 60 / 100);
                paperMapper.insert(paper);

                int sort = 1;
                for (Question q : paperQuestions) {
                    PaperQuestion pq = new PaperQuestion();
                    pq.setPaperId(paper.getId());
                    pq.setQuestionId(q.getId());
                    pq.setSort(sort++);
                    paperQuestionMapper.insert(pq);
                }
            }
        }
    }

    private void initExams() {
        Long examCount = examMapper.selectCount(null);
        if (examCount != null && examCount > 0) return;

        List<Paper> papers = paperMapper.selectList(
                new LambdaQueryWrapper<Paper>().eq(Paper::getStatus, 1));
        Long teacherId = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "teacher1")).getId();

        for (Paper paper : papers) {
            Exam exam = new Exam();
            exam.setName(paper.getName().replace(" - ", "") + "考试");
            exam.setPaperId(paper.getId());
            exam.setStartTime(LocalDateTime.now().plusDays(-7));
            exam.setEndTime(LocalDateTime.now().plusDays(7));
            exam.setStatus(1);
            exam.setCreateBy(teacherId);
            examMapper.insert(exam);
        }
    }

    private void initExamRecordsAndAnswers() {
        Long recordCount = examRecordMapper.selectCount(null);
        if (recordCount != null && recordCount > 50) return;

        List<User> students = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getRole, 3));
        List<Exam> exams = examMapper.selectList(null);

        for (Exam exam : exams) {
            Paper paper = paperMapper.selectById(exam.getPaperId());
            List<PaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                    new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, paper.getId()));
            List<Long> questionIds = paperQuestions.stream()
                    .map(PaperQuestion::getQuestionId)
                    .sorted(Comparator.comparingLong(q -> paperQuestions.stream()
                            .filter(pq -> pq.getQuestionId().equals(q))
                            .map(PaperQuestion::getSort)
                            .findFirst().orElse(0)))
                    .collect(Collectors.toList());

            Map<Long, Question> questionMap = questionMapper.selectBatchIds(questionIds).stream()
                    .collect(Collectors.toMap(Question::getId, q -> q));

            for (User student : students) {
                if (random.nextInt(10) < 2) continue;

                ExamRecord record = new ExamRecord();
                record.setExamId(exam.getId());
                record.setUserId(student.getId());
                record.setPaperId(paper.getId());
                record.setStartTime(exam.getStartTime().plusHours(1));
                record.setSubmitTime(exam.getStartTime().plusHours(2));
                record.setStatus(2);
                record.setDuration(3600);
                record.setQuestionOrder(String.join(",", questionIds.stream().map(String::valueOf).collect(Collectors.toList())));

                int totalScore = 0;
                List<ExamAnswer> answers = new ArrayList<>();

                for (Long questionId : questionIds) {
                    Question q = questionMap.get(questionId);
                    ExamAnswer answer = new ExamAnswer();
                    answer.setQuestionId(questionId);
                    answer.setOptionOrder("ABCD");

                    String studentAnswer;
                    int isCorrect;
                    int score;

                    int performance = random.nextInt(100);
                    if (performance < 60) {
                        studentAnswer = generateWrongAnswer(q);
                        isCorrect = 0;
                        score = 0;
                    } else if (performance < 85) {
                        studentAnswer = q.getAnswer();
                        isCorrect = 1;
                        score = q.getScore();
                        totalScore += q.getScore();
                    } else {
                        if (q.getType() == 2 && random.nextBoolean()) {
                            studentAnswer = generatePartialCorrectAnswer(q);
                            isCorrect = 2;
                            score = q.getScore() / 2;
                            totalScore += score;
                        } else {
                            studentAnswer = q.getAnswer();
                            isCorrect = 1;
                            score = q.getScore();
                            totalScore += q.getScore();
                        }
                    }

                    answer.setAnswer(studentAnswer);
                    answer.setIsCorrect(isCorrect);
                    answer.setScore(score);
                    answer.setAutoScore(score);
                    answers.add(answer);
                }

                record.setScore(totalScore);
                examRecordMapper.insert(record);

                for (ExamAnswer answer : answers) {
                    answer.setRecordId(record.getId());
                    examAnswerMapper.insert(answer);
                }
            }
        }
    }

    private String generateWrongAnswer(Question q) {
        if (q.getType() == 1) {
            String[] options = {"A", "B", "C", "D"};
            String correct = q.getAnswer();
            List<String> wrongOptions = Arrays.stream(options)
                    .filter(o -> !o.equals(correct))
                    .collect(Collectors.toList());
            return wrongOptions.get(random.nextInt(wrongOptions.size()));
        } else if (q.getType() == 2) {
            String correct = q.getAnswer();
            String allOptions = "ABCD";
            StringBuilder sb = new StringBuilder();
            for (char c : allOptions.toCharArray()) {
                if (random.nextBoolean() && correct.indexOf(c) == -1) {
                    sb.append(c);
                }
            }
            if (sb.length() == 0) sb.append('A');
            char[] chars = sb.toString().toCharArray();
            Arrays.sort(chars);
            return new String(chars);
        } else if (q.getType() == 3) {
            return q.getAnswer().equals("对") ? "错" : "对";
        } else if (q.getType() == 4) {
            return "错误答案" + random.nextInt(100);
        } else {
            return "这是一个错误的回答。";
        }
    }

    private String generatePartialCorrectAnswer(Question q) {
        String correct = q.getAnswer();
        StringBuilder sb = new StringBuilder();
        for (char c : correct.toCharArray()) {
            if (random.nextBoolean()) {
                sb.append(c);
            }
        }
        if (sb.length() == 0) sb.append(correct.charAt(0));
        char[] chars = sb.toString().toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }
}

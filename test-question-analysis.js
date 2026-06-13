const axios = require('axios')

const BASE_URL = 'http://localhost:6080'
let token = ''

async function login() {
  try {
    const res = await axios.post(`${BASE_URL}/api/auth/login`, {
      username: 'teacher1',
      password: 'teacher123'
    })
    if (res.data.code === 200) {
      token = res.data.data.token
      console.log('登录成功！Token已获取')
      return true
    }
    return false
  } catch (err) {
    console.log('登录失败:', err.response?.data?.msg || err.message)
    console.log('请确保后端服务已启动，端口为6080')
    return false
  }
}

async function testGenerateReport() {
  if (!token) {
    console.log('请先登录获取Token')
    return
  }

  console.log('\n=== 测试生成题目分析报告 ===')
  try {
    const res = await axios.post(
      `${BASE_URL}/api/question-analysis/report`,
      {
        subjectId: 1,
        wrongRateThreshold: 0.6,
        topN: 10
      },
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    )

    if (res.data.code === 200) {
      const data = res.data.data
      console.log('\n✅ 分析报告生成成功！')
      console.log('\n📊 分析概览:')
      console.log(`  科目: ${data.subjectName || '全部'}`)
      console.log(`  试卷: ${data.paperName || '全部'}`)
      console.log(`  题目总数: ${data.totalQuestionCount}`)
      console.log(`  总答题次数: ${data.totalAnswerCount}`)
      console.log(`  参与学生数: ${data.totalUserCount}`)
      console.log(`  整体正确率: ${(data.overallCorrectRate * 100).toFixed(1)}%`)
      console.log(`  平均得分: ${data.overallAverageScore}`)
      console.log(`  高频错题数: ${data.highFrequencyWrongQuestions.length}`)

      console.log('\n📈 逐题分析 (前5题):')
      data.questionAnalysisList.slice(0, 5).forEach((q, i) => {
        console.log(`  ${i + 1}. 题目ID: ${q.questionId}`)
        console.log(`     题型: ${q.typeName}, 难度: ${q.difficultyName}`)
        console.log(`     正确率: ${(q.correctRate * 100).toFixed(1)}%, 错误率: ${(q.wrongRate * 100).toFixed(1)}%`)
        console.log(`     平均得分: ${q.averageScore}/${q.score}`)
        console.log(`     答题次数: ${q.totalAnswerCount} (正确: ${q.correctCount}, 错误: ${q.wrongCount})`)
      })

      if (data.highFrequencyWrongQuestions.length > 0) {
        console.log('\n⚠️  高频错题 (前3题):')
        data.highFrequencyWrongQuestions.slice(0, 3).forEach((q, i) => {
          console.log(`  排名${q.wrongRank}: 错误率 ${(q.wrongRate * 100).toFixed(1)}%`)
          console.log(`     题目: ${q.content.substring(0, 50)}...`)
          console.log(`     错误原因: ${q.wrongReason}`)
        })
      }

      console.log('\n💡 优化建议:')
      data.optimizationSuggestions.forEach((s, i) => {
        console.log(`  ${i + 1}. ${s.substring(0, 80)}${s.length > 80 ? '...' : ''}`)
      })

      console.log('\n📊 按题型分类分析:')
      Object.keys(data.analysisByType).forEach(type => {
        const questions = data.analysisByType[type]
        const avgRate = questions.reduce((sum, q) => sum + q.correctRate, 0) / questions.length
        console.log(`  ${type}: ${questions.length}题, 平均正确率 ${(avgRate * 100).toFixed(1)}%`)
      })

      console.log('\n📊 按难度分类分析:')
      Object.keys(data.analysisByDifficulty).forEach(diff => {
        const questions = data.analysisByDifficulty[diff]
        const avgRate = questions.reduce((sum, q) => sum + q.correctRate, 0) / questions.length
        console.log(`  ${diff}: ${questions.length}题, 平均正确率 ${(avgRate * 100).toFixed(1)}%`)
      })

      console.log('\n✅ 测试完成！数据分析功能正常工作！')
    } else {
      console.log('❌ API调用失败:', res.data.msg)
    }
  } catch (err) {
    console.log('❌ 测试失败:', err.response?.data?.msg || err.message)
    if (err.response?.status === 401) {
      console.log('请确保登录凭证有效')
    }
  }
}

async function main() {
  console.log('========================================')
  console.log('  题目数据分析功能测试脚本')
  console.log('========================================')
  console.log('\n注意: 请确保后端服务已启动在端口6080')
  console.log('默认账号: teacher1 / teacher123\n')

  const loggedIn = await login()
  if (loggedIn) {
    await testGenerateReport()
  }

  console.log('\n========================================')
  console.log('  测试账号汇总')
  console.log('========================================')
  console.log('管理员: admin / admin123')
  console.log('教师: teacher1 ~ teacher8 / teacher123')
  console.log('学生: student1 ~ student30 / student123')
  console.log('========================================')
}

main()

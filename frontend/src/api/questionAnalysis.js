import request from '../utils/request'

export function generateAnalysisReport(data) {
  return request.post('/question-analysis/report', data)
}

export function getQuestionAnalysisList(data) {
  return request.post('/question-analysis/list', data)
}

export const ERROR_CATEGORY = {
  SUCCESS: 'success',
  PARAM: 'param',
  AUTH: 'auth',
  BUSINESS: 'business',
  SYSTEM: 'system',
  RESOURCE: 'resource',
  FILE: 'file'
}

export const ERROR_CODE = {
  SUCCESS: 200,
  FAIL: 500,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,

  USER_NOT_FOUND: 1001,
  USER_PASSWORD_ERROR: 1002,
  USER_ALREADY_EXIST: 1003,
  USER_DISABLED: 1004,
  USER_PASSWORD_SAME: 1005,

  TOKEN_INVALID: 2001,
  TOKEN_EXPIRED: 2002,
  TOKEN_EMPTY: 2003,

  EXAM_NOT_FOUND: 3001,
  EXAM_NOT_STARTED: 3002,
  EXAM_ENDED: 3003,
  EXAM_ALREADY_TAKEN: 3004,

  PAPER_NOT_FOUND: 4001,
  PAPER_NO_QUESTIONS: 4002,
  PAPER_PUBLISHED: 4003,

  QUESTION_NOT_FOUND: 5001,
  QUESTION_TYPE_UNSUPPORTED: 5002,

  SUBJECT_NOT_FOUND: 6001,
  SUBJECT_HAS_EXAM: 6002,

  PARAM_ERROR: 7001,
  PARAM_EMPTY: 7002,
  PARAM_FORMAT_ERROR: 7003,
  PARAM_OUT_OF_RANGE: 7004,

  FILE_UPLOAD_ERROR: 8001,
  FILE_TYPE_ERROR: 8002,
  FILE_SIZE_ERROR: 8003,
  FILE_NOT_FOUND: 8004,
  FILE_READ_ERROR: 8005,

  DATABASE_ERROR: 9001,
  SYSTEM_ERROR: 9999
}

export const ERROR_INFO = {
  [ERROR_CODE.SUCCESS]: { message: '操作成功', detail: '操作执行成功', category: ERROR_CATEGORY.SUCCESS },
  [ERROR_CODE.FAIL]: { message: '操作失败', detail: '操作执行失败，请稍后重试', category: ERROR_CATEGORY.SYSTEM },
  [ERROR_CODE.BAD_REQUEST]: { message: '请求参数错误', detail: '请求参数不合法，请检查后重试', category: ERROR_CATEGORY.PARAM },
  [ERROR_CODE.UNAUTHORIZED]: { message: '未授权，请先登录', detail: '用户未登录或登录状态已过期', category: ERROR_CATEGORY.AUTH },
  [ERROR_CODE.FORBIDDEN]: { message: '无权限访问', detail: '当前用户没有访问该资源的权限', category: ERROR_CATEGORY.AUTH },
  [ERROR_CODE.NOT_FOUND]: { message: '资源不存在', detail: '请求的资源不存在或已被删除', category: ERROR_CATEGORY.RESOURCE },

  [ERROR_CODE.USER_NOT_FOUND]: { message: '用户不存在', detail: '该用户账号不存在', category: ERROR_CATEGORY.BUSINESS },
  [ERROR_CODE.USER_PASSWORD_ERROR]: { message: '用户名或密码错误', detail: '请检查用户名和密码是否正确', category: ERROR_CATEGORY.BUSINESS },
  [ERROR_CODE.USER_ALREADY_EXIST]: { message: '用户已存在', detail: '该用户名或邮箱已被注册', category: ERROR_CATEGORY.BUSINESS },
  [ERROR_CODE.USER_DISABLED]: { message: '用户已被禁用', detail: '该账号已被管理员禁用，请联系管理员', category: ERROR_CATEGORY.BUSINESS },
  [ERROR_CODE.USER_PASSWORD_SAME]: { message: '新密码不能与旧密码相同', detail: '请输入与旧密码不同的新密码', category: ERROR_CATEGORY.BUSINESS },

  [ERROR_CODE.TOKEN_INVALID]: { message: 'Token无效', detail: 'Token格式不正确或已被篡改', category: ERROR_CATEGORY.AUTH },
  [ERROR_CODE.TOKEN_EXPIRED]: { message: 'Token已过期', detail: '登录状态已过期，请重新登录', category: ERROR_CATEGORY.AUTH },
  [ERROR_CODE.TOKEN_EMPTY]: { message: 'Token不能为空', detail: '请求头中未包含Token信息', category: ERROR_CATEGORY.AUTH },

  [ERROR_CODE.EXAM_NOT_FOUND]: { message: '考试不存在', detail: '该考试信息不存在或已被删除', category: ERROR_CATEGORY.BUSINESS },
  [ERROR_CODE.EXAM_NOT_STARTED]: { message: '考试未开始', detail: '当前时间早于考试开始时间', category: ERROR_CATEGORY.BUSINESS },
  [ERROR_CODE.EXAM_ENDED]: { message: '考试已结束', detail: '当前时间已超过考试结束时间', category: ERROR_CATEGORY.BUSINESS },
  [ERROR_CODE.EXAM_ALREADY_TAKEN]: { message: '您已参加过此次考试', detail: '该考试只允许参加一次', category: ERROR_CATEGORY.BUSINESS },

  [ERROR_CODE.PAPER_NOT_FOUND]: { message: '试卷不存在', detail: '该试卷信息不存在或已被删除', category: ERROR_CATEGORY.BUSINESS },
  [ERROR_CODE.PAPER_NO_QUESTIONS]: { message: '试卷没有题目', detail: '当前试卷未配置任何题目', category: ERROR_CATEGORY.BUSINESS },
  [ERROR_CODE.PAPER_PUBLISHED]: { message: '试卷已发布，无法修改', detail: '已发布的试卷不能编辑或删除', category: ERROR_CATEGORY.BUSINESS },

  [ERROR_CODE.QUESTION_NOT_FOUND]: { message: '题目不存在', detail: '该题目信息不存在或已被删除', category: ERROR_CATEGORY.BUSINESS },
  [ERROR_CODE.QUESTION_TYPE_UNSUPPORTED]: { message: '题目类型不支持', detail: '该题目类型暂不支持', category: ERROR_CATEGORY.BUSINESS },

  [ERROR_CODE.SUBJECT_NOT_FOUND]: { message: '学科不存在', detail: '该学科信息不存在或已被删除', category: ERROR_CATEGORY.BUSINESS },
  [ERROR_CODE.SUBJECT_HAS_EXAM]: { message: '学科下存在考试', detail: '请先删除该学科下的所有考试', category: ERROR_CATEGORY.BUSINESS },

  [ERROR_CODE.PARAM_ERROR]: { message: '参数错误', detail: '请求参数不合法', category: ERROR_CATEGORY.PARAM },
  [ERROR_CODE.PARAM_EMPTY]: { message: '参数不能为空', detail: '必填参数缺失', category: ERROR_CATEGORY.PARAM },
  [ERROR_CODE.PARAM_FORMAT_ERROR]: { message: '参数格式错误', detail: '参数格式不符合要求', category: ERROR_CATEGORY.PARAM },
  [ERROR_CODE.PARAM_OUT_OF_RANGE]: { message: '参数超出范围', detail: '参数值超出允许的范围', category: ERROR_CATEGORY.PARAM },

  [ERROR_CODE.FILE_UPLOAD_ERROR]: { message: '文件上传失败', detail: '文件上传过程中发生错误', category: ERROR_CATEGORY.FILE },
  [ERROR_CODE.FILE_TYPE_ERROR]: { message: '文件类型不支持', detail: '请上传支持的文件格式', category: ERROR_CATEGORY.FILE },
  [ERROR_CODE.FILE_SIZE_ERROR]: { message: '文件大小超出限制', detail: '文件大小超过最大允许值', category: ERROR_CATEGORY.FILE },
  [ERROR_CODE.FILE_NOT_FOUND]: { message: '文件不存在', detail: '请求的文件不存在', category: ERROR_CATEGORY.FILE },
  [ERROR_CODE.FILE_READ_ERROR]: { message: '文件读取失败', detail: '文件读取过程中发生错误', category: ERROR_CATEGORY.FILE },

  [ERROR_CODE.DATABASE_ERROR]: { message: '数据库操作错误', detail: '数据库操作失败，请稍后重试', category: ERROR_CATEGORY.SYSTEM },
  [ERROR_CODE.SYSTEM_ERROR]: { message: '系统异常', detail: '系统内部错误，请联系管理员', category: ERROR_CATEGORY.SYSTEM }
}

export function getErrorInfo(code) {
  return ERROR_INFO[code] || { message: '未知错误', detail: '发生了未知错误，请稍后重试', category: ERROR_CATEGORY.SYSTEM }
}

export function getErrorMessage(code) {
  const info = getErrorInfo(code)
  return info.message
}

export function getErrorDetail(code) {
  const info = getErrorInfo(code)
  return info.detail
}

export function getErrorCategory(code) {
  const info = getErrorInfo(code)
  return info.category
}

export function isAuthError(code) {
  return getErrorCategory(code) === ERROR_CATEGORY.AUTH
}

export function isParamError(code) {
  return getErrorCategory(code) === ERROR_CATEGORY.PARAM
}

export function isBusinessError(code) {
  return getErrorCategory(code) === ERROR_CATEGORY.BUSINESS
}

export function isSystemError(code) {
  return getErrorCategory(code) === ERROR_CATEGORY.SYSTEM
}

export function isSuccess(code) {
  return code === ERROR_CODE.SUCCESS
}

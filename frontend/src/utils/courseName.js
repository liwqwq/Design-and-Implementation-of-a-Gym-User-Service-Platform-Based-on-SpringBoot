// 统一处理课程名称显示，避免页面直接显示 courses.xxx / course.xxx 这类 i18n key
const COURSE_NAME_ALIASES = {
  'course.strength': '力量训练',
  'course.strengthTraining': '力量训练',
  'course.yoga': '瑜伽放松',
  'course.yogaRelaxation': '瑜伽放松',
  'course.spinning': '动感单车',
  'course.cycling': '动感单车',
  'course.running': '跑步',
  'course.run': '跑步',
  'course.swimming': '游泳',
  'course.swim': '游泳',
  'course.pilates': '普拉提',
  'course.boxing': '搏击',
  'course.dance': '舞蹈',
  'course.aerobics': '有氧操',
  'course.hiit': 'HIIT训练',
  'courses.strength': '力量训练',
  'courses.strengthTraining': '力量训练',
  'courses.yoga': '瑜伽放松',
  'courses.yogaRelaxation': '瑜伽放松',
  'courses.spinning': '动感单车',
  'courses.cycling': '动感单车',
  'courses.running': '跑步',
  'courses.run': '跑步',
  'courses.swimming': '游泳',
  'courses.swim': '游泳',
  'courses.pilates': '普拉提',
  'courses.boxing': '搏击',
  'courses.dance': '舞蹈',
  'courses.aerobics': '有氧操',
  'courses.hiit': 'HIIT训练',
  strength: '力量训练',
  strengthTraining: '力量训练',
  yoga: '瑜伽放松',
  yogaRelaxation: '瑜伽放松',
  spinning: '动感单车',
  cycling: '动感单车',
  running: '跑步',
  run: '跑步',
  swimming: '游泳',
  swim: '游泳',
  pilates: '普拉提',
  boxing: '搏击',
  dance: '舞蹈',
  aerobics: '有氧操',
  hiit: 'HIIT训练'
}

export function formatCourseName(name, t, te) {
  if (!name) return ''
  const raw = String(name).trim()
  if (!raw) return ''

  // 兼容数据库里已经保存成 course.xxx / courses.xxx 的旧数据
  const alias = COURSE_NAME_ALIASES[raw]
  if (alias) {
    const aliasKey = `courses.${alias}`
    return te(aliasKey) ? t(aliasKey) : alias
  }

  // 如果传入的本身就是完整 i18n key，优先直接翻译
  if ((raw.startsWith('courses.') || raw.startsWith('course.')) && te(raw)) {
    return t(raw)
  }

  // 正常情况：数据库保存中文课程名，用 courses.中文名 做中英文转换
  const key = `courses.${raw}`
  if (te(key)) return t(key)

  // 找不到翻译时不要返回 courses.xxx，直接显示原始课程名
  return raw.replace(/^courses?\./, '')
}

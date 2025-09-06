/**
 * 处理请求参数，将日期范围数组转换为后端期望的格式
 * @param {Object} params - 原始参数对象
 * @returns {Object} - 处理后的参数对象
 */
export function processDateRangeParams(params) {
  const processedParams = { ...params }
  
  // 处理 dateRange 参数
  if (processedParams.dateRange && Array.isArray(processedParams.dateRange) && processedParams.dateRange.length === 2) {
    processedParams.startDate = processedParams.dateRange[0]
    processedParams.endDate = processedParams.dateRange[1]
    delete processedParams.dateRange
  }
  
  return processedParams
}

/**
 * 格式化日期为 YYYY-MM-DD 格式
 * @param {Date|string} date - 日期对象或日期字符串
 * @returns {string} - 格式化后的日期字符串
 */
export function formatDate(date) {
  if (!date) return null
  
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  
  return `${year}-${month}-${day}`
} 
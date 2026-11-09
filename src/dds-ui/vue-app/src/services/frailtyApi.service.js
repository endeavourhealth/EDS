import { api, init } from './apiClient'

export { init }

export async function getFrailtyApiStats(minutesBack = 60, groupBy = 'subscriber', flagCode = '') {
  const res = await api.get('/frailtyApi/recentStats', { params: { minutesBack, groupBy, flagCode } })
  return res.data
}

export async function downloadFrailtyMonthlyStats() {
  const res = await api.get('/frailtyApi/downloadMonthlyStats')
  return res.data
}

export async function getFrailtySubscriberFlags() {
  const res = await api.get('/frailtyApi/subscriberFlags')
  return res.data
}

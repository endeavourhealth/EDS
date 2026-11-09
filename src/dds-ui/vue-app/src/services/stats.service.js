import { api, init } from './apiClient'

export { init }

export async function getStorageStatistics(serviceList, systemList) {
  const res = await api.get('/stats/getStorageStatistics', { params: { serviceList, systemList } })
  return res.data
}

export async function downloadPatientCounts() {
  const res = await api.get('/stats/downloadPatientCounts')
  return res.data
}

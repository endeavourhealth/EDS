import { api, init } from './apiClient'

export { init }

export async function getScheduledTaskRules() {
  const res = await api.get('/scheduledTask/rules')
  return res.data
}

export async function getScheduledTaskSummary() {
  const res = await api.get('/scheduledTask/summary')
  return res.data
}

import { api, init } from './apiClient'

export { init }

export async function getLoggingEvents(serviceId, level, page = 1) {
  const res = await api.get('/logging/getLoggingEvents', { params: { serviceId, level, page } })
  return res.data
}

export async function getStackTrace(eventId) {
  const res = await api.get('/logging/getStackTrace', { params: { eventId } })
  return res.data
}

import { api, init } from './apiClient'

export { init }

export async function getHl7v2ApiStatus() {
  const res = await api.get('/hl7v2Api/status')
  return res.data
}

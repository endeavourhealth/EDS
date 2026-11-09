import { api, init } from './apiClient'

export { init }

export async function getDatabaseServers() {
  const res = await api.get('/databaseStats/databaseServers')
  return res.data
}

export async function getDatabases(type, host) {
  const res = await api.get(`/databaseStats/databases/${type}/${host}`)
  return res.data
}

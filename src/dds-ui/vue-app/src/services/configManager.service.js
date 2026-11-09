import { api } from './apiClient.js'

export async function getRecords() {
  const response = await api.get('/configManager/records')
  return response.data
}

export async function saveRecord(record) {
  const response = await api.post('/configManager/saveRecord', JSON.stringify(record), {
    headers: { 'Content-Type': 'application/json' }
  })
  return response.data
}

export async function deleteRecord(record) {
  const response = await api.post('/configManager/deleteRecord', JSON.stringify(record), {
    headers: { 'Content-Type': 'application/json' }
  })
  return response.data
}

export async function getHistory(appId, configId) {
  const response = await api.get('/configManager/history', { params: { appId, configId } })
  return response.data
}

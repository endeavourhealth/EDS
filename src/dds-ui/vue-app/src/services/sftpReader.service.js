import { api, init } from './apiClient'

export { init }

export async function getSftpReaderInstances() {
  const res = await api.get('/sftpReader/instances')
  return res.data
}

export async function getSftpReaderStatus(configurationId) {
  const res = await api.get('/sftpReader/status', { params: { configurationId } })
  return res.data
}

export async function getSftpReaderHistory(configurationId, dFrom, dTo) {
  const res = await api.get('/sftpReader/history', { params: { configurationId, from: dFrom.getTime(), to: dTo.getTime() } })
  return res.data
}

export async function ignoreBatchSplit(batchId, batchSplitId, configurationId, reason) {
  const res = await api.post('/sftpReader/ignore', { batchId, batchSplitId, configurationId, reason })
  return res.data
}

export async function togglePause(configurationId) {
  const res = await api.post('/sftpReader/togglePause', { configurationId })
  return res.data
}

export async function togglePauseAll() {
  const res = await api.post('/sftpReader/togglePauseAll')
  return res.data
}

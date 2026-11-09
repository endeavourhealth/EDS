import { api, init } from './apiClient'

export { init }

export async function getRemoteFilers() {
  const res = await api.get('/remoteFiling/getRemoteFilers')
  return res.data
}

export async function getRemoteFilerStatus(subscriberId) {
  const res = await api.get('/remoteFiling/getRemoteFilerStatus', { params: { subscriberId } })
  return res.data
}

export async function getQueuedMessage(queuedMessageId) {
  const res = await api.get('/remoteFiling/getQueuedMessage', { params: { queuedMessageId } })
  return res.data
}

import { api, init } from './apiClient'

export { init }

export async function getRabbitNodes() {
  const res = await api.get('/rabbit/nodes')
  return res.data
}

export async function getRabbitQueues(address) {
  const res = await api.get('/rabbit/queues', { params: { address } })
  return res.data
}

export async function getRabbitQueuesFiltered(address, prefix) {
  const res = await api.get('/rabbit/queues/filtered', { params: { address, prefix } })
  return res.data
}

export async function getRabbitExchanges(address) {
  const res = await api.get('/rabbit/exchanges', { params: { address } })
  return res.data
}

export async function getRabbitBindings(address) {
  const res = await api.get('/rabbit/bindings', { params: { address } })
  return res.data
}

export async function pingRabbitNode(address) {
  const res = await api.get('/rabbit/ping', { params: { address } })
  return res.data
}

export async function synchronizeRabbit() {
  const res = await api.post('/rabbit/synchronize')
  return res.data
}

export async function getRoutings() {
  const res = await api.get('/rabbit/routings')
  return res.data
}

export async function saveRoutings(routings) {
  const res = await api.post('/rabbit/routings', routings)
  return res.data
}

export async function getOverrides() {
  const res = await api.get('/rabbit/overrides')
  return res.data
}

export async function saveOverrides(overrides) {
  const res = await api.post('/rabbit/overrides', overrides)
  return res.data
}

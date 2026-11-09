import { api } from './apiClient.js'

export async function getTransformErrorSummaries() {
  const response = await api.get('/exchangeAudit/getTransformErrorSummaries')
  return response.data
}

export async function getInboundTransformAudits(serviceId, systemId, exchangeId) {
  const response = await api.get('/exchangeAudit/getInboundTransformAudits', {
    params: { serviceId, systemId, exchangeId, getAllAuditsAndEvents: false }
  })
  return response.data
}

export async function rerunAllExchangesInError(serviceId, systemId) {
  const response = await api.post('/exchangeAudit/rerunAllExchangesInError', { serviceId, systemId })
  return response.data
}

export async function requeueServicesInError(summaries) {
  const requests = summaries.map(s => ({ serviceId: s.service.uuid, systemId: s.systemId }))
  const response = await api.post('/exchangeAudit/requeueServicesInError', requests)
  return response.data
}

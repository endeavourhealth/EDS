import { api } from './apiClient.js'

export async function getUsers() {
  const response = await api.get('/audit/users')
  return response.data
}

export async function getModules() {
  const response = await api.get('/audit/modules')
  return response.data
}

export async function getActions() {
  const response = await api.get('/audit/actions')
  return response.data
}

export async function getSubmodules(module) {
  const response = await api.get('/audit/submodules', { params: { module } })
  return response.data
}

export async function getUserAudit(module, userId, month, organisationId) {
  const response = await api.get('/audit', {
    params: { module, userId, month: month.getTime(), organisationId }
  })
  return response.data
}

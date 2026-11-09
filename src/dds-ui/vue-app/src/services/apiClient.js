import axios from 'axios'

const getBasePath = () => {
  const knownRoutes = ['queueServerAdmin', 'services', 'subscribers', 'queueReaders', 'subscriberApi', 'hl7Receiver', 'hl7v2Api', 'sftpReader', 'scheduledTasks', 'stats', 'remoteFiling', 'logging', 'databaseStats', 'systemConfig', 'configManager', 'publisherErrors', 'routingConfig', 'audit']
  const path = window.location.pathname
  
  let routeIndex = -1
  for (const route of knownRoutes) {
    const idx = path.indexOf('/' + route)
    if (idx > 0 && (routeIndex === -1 || idx < routeIndex)) {
      routeIndex = idx
    }
  }
  
  if (routeIndex > 0) {
    const base = path.substring(0, routeIndex)
    return base === '/' ? '/' : base + '/'
  }
  
  const parts = path.split('/').filter(p => p)
  if (parts.length > 0 && !knownRoutes.includes(parts[0])) {
    return '/' + parts[0] + '/'
  }
  
  return '/'
}

const basePath = getBasePath()

export const api = axios.create({
  baseURL: basePath + 'api',
})

let keycloakInstance = null

export function init(keycloak) {
  keycloakInstance = keycloak
  api.interceptors.request.use(config => {
    if (keycloakInstance?.token) config.headers.Authorization = `Bearer ${keycloakInstance.token}`
    return config
  })
  api.interceptors.response.use(
    response => response,
    error => Promise.reject(error)
  )
}

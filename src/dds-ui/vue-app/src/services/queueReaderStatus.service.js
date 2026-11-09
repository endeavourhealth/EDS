import { api, init } from './apiClient'

export { init }

export async function getQueueReaderStatus(applicationName) {
  const res = await api.get('/queueReader/status', { params: { applicationName } })
  return res.data
}

export async function queueReaderCommand() {
  const res = await api.post('/queueReader/queueReaderCommand', { command: 'sudo ./queueReader/deploy.sh' })
  return res.data
}

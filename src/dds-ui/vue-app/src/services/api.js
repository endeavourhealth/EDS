import { api } from './apiClient'
export { api, init } from './apiClient'

// Queue Server Admin API functions
export async function getServers() {
  const response = await api.get('/queueServer/servers')
  return response.data
}

export async function getInstances() {
  const response = await api.get('/queueReader/instances')
  return response.data
}

export async function getMemoryDefaults() {
  const response = await api.get('/queueReader/memoryDefaults')
  return response.data
}

export async function getEc2Status() {
  const response = await api.get('/queueServer/ec2Status')
  return response.data
}

export async function getJarVersions() {
  const response = await api.get('/queueServer/jarVersions')
  return response.data
}

export async function addServer(serverData) {
  const response = await api.post('/queueServer/saveServer', serverData)
  return response.data
}

export async function editServer(serverNumber, serverData) {
  const response = await api.post('/queueServer/saveServer', serverData)
  return response.data
}

export async function deleteServer(serverNumber) {
  const response = await api.delete(`/queueServer/deleteServer/${serverNumber}`)
  return response.data
}

export async function addQueue(queueData) {
  const response = await api.post('/queueServer/saveQueueAssignments', {
    queueName: queueData.name,
    type: queueData.type,
    memory: queueData.memory,
    serverNumbers: queueData.servers
  })
  return response.data
}

export async function saveQueueAssignments(name, type, memory, servers) {
  const response = await api.post('/queueServer/saveQueueAssignments', { name, type, memory, servers })
  return response.data
}

export async function startQueue(serverNumber, queueName, memory) {
  const response = await api.post('/queueServer/startQueue', { serverNumber, queueName, memory })
  return response.data
}

export async function startQueuesOnMultipleServers(queueName, memory, serverNumbers) {
  const response = await api.post('/queueServer/startQueuesOnMultipleServers', { queueName, memory, serverNumbers })
  return response.data
}

export async function stopQueue(serverNumber, queueName) {
  const response = await api.post('/queueServer/stopQueue', { serverNumber, queueName })
  return response.data
}

export async function restartQueue(serverNumber, queueName) {
  const response = await api.post('/queueServer/restartQueue', { serverNumber, queueName })
  return response.data
}

export async function stopAllQueues(serverNumber) {
  const response = await api.post('/queueServer/stopAllQueues', { serverNumber })
  return response.data
}

export async function restartAllQueues(serverNumber) {
  const response = await api.post('/queueServer/restartAllQueues', { serverNumber })
  return response.data
}

export async function shutdownServer(serverNumber) {
  const response = await api.post('/queueServer/shutdownServer', { serverNumber })
  return response.data
}

export async function rebootServer(serverNumber) {
  const response = await api.post('/queueServer/rebootServer', { serverNumber })
  return response.data
}

export async function ec2Start(serverNumber) {
  const response = await api.post('/queueServer/ec2Start', { serverNumber })
  return response.data
}

export async function ec2Stop(serverNumber) {
  const response = await api.post('/queueServer/ec2Stop', { serverNumber })
  return response.data
}

export async function stopInstance(configIds) {
  const response = await api.post('/queueReader/stop', { configIds })
  return response.data
}

export async function restartInstance(configIds) {
  const response = await api.post('/queueReader/restart', { configIds })
  return response.data
}

export async function saveMemoryDefaults(queueType, maxHeap, unit) {
  const response = await api.post('/queueReader/memoryDefaults', { queueType, maxHeap, unit })
  return response.data
}

export async function uploadJar(serverNumber, hostName, fileName, fileData) {
  const response = await api.post('/queueServer/uploadJar', { serverNumber, hostName, fileName, fileData })
  return response.data
}

export async function saveMemoryConfig(configData) {
  const response = await api.post('/queueReader/config', configData)
  return response.data
}

export async function deleteMemoryConfig(configId) {
  const response = await api.delete(`/queueReader/config/${configId}`)
  return response.data
}

export async function queueReaderCommand(configIds) {
  const response = await api.post('/queueReader/queueReaderCommand', { configIds, command: 'sudo ./queueReader/deploy.sh' })
  return response.data
}

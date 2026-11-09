import { api, init } from './apiClient'

export { init }

export async function getHl7ReceiverChannelStatus() {
  const res = await api.get('/hl7Receiver/channelStatus')
  return res.data
}

export async function pauseHl7ReceiverChannel(channelId, pause) {
  const res = await api.post(`/hl7Receiver/pause/${channelId}/${pause}`)
  return res.data
}

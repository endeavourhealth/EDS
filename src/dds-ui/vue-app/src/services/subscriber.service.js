import { api, init } from './apiClient'

export { init }

export const subscribersService = {
  showDates: false,
  publisherNameFilter: null,
  systemNameFilter: null,
  sortFilter: 'NameAsc',
  statusFilter: 'any-issue',
  showFullSubscriberConfigDetails: false
}

export async function getSubscribersInstances() {
  const res = await api.get('/subscribers/subscribers')
  return res.data
}

export async function getSubscriberDetails(subscriberName) {
  const res = await api.get('/subscribers/subscriberDetail', { params: { subscriberName } })
  return res.data
}

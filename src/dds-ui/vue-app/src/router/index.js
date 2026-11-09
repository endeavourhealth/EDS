import { createRouter, createWebHistory } from 'vue-router'
import QueueServerAdmin from '@/views/QueueServerAdmin.vue'

const routes = [
  {
    path: '/',
    redirect: '/queueServerAdmin',
  },
  {
    path: '/queueServerAdmin',
    name: 'QueueServerAdmin',
    component: () => import('@/views/QueueServerAdmin.vue'),
  },
  {
    path: '/services',
    name: 'Publishers',
    component: () => import('@/views/Services.vue'),
  },
  {
    path: '/subscribers',
    name: 'Subscribers',
    component: () => import('@/views/Subscribers.vue'),
  },
  {
    path: '/subscribers/:subscriberName',
    name: 'SubscriberDetail',
    component: () => import('@/views/SubscriberDetail.vue'),
  },
  {
    path: '/queueReaders',
    name: 'QueueReaders',
    component: () => import('@/views/QueueReaders.vue'),
  },
  {
    path: '/subscriberApi',
    name: 'SubscriberApi',
    component: () => import('@/views/FrailtyApi.vue'),
  },
  {
    path: '/hl7Receiver',
    name: 'Hl7Receiver',
    component: () => import('@/views/Hl7Receiver.vue'),
  },
  {
    path: '/hl7v2Api',
    name: 'Hl7v2Api',
    component: () => import('@/views/Hl7v2Api.vue'),
  },
  {
    path: '/sftpReader',
    name: 'SftpReader',
    component: () => import('@/views/SftpReader.vue'),
  },
  {
    path: '/scheduledTasks',
    name: 'ScheduledTasks',
    component: () => import('@/views/ScheduledTasks.vue'),
  },
  {
    path: '/stats',
    name: 'Stats',
    component: () => import('@/views/Stats.vue'),
  },
  {
    path: '/remoteFiling',
    name: 'RemoteFiling',
    component: () => import('@/views/RemoteFiling.vue'),
  },
  {
    path: '/logging',
    name: 'Logging',
    component: () => import('@/views/Logging.vue'),
  },
  {
    path: '/databaseStats',
    name: 'DatabaseStats',
    component: () => import('@/views/DatabaseStats.vue'),
  },
  {
    path: '/systemConfig',
    name: 'SystemConfig',
    component: () => import('@/views/SystemConfig.vue'),
  },
  {
    path: '/configManager',
    name: 'ConfigManager',
    component: () => import('@/views/ConfigManager.vue'),
  },
  {
    path: '/publisherErrors',
    name: 'PublisherErrors',
    component: () => import('@/views/PublisherErrors.vue'),
  },
  {
    path: '/routingConfig',
    name: 'RoutingConfig',
    component: () => import('@/views/RoutingConfig.vue'),
  },
  {
    path: '/audit',
    name: 'Audit',
    component: () => import('@/views/Audit.vue'),
  },
]

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

const router = createRouter({
  history: createWebHistory(getBasePath()),
  routes,
})

export default router

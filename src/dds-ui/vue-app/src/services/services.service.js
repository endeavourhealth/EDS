import { api, init } from './apiClient'

export { init }

export async function getAllServices() {
  const res = await api.get('/service')
  return res.data
}

export async function getService(uuid) {
  const res = await api.get('/service', { params: { uuid } })
  return res.data
}

export async function saveService(service) {
  const res = await api.post('/service', service)
  return res.data
}

export async function validateService(service) {
  const res = await api.post('/service/validateService', service)
  return res.data
}

export async function deleteService(uuid) {
  const res = await api.delete('/service/', { params: { uuid } })
  return res.data
}

export async function deleteData(serviceId) {
  const res = await api.delete('/service/data', { params: { serviceId } })
  return res.data
}

export async function getTagNames() {
  const res = await api.get('/service/tagNames')
  return res.data
}

export async function getPublisherConfigNames() {
  const res = await api.get('/service/publisherConfigNames')
  return res.data
}

export async function getCcgCodes() {
  const res = await api.get('/service/ccgCodes')
  return res.data
}

export async function getCcgNames() {
  const res = await api.get('/service/ccgNames')
  return res.data
}

export async function getOrganisationTypeList() {
  const res = await api.get('/service/organisationTypeList')
  return res.data
}

export async function getOpenOdsRecord(odsCode) {
  const res = await api.get('/service/openOdsRecord', { params: { odsCode } })
  return res.data
}

export async function getDsmDetails(odsCode) {
  const res = await api.get('/service/dsmDetails', { params: { odsCode } })
  return res.data
}

export async function getDpaHistory(uuid) {
  const res = await api.get('/service/dpaHistory', { params: { uuid } })
  return res.data
}

export async function getSubscriberHistory(uuid) {
  const res = await api.get('/service/subscriberHistory', { params: { uuid } })
  return res.data
}

export async function getSystemsForService(serviceId) {
  const res = await api.get('/service/systemsForService', { params: { serviceId } })
  return res.data
}

let tagNameCache = null
let ccgCodeCache = null
let publisherConfigNameCache = null
let ccgNameCache = null

export async function getTagNamesFromCache() {
  if (tagNameCache) return tagNameCache
  
  try {
    tagNameCache = await getTagNames()
  } catch (error) {
    tagNameCache = []
  }
  return tagNameCache
}

export async function getCcgCodesFromCache() {
  if (ccgCodeCache) return ccgCodeCache
  
  try {
    ccgCodeCache = await getCcgCodes()
  } catch (error) {
    ccgCodeCache = []
  }
  return ccgCodeCache
}

export async function getPublisherConfigNamesFromCache() {
  if (publisherConfigNameCache) return publisherConfigNameCache
  
  try {
    publisherConfigNameCache = await getPublisherConfigNames()
  } catch (error) {
    publisherConfigNameCache = []
  }
  return publisherConfigNameCache
}

export async function getCcgNamesFromCache() {
  if (ccgNameCache) return ccgNameCache
  
  try {
    ccgNameCache = await getCcgNames()
  } catch (error) {
    ccgNameCache = {}
  }
  return ccgNameCache
}

export function getCcgName(ccgCode) {
  if (!ccgCode) return ''
  if (ccgNameCache && ccgNameCache[ccgCode]) return ccgNameCache[ccgCode]
  return 'loading...'
}

export const filterState = {
  serviceNameFilter: null,
  serviceNameSearchIncludeTags: false,
  serviceNameSearchSpecificTag: '',
  publisherConfigFilter: null,
  statusFilter: null,
  ccgCodeFilterStr: null,
  ccgCodeFilterRegex: null,
  ccgCodeFilterIsRegex: false,
  lastDataFilter: null,
  publisherModeFilter: null,
  hideClosedFilter: true,
  sortFilter: 'NameAsc'
}

export function clearFilters() {
  filterState.serviceNameFilter = null
  filterState.serviceNameSearchIncludeTags = false
  filterState.serviceNameSearchSpecificTag = ''
  filterState.publisherConfigFilter = null
  filterState.statusFilter = null
  filterState.ccgCodeFilterStr = null
  filterState.ccgCodeFilterRegex = null
  filterState.ccgCodeFilterIsRegex = false
  filterState.lastDataFilter = null
  filterState.publisherModeFilter = null
  filterState.hideClosedFilter = true
  filterState.sortFilter = 'NameAsc'
}

export function applyFiltering(services, isPublisherErrors = false) {
  if (!services || !Array.isArray(services)) return []

  let filtered = []

  let validNameFilterRegex
  if (filterState.serviceNameFilter) {
    try {
      new RegExp(filterState.serviceNameFilter)
      validNameFilterRegex = filterState.serviceNameFilter.toLowerCase().trim()
    } catch (e) {
    }
  }

  let validParentFilterRegex
  if (filterState.ccgCodeFilterIsRegex && filterState.ccgCodeFilterRegex) {
    try {
      new RegExp(filterState.ccgCodeFilterRegex)
      validParentFilterRegex = filterState.ccgCodeFilterRegex.toLowerCase()
    } catch (e) {
    }
  }

  let latestCutoffLimit
  const now = new Date().getTime()
  const dayDuration = 1000 * 60 * 60 * 24

  if (filterState.lastDataFilter) {
    if (filterState.lastDataFilter === '1d') {
      latestCutoffLimit = now - dayDuration
    } else if (filterState.lastDataFilter === '2d') {
      latestCutoffLimit = now - (dayDuration * 2)
    } else if (filterState.lastDataFilter === '1wk') {
      latestCutoffLimit = now - (dayDuration * 7)
    } else if (filterState.lastDataFilter === '2wk') {
      latestCutoffLimit = now - (dayDuration * 14)
    } else if (filterState.lastDataFilter === 'older') {
      latestCutoffLimit = now - (dayDuration * 14)
    }
  }

  for (const service of services) {
    if (filterState.publisherConfigFilter) {
      const publisherConfigName = service.publisherConfigName
      if (filterState.publisherConfigFilter === 'NoPublisher') {
        if (publisherConfigName) continue
      } else {
        if (!publisherConfigName || publisherConfigName !== filterState.publisherConfigFilter) continue
      }
    }

    if (!filterState.ccgCodeFilterIsRegex && filterState.ccgCodeFilterStr) {
      const ccgCode = service.ccgCode
      if (!ccgCode || ccgCode !== filterState.ccgCodeFilterStr) continue
    }

    if (filterState.ccgCodeFilterIsRegex && validParentFilterRegex) {
      const ccgCode = service.ccgCode
      if (!ccgCode || !ccgCode.toLowerCase().match(validParentFilterRegex)) continue
    }

    if (!isPublisherErrors && filterState.statusFilter) {
      const desiredVal = getStatusValue(filterState.statusFilter)
      const statusVal = getSortingStatusValue(service)
      if (statusVal !== desiredVal) continue
    }

    if (filterState.publisherModeFilter) {
      let include = false
      if (service.systemStatuses) {
        for (const systemStatus of service.systemStatuses) {
          if (systemStatus.publisherMode === filterState.publisherModeFilter) {
            include = true
            break
          }
        }
      }
      if (!include) continue
    }

    if (latestCutoffLimit) {
      let include = false
      if (service.systemStatuses) {
        for (const systemStatus of service.systemStatuses) {
          const cutoff = systemStatus.lastReceivedExtractCutoff
          if (filterState.lastDataFilter === 'older') {
            if (cutoff < latestCutoffLimit) include = true
          } else {
            if (cutoff > latestCutoffLimit) include = true
          }
        }
      }
      if (!include) continue
    }

    if (filterState.hideClosedFilter) {
      if (service.tags && service.tags.hasOwnProperty('Closed')) continue
    }

    if (filterState.serviceNameSearchIncludeTags && filterState.serviceNameSearchSpecificTag) {
      if (!service.tags || !service.tags.hasOwnProperty(filterState.serviceNameSearchSpecificTag)) continue
    }

    if (validNameFilterRegex) {
      const name = service.name
      const alias = service.alias
      const id = service.localIdentifier
      const uuid = service.uuid

      let include = false
      if (name && name.toLowerCase().match(validNameFilterRegex)) include = true
      if (alias && alias.toLowerCase().match(validNameFilterRegex)) include = true
      if (id && id.toLowerCase().match(validNameFilterRegex)) include = true
      if (uuid && uuid.toLowerCase() === validNameFilterRegex) include = true

      if (!include && service.tags && filterState.serviceNameSearchIncludeTags) {
        const tagNames = Object.keys(service.tags)
        for (const tagName of tagNames) {
          const tagValue = service.tags[tagName]
          if (!filterState.serviceNameSearchSpecificTag || tagName === filterState.serviceNameSearchSpecificTag) {
            if (tagName.toLowerCase().match(validNameFilterRegex) || (tagValue && tagValue.toLowerCase().match(validNameFilterRegex))) {
              include = true
              break
            }
          }
        }
      }

      if (!include) continue
    }

    filtered.push(service)
  }

  filtered.sort((a, b) => a.name.toLowerCase().localeCompare(b.name.toLowerCase()))

  if (filterState.sortFilter) {
    if (filterState.sortFilter === 'IDAsc' || filterState.sortFilter === 'IDDesc') {
      filtered.sort((a, b) => (a.localIdentifier || '').localeCompare(b.localIdentifier || ''))
      if (filterState.sortFilter === 'IDDesc') filtered.reverse()
    } else if (filterState.sortFilter === 'ParentAsc' || filterState.sortFilter === 'ParentDesc') {
      filtered.sort((a, b) => (a.ccgCode || '').localeCompare(b.ccgCode || ''))
      if (filterState.sortFilter === 'ParentDesc') filtered.reverse()
    } else if (filterState.sortFilter === 'PublisherConfigAsc' || filterState.sortFilter === 'PublisherConfigDesc') {
      filtered.sort((a, b) => (a.publisherConfigName || '').localeCompare(b.publisherConfigName || ''))
      if (filterState.sortFilter === 'PublisherConfigDesc') filtered.reverse()
    } else if (filterState.sortFilter === 'LastDataAsc' || filterState.sortFilter === 'LastDataDesc') {
      filtered.sort((a, b) => getSortingDate(b) - getSortingDate(a))
      if (filterState.sortFilter === 'LastDataDesc') filtered.reverse()
    } else if (filterState.sortFilter === 'StatusAsc' || filterState.sortFilter === 'StatusDesc') {
      filtered.sort((a, b) => getSortingStatusValue(a) - getSortingStatusValue(b))
      if (filterState.sortFilter === 'StatusDesc') filtered.reverse()
    }
  }

  return filtered
}

function getStatusValue(filter) {
  const map = { NoStatus: 0, NoData: 1, OK: 2, Behind: 3, Error: 4 }
  return map[filter] || 0
}

function getSortingStatusValue(service) {
  let ret = 0
  if (service.systemStatuses) {
    for (const status of service.systemStatuses) {
      let thisVal
      if (!status.lastReceivedExtract) thisVal = 1
      else if (!status.processingInError && status.processingUpToDate) thisVal = 2
      else if (!status.processingInError && !status.processingUpToDate) thisVal = 3
      else thisVal = 4

      if (thisVal > ret) ret = thisVal
    }
  }
  return ret
}

function getSortingDate(service) {
  let ret
  if (service.systemStatuses) {
    for (const status of service.systemStatuses) {
      if (status.lastReceivedExtractCutoff && (!ret || status.lastReceivedExtractCutoff > ret)) {
        ret = status.lastReceivedExtractCutoff
      }
    }
  }
  if (!ret) ret = new Date().getTime()
  return ret
}

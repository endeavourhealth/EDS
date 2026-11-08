<template>
  <div class="module">
    <div class="container-fluid">
      <div class="module-heading">
        Publishers
        <span v-if="services">
          (showing {{ filteredServices.length }} of {{ services.length }})
        </span>

        <div class="pull-right">
          <button class="p-button p-button-secondary p-button-sm" @click="saveToCsv">Save to CSV</button>
          <button class="p-button p-button-secondary p-button-sm" @click="clearFilters">Reset Filters</button>
          <button class="p-button p-button-secondary p-button-sm" @click="refreshAllServices">Refresh</button>
          <button class="p-button p-button-success p-button-sm" @click="addService">Add new</button>
        </div>
      </div>
      <div class="module-body">
        <div class="module" style="margin-bottom: 1rem;">
          <div class="module-body">
            <div class="form-horizontal" role="form">
              <div class="form-group">
                <div class="col-sm-2">
                  <label for="serviceNameFilter" class="control-label">Name/ID</label>
                  <div class="pull-right">
                    <label for="serviceNameSearchIncludeTags" class="control-label">Match tags</label>
                    <input type="checkbox" id="serviceNameSearchIncludeTags" v-model="serviceNameSearchIncludeTags" @change="applyFiltering">
                    <span v-if="serviceNameSearchIncludeTags">
                      <select id="serviceNameSearchSpecificTag" v-model="serviceNameSearchSpecificTag" @change="applyFiltering">
                        <option value="">All Tags</option>
                        <option v-for="tagName in tagNames" :key="tagName" :value="tagName">{{ tagName }}</option>
                      </select>
                    </span>
                  </div>
                  <input type="text" class="form-control" id="serviceNameFilter" placeholder="Regex" v-model="serviceNameFilter" @input="applyFiltering">
                </div>

                <div class="col-sm-2">
                  <label for="ccgCodeFilter" class="control-label">Parent</label>
                  <div class="pull-right">
                    <label for="ccgCodeFilterIsRegex" class="control-label">Regex</label>
                    <input type="checkbox" id="ccgCodeFilterIsRegex" v-model="ccgCodeFilterIsRegex" @change="applyFiltering">
                  </div>
                  <select v-if="!ccgCodeFilterIsRegex" id="ccgCodeFilter" class="form-control" v-model="ccgCodeFilterStr" @change="applyFiltering">
                    <option value=""></option>
                    <option v-for="ccgCode in ccgCodes" :key="ccgCode" :value="ccgCode">{{ ccgCode }} {{ getCcgName(ccgCode) }}</option>
                  </select>
                  <input v-if="ccgCodeFilterIsRegex" type="text" class="form-control" id="ccgCodeFilter" placeholder="Regex" v-model="ccgCodeFilterRegex" @input="applyFiltering">
                </div>

                <div class="col-sm-2">
                  <label for="publisherConfigFilter" class="control-label">Publisher DB</label>
                  <select id="publisherConfigFilter" class="form-control" v-model="publisherConfigFilter" @change="applyFiltering">
                    <option value=""></option>
                    <option v-for="configName in publisherConfigNames" :key="configName" :value="configName">{{ configName }}</option>
                    <option value="NoPublisher">No publisher set</option>
                  </select>
                </div>

                <div class="col-sm-1">
                  <label for="lastDataFilter" class="control-label">Latest Cutoff</label>
                  <select id="lastDataFilter" class="form-control" v-model="lastDataFilter" @change="applyFiltering">
                    <option value=""></option>
                    <option value="1d">&le;1d</option>
                    <option value="2d">&le;2d</option>
                    <option value="1wk">&le;1wkd</option>
                    <option value="2wk">&le;2wk</option>
                    <option value="older">&gt;2wk</option>
                  </select>
                </div>

                <div class="col-sm-1">
                  <label for="publisherModeFilter" class="control-label">Publisher Mode</label>
                  <select id="publisherModeFilter" class="form-control" v-model="publisherModeFilter" @change="applyFiltering">
                    <option value=""></option>
                    <option value="Publisher_Draft">Draft</option>
                    <option value="Publisher_Normal">Normal</option>
                    <option value="Publisher_Auto_Fail">Auto-fail</option>
                    <option value="Publisher_Bulk">Bulk</option>
                  </select>
                </div>

                <div class="col-sm-1">
                  <label for="statusFilter" class="control-label">Status</label>
                  <select id="statusFilter" class="form-control" v-model="statusFilter" @change="applyFiltering">
                    <option value=""></option>
                    <option value="Error">Error</option>
                    <option value="Behind">Behind</option>
                    <option value="OK">OK</option>
                    <option value="NoData">No Data</option>
                    <option value="NoStatus">No Status</option>
                  </select>
                </div>

                <div class="col-sm-2">
                  <label for="sortFilter" class="control-label">Sorting</label>
                  <select id="sortFilter" class="form-control" v-model="sortFilter" @change="applyFiltering">
                    <option value="NameAsc">Name</option>
                    <option value="NameDesc">Name (D)</option>
                    <option value="IDAsc">ID</option>
                    <option value="IDDesc">ID (D)</option>
                    <option value="ParentAsc">Parent</option>
                    <option value="ParentDesc">Parent (D)</option>
                    <option value="PublisherConfigAsc">Publisher DB</option>
                    <option value="PublisherConfigDesc">Publisher DB (D)</option>
                    <option value="LastDataAsc">Latest Cutoff</option>
                    <option value="LastDataDesc">Latest Cutoff (D)</option>
                    <option value="StatusAsc">Status</option>
                    <option value="StatusDesc">Status (D)</option>
                  </select>
                </div>

                <div class="col-sm-1">
                  <label for="hideClosedFilter" class="control-label">Hide Closed</label>
                  <input type="checkbox" id="hideClosedFilter" v-model="hideClosedFilter" @change="applyFiltering">
                  <div>
                    <label for="showDateFilter" class="control-label">Show Dates</label>
                    <input type="checkbox" id="showDateFilter" v-model="showDateFilter">
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <form>
          <div class="row">
            <div class="form-group col-md-12">
              <div class="table">
                <table class="table table-striped table-condensed">
                  <thead>
                    <tr>
                      <th class="col-md-3">Name</th>
                      <th class="col-md-1">ID</th>
                      <th class="col-md-1">Parent</th>
                      <th class="col-md-1">Publisher DB</th>
                      <th class="col-md-1">Latest Cutoff</th>
                      <th class="col-md-1">Status</th>
                      <th class="col-md-3">Tags</th>
                      <th class="col-md-1">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="service in filteredServices" :key="service.uuid" class="hover-box show-child-on-hover">
                      <td>
                        <a @click="editService(service)" style="cursor:pointer;">{{ service.name }}</a>
                        <span v-if="service.alias" class="fa fa-info-circle fa-right-pad text-muted" :title="service.alias"></span>
                      </td>
                      <td>{{ service.localIdentifier }}</td>
                      <td>
                        <span :title="getCcgName(service.ccgCode)">
                          {{ service.ccgCode }}
                        </span>
                      </td>
                      <td>{{ service.publisherConfigName }}</td>
                      <td>
                        <div v-if="service.systemStatuses">
                          <div v-for="status in service.systemStatuses" :key="status.systemName">
                            <span v-if="!showDateFilter" :title="formatLatestCutoffTooltip(service, status)">
                              {{ formatLatestCutoff(service, status) }}
                            </span>
                            <span v-if="showDateFilter" :title="formatLatestCutoffTooltip(service, status)">
                              {{ formatYYYYMMDDHHMM(status.lastReceivedExtractCutoff) }}
                            </span>
                          </div>
                        </div>
                      </td>
                      <td>
                        <div v-if="service.systemStatuses">
                          <div v-for="status in service.systemStatuses" :key="status.systemName">
                            <span :title="formatProcessingStatusTooltip(service, status)">
                              {{ formatProcessingStatus(service, status) }}
                            </span>
                            <span v-if="status.processingInError" class="fa fa-exclamation-triangle"></span>
                          </div>
                        </div>
                      </td>
                      <td>
                        {{ getTagStrPrefix(service) }}
                        <span v-if="getTagStr(service).length > tagStrDisplayLimit" class="fa fa-info-circle fa-right-pad text-muted" :title="getTagStr(service)"></span>
                      </td>
                      <td>
                        <button class="p-button p-button-secondary p-button-sm child-to-show" @click="viewExchanges(service)">Exchanges</button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as servicesService from '@/services/services.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'

const formatYYYYMMDDHHMM = DateTimeFormatter.formatYYYYMMDDHHMM

const services = ref([])
const filteredServices = ref([])
const tagStrDisplayLimit = 35
const cachedTagStrs = ref({})
const tagNames = ref([])
const publisherConfigNames = ref([])
const ccgCodes = ref([])
const ccgNameCache = ref({})

const serviceNameFilter = ref(null)
const serviceNameSearchIncludeTags = ref(false)
const serviceNameSearchSpecificTag = ref('')
const publisherConfigFilter = ref(null)
const statusFilter = ref(null)
const ccgCodeFilterStr = ref(null)
const ccgCodeFilterRegex = ref(null)
const ccgCodeFilterIsRegex = ref(false)
const lastDataFilter = ref(null)
const publisherModeFilter = ref(null)
const hideClosedFilter = ref(true)
const showDateFilter = ref(false)
const sortFilter = ref('NameAsc')

onMounted(async () => {
  refreshAllServices()
  await servicesService.getTagNamesFromCache()
  await servicesService.getCcgCodesFromCache()
  await servicesService.getPublisherConfigNamesFromCache()
  await servicesService.getCcgNamesFromCache()
})

async function refreshAllServices() {
  try {
    services.value = await servicesService.getAllServices()
    applyFiltering()
  } catch (error) {
    console.error('Failed to load services', error)
  }
}

function addService() {
  console.log('Add new service - to be implemented')
}

function editService(service) {
  console.log('Edit service', service.uuid)
}

function viewExchanges(service) {
  console.log('View exchanges for', service.name)
}

function clearFilters() {
  serviceNameFilter.value = null
  serviceNameSearchIncludeTags.value = false
  serviceNameSearchSpecificTag.value = ''
  publisherConfigFilter.value = null
  statusFilter.value = null
  ccgCodeFilterStr.value = null
  ccgCodeFilterRegex.value = null
  ccgCodeFilterIsRegex.value = false
  lastDataFilter.value = null
  publisherModeFilter.value = null
  hideClosedFilter.value = true
  sortFilter.value = 'NameAsc'
  applyFiltering()
}

function applyFiltering() {
  if (!services.value) return

  let filtered = []

  let validNameFilterRegex
  if (serviceNameFilter.value) {
    try {
      new RegExp(serviceNameFilter.value)
      validNameFilterRegex = serviceNameFilter.value.toLowerCase().trim()
    } catch (e) {
    }
  }

  let validParentFilterRegex
  if (ccgCodeFilterIsRegex.value && ccgCodeFilterRegex.value) {
    try {
      new RegExp(ccgCodeFilterRegex.value)
      validParentFilterRegex = ccgCodeFilterRegex.value.toLowerCase()
    } catch (e) {
    }
  }

  let latestCutoffLimit
  const now = new Date().getTime()
  const dayDuration = 1000 * 60 * 60 * 24

  if (lastDataFilter.value) {
    if (lastDataFilter.value === '1d') {
      latestCutoffLimit = now - dayDuration
    } else if (lastDataFilter.value === '2d') {
      latestCutoffLimit = now - (dayDuration * 2)
    } else if (lastDataFilter.value === '1wk') {
      latestCutoffLimit = now - (dayDuration * 7)
    } else if (lastDataFilter.value === '2wk') {
      latestCutoffLimit = now - (dayDuration * 14)
    } else if (lastDataFilter.value === 'older') {
      latestCutoffLimit = now - (dayDuration * 14)
    }
  }

  for (const service of services.value) {
    if (publisherConfigFilter.value) {
      const publisherConfigName = service.publisherConfigName
      if (publisherConfigFilter.value === 'NoPublisher') {
        if (publisherConfigName) continue
      } else {
        if (!publisherConfigName || publisherConfigName !== publisherConfigFilter.value) continue
      }
    }

    if (!ccgCodeFilterIsRegex.value && ccgCodeFilterStr.value) {
      const ccgCode = service.ccgCode
      if (!ccgCode || ccgCode !== ccgCodeFilterStr.value) continue
    }

    if (ccgCodeFilterIsRegex.value && validParentFilterRegex) {
      const ccgCode = service.ccgCode
      if (!ccgCode || !ccgCode.toLowerCase().match(validParentFilterRegex)) continue
    }

    if (statusFilter.value) {
      const desiredVal = getStatusValue(statusFilter.value)
      const statusVal = getSortingStatusValue(service)
      if (statusVal !== desiredVal) continue
    }

    if (publisherModeFilter.value) {
      let include = false
      if (service.systemStatuses) {
        for (const systemStatus of service.systemStatuses) {
          if (systemStatus.publisherMode === publisherModeFilter.value) {
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
          if (lastDataFilter.value === 'older') {
            if (cutoff < latestCutoffLimit) include = true
          } else {
            if (cutoff > latestCutoffLimit) include = true
          }
        }
      }
      if (!include) continue
    }

    if (hideClosedFilter.value) {
      if (service.tags && service.tags.hasOwnProperty('Closed')) continue
    }

    if (serviceNameSearchIncludeTags.value && serviceNameSearchSpecificTag.value) {
      if (!service.tags || !service.tags.hasOwnProperty(serviceNameSearchSpecificTag.value)) continue
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

      if (!include && service.tags && serviceNameSearchIncludeTags.value) {
        const tagNames = Object.keys(service.tags)
        for (const tagName of tagNames) {
          const tagValue = service.tags[tagName]
          if (!serviceNameSearchSpecificTag.value || tagName === serviceNameSearchSpecificTag.value) {
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

  if (sortFilter.value) {
    if (sortFilter.value === 'IDAsc' || sortFilter.value === 'IDDesc') {
      filtered.sort((a, b) => (a.localIdentifier || '').localeCompare(b.localIdentifier || ''))
      if (sortFilter.value === 'IDDesc') filtered.reverse()
    } else if (sortFilter.value === 'ParentAsc' || sortFilter.value === 'ParentDesc') {
      filtered.sort((a, b) => (a.ccgCode || '').localeCompare(b.ccgCode || ''))
      if (sortFilter.value === 'ParentDesc') filtered.reverse()
    } else if (sortFilter.value === 'PublisherConfigAsc' || sortFilter.value === 'PublisherConfigDesc') {
      filtered.sort((a, b) => (a.publisherConfigName || '').localeCompare(b.publisherConfigName || ''))
      if (sortFilter.value === 'PublisherConfigDesc') filtered.reverse()
    } else if (sortFilter.value === 'LastDataAsc' || sortFilter.value === 'LastDataDesc') {
      filtered.sort((a, b) => getSortingDate(b) - getSortingDate(a))
      if (sortFilter.value === 'LastDataDesc') filtered.reverse()
    } else if (sortFilter.value === 'StatusAsc' || sortFilter.value === 'StatusDesc') {
      filtered.sort((a, b) => getSortingStatusValue(a) - getSortingStatusValue(b))
      if (sortFilter.value === 'StatusDesc') filtered.reverse()
    }
  }

  filteredServices.value = filtered
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

function formatLatestCutoff(service, status) {
  if (status.cachedLastDataDateDesc) return status.cachedLastDataDateDesc

  let ret = ''
  if (service.systemStatuses.length > 1) {
    ret += status.systemName + ': '
  }

  if (status.lastReceivedExtractCutoff) {
    const lastDate = new Date(status.lastReceivedExtractCutoff)
    const today = new Date()
    ret += DateTimeFormatter.getDateDiffDesc(lastDate, today, 2)
  } else {
    ret += 'n/a'
  }

  status.cachedLastDataDateDesc = ret
  return ret
}

function formatLatestCutoffTooltip(service, status) {
  if (status.lastReceivedExtractCutoff) {
    const lastDate = new Date(status.lastReceivedExtractCutoff)
    const lastDataReceived = new Date(status.lastReceivedExtract)
    return 'Last data received ' + formatDate(lastDataReceived) + ' for ' + formatDate(lastDate)
  } else {
    return 'No data received'
  }
}

function formatProcessingStatusTooltip(service, status) {
  let ret = ''

  if (status.publisherMode) {
    if (status.publisherMode === 'Publisher_Draft') ret += '(Draft - new data will be rejected by messaging API) '
    else if (status.publisherMode === 'Publisher_Auto_Fail') ret += '(Auto-fail - inbound transform will automatically fail) '
    else if (status.publisherMode === 'Publisher_Bulk') ret += '(Bulk - exchanges will be routed to bulk queues) '
  }

  if (status.lastProcessedExtract) {
    const d = new Date(status.lastProcessedExtract)
    ret += 'Last successfully processed on ' + formatDate(d)

    if (status.lastProcessedExtractCutoff) {
      const d2 = new Date(status.lastProcessedExtractCutoff)
      ret += ' for ' + formatDate(d2)
    }
  } else {
    ret += 'Not successfully processed any data yet'
  }

  return ret
}

function formatProcessingStatus(service, status) {
  let ret = ''

  if (status.publisherMode) {
    if (status.publisherMode === 'Publisher_Draft') ret += '(D) '
    else if (status.publisherMode === 'Publisher_Auto_Fail') ret += '(AF) '
    else if (status.publisherMode === 'Publisher_Bulk') ret += '(B) '
  }

  if (status.processingInError) ret += 'ERROR'
  else if (status.processingUpToDate) ret += 'OK'
  else if (status.lastReceivedExtract) ret += 'Behind'
  else ret += 'No data'

  if (status.processingInError || !status.processingUpToDate) {
    if (status.lastProcessedExtractCutoff) {
      if (!status.cachedLastProcessedDiffDesc) {
        const d = new Date(status.lastProcessedExtractCutoff)
        const today = new Date()
        status.cachedLastProcessedDiffDesc = DateTimeFormatter.getDateDiffDesc(d, today, 2)
      }
      ret += ' (' + status.cachedLastProcessedDiffDesc + ')'
    }
  }

  return ret
}

function getCcgName(ccgCode) {
  return servicesService.getCcgName(ccgCode)
}

function getTagStrPrefix(service) {
  const str = getTagStr(service)
  if (str && str.length > tagStrDisplayLimit) return str.substr(0, tagStrDisplayLimit) + '...'
  return str
}

function getTagStr(service) {
  if (!cachedTagStrs.value[service.uuid]) {
    const list = tagNames.value
    let ret = ''
    if (service.tags && list) {
      for (const tagName of list) {
        if (service.tags.hasOwnProperty(tagName)) {
          const tagValue = service.tags[tagName]
          if (ret.length > 0) ret += ', '
          ret += tagName
          if (tagValue) ret += ' ' + tagValue
        }
      }
    }
    cachedTagStrs.value[service.uuid] = ret
  }
  return cachedTagStrs.value[service.uuid]
}

function formatDate(d) {
  if (!d) return '<missing date>'
  return d.getFullYear() + '-' + ('0' + (d.getMonth() + 1)).slice(-2) + '-' + ('0' + d.getDate()).slice(-2) + ' ' + ('0' + d.getHours()).slice(-2) + ':' + ('0' + d.getMinutes()).slice(-2)
}

function saveToCsv() {
  const lines = []
  let line = '"Name","ID","Parent","Latest Data Cutoff","Status","Tags"'
  lines.push(line)

  for (const service of filteredServices.value) {
    const cols = [service.name, service.localIdentifier, service.ccgCode, '', '', getTagStr(service)]

    if (service.systemStatuses) {
      for (const status of service.systemStatuses) {
        const lastDataStr = formatLatestCutoff(service, status)
        const statusStr = formatProcessingStatus(service, status)
        cols[3] = lastDataStr
        cols[4] = statusStr
        line = '"' + cols.join('","') + '"'
        lines.push(line)
      }
    } else {
      line = '"' + cols.join('","') + '"'
      lines.push(line)
    }
  }

  const csvStr = lines.join('\r\n')
  const blob = new Blob([csvStr], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'Services.csv'
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<template>
  <div class="module">
    <div class="module-heading">
      Publisher Errors {{ getDateDiffSimpleFromNow(statusLastRefreshed) }}
      <span v-if="transformErrorSummaries && filteredErrorSummaries">
        (showing {{ filteredErrorSummaries.length }} of {{ transformErrorSummaries.length }})
      </span>
      <div class="pull-right">
        <button class="p-button p-button-secondary p-button-sm" @click="clearFilters" title="Clear Filters">Reset Filters</button>
        <button class="p-button p-button-secondary p-button-sm" @click="checkAll" title="Check All Services">Check All</button>
        <button class="p-button p-button-success p-button-sm" :disabled="busyPosting" @click="requeueChecked" title="Re-queue Checked">Re-queue Checked ({{ getCheckedCount }} selected)</button>
        <button class="p-button p-button-success p-button-sm" @click="refreshSummaries" title="Refresh">Refresh</button>
      </div>
    </div>
    <div class="module-body">
      <div class="module" style="margin-bottom: 1rem;">
        <div class="module-body">
          <div role="form">
            <div class="form-group">
              <div class="col-sm-2">
                <label for="serviceNameFilter" class="control-label">Name/ID</label>
                <div class="pull-right">
                  <label for="includeTags" class="control-label">Match tags</label>
                  <input type="checkbox" id="includeTags" v-model="fs.serviceNameSearchIncludeTags" @change="applyFiltering">
                  <span v-if="fs.serviceNameSearchIncludeTags">
                    <select id="specificTag" v-model="fs.serviceNameSearchSpecificTag" @change="applyFiltering">
                      <option value="">All Tags</option>
                      <option v-for="tagName of tagNames" :key="tagName" :value="tagName">{{ tagName }}</option>
                    </select>
                  </span>
                </div>
                <input type="text" class="form-control" id="serviceNameFilter" placeholder="Regex" v-model="fs.serviceNameFilter" @input="applyFiltering">
              </div>
              <div class="col-sm-2">
                <label for="parentFilter" class="control-label">Parent</label>
                <div class="pull-right">
                  <label for="parentIsRegex" class="control-label">Regex</label>
                  <input type="checkbox" id="parentIsRegex" v-model="fs.ccgCodeFilterIsRegex" @change="applyFiltering">
                </div>
                <select v-if="!fs.ccgCodeFilterIsRegex" id="parentFilter" class="form-control" v-model="fs.ccgCodeFilterStr" @change="applyFiltering">
                  <option value=""></option>
                  <option v-for="ccgCode of ccgCodes" :key="ccgCode" :value="ccgCode">{{ ccgCode }} {{ getCcgName(ccgCode) }}</option>
                </select>
                <input v-if="fs.ccgCodeFilterIsRegex" type="text" class="form-control" id="parentFilter" placeholder="Regex" v-model="fs.ccgCodeFilterRegex" @input="applyFiltering">
              </div>
              <div class="col-sm-2">
                <label for="publisherDbFilter" class="control-label">Publisher DB</label>
                <select id="publisherDbFilter" class="form-control" v-model="fs.publisherConfigFilter" @change="applyFiltering">
                  <option value=""></option>
                  <option v-for="configName of publisherConfigNames" :key="configName" :value="configName">{{ configName }}</option>
                  <option value="NoPublisher">No publisher set</option>
                </select>
              </div>
              <div class="col-sm-1">
                <label for="latestCutoffFilter" class="control-label">Latest Cutoff</label>
                <select id="latestCutoffFilter" class="form-control" v-model="fs.lastDataFilter" @change="applyFiltering">
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
                <select id="publisherModeFilter" class="form-control" v-model="fs.publisherModeFilter" @change="applyFiltering">
                  <option value=""></option>
                  <option value="Publisher_Draft">Draft</option>
                  <option value="Publisher_Normal">Normal</option>
                  <option value="Publisher_Auto_Fail">Auto-fail</option>
                  <option value="Publisher_Bulk">Bulk</option>
                </select>
              </div>
              <div class="col-sm-1">
                <label for="sortFilter" class="control-label">Sorting</label>
                <select id="sortFilter" class="form-control" v-model="fs.sortFilter" @change="applyFiltering">
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
                </select>
              </div>
              <div class="col-sm-1">
                <label for="hideClosedFilter" class="control-label">Hide Closed</label>
                <input type="checkbox" id="hideClosedFilter" v-model="fs.hideClosedFilter" @change="applyFiltering">
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="container-fluid">
        <div class="row">
          <div class="col-md-6">
            <div class="table">
              <table class="table table-striped table-condensed">
                <thead>
                  <tr>
                    <th class="col-md-1"></th>
                    <th class="col-md-3">Name</th>
                    <th class="col-md-1">ID</th>
                    <th class="col-md-2">Tags</th>
                    <th class="col-md-3">System</th>
                    <th class="col-md-1">Exchanges in Error</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="summary in filteredErrorSummaries" :key="summary.service.uuid + '_' + summary.systemId" class="hover-box show-child-on-hover" @click="selectSummary(summary)" :class="{ 'selection': summary === selectedSummary }">
                    <td>
                      <input type="checkbox" v-model="summary.checked" @click.stop>
                    </td>
                    <td>
                      <span style="cursor:pointer;">{{ summary.service.name }}</span>
                    </td>
                    <td>{{ summary.service.localIdentifier }}</td>
                    <td>
                      {{ getTagStrPrefix(summary.service) }}
                      <span v-if="getTagStr(summary.service).length > tagStrDisplayLimit" class="fa fa-info-circle fa-right-pad text-muted" :title="getTagStr(summary.service)"></span>
                    </td>
                    <td>{{ summary.systemName }}</td>
                    <td>{{ summary.countExchanges }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="col-md-6" v-if="selectedSummary">
            <div class="module" style="margin-bottom: 1rem;">
              <div class="module-heading">
                Errors for {{ selectedSummary.service.name }} {{ selectedSummary.service.localIdentifier }} {{ selectedSummary.service.uuid }}
                <div class="pull-right">
                  <button class="p-button p-button-success p-button-sm" @click="rerunAll(selectedSummary)" :disabled="busyPosting" title="Re-queue">Re-queue</button>
                </div>
              </div>
              <div class="module-body" style="overflow:hidden; height:100%;">
                <div v-if="selectedSummary.service.notes">
                  {{ selectedSummary.service.notes }}
                </div>
                <div v-if="selectExchangeErrorDetail">
                  <h4>Exchange {{ selectExchangeErrorDetail.exchangeId }}</h4>
                  <div v-if="selectExchangeErrorDetail.resubmitted">
                    <b>Has been resubmitted, so should be in inbound queue</b>
                  </div>
                  <code v-if="selectExchangeErrorDetail.lines && selectExchangeErrorDetail.lines.length > 0">
                    <div v-for="line of selectExchangeErrorDetail.lines" :key="line">
                      <div v-if="line.length > 0">{{ line }}</div>
                      <div v-if="line.length === 0">&nbsp;</div>
                    </div>
                  </code>
                  <div v-if="(!selectExchangeErrorDetail.lines || selectExchangeErrorDetail.lines.length === 0) && !selectExchangeErrorDetail.transformEnd">
                    <div>Previous errors not shown once the inbound transform has started</div>
                    <div>To view original errors, use the Services page</div>
                    <div>Transform started at {{ formatYYYYMMDDHHMMSS(selectExchangeErrorDetail.transformStart) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import * as exchangeAuditService from '@/services/exchangeAudit.service.js'
import * as servicesService from '@/services/services.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'

const getDateDiffSimpleFromNow = DateTimeFormatter.getDateDiffSimpleFromNow
const formatYYYYMMDDHHMMSS = DateTimeFormatter.formatYYYYMMDDHHMMSS
const fs = servicesService.filterState

const transformErrorSummaries = ref([])
const selectedSummary = ref(null)
const statusLastRefreshed = ref(null)
const busyPosting = ref(false)
const selectExchangeErrorDetail = ref(null)
const filteredErrorSummaries = ref([])
const tagStrDisplayLimit = 10
const cachedTagStrs = ref({})
const tagNames = ref([])
const publisherConfigNames = ref([])
const ccgCodes = ref([])
const ccgNameCache = ref({})

onMounted(async () => {
  tagNames.value = await servicesService.getTagNamesFromCache()
  ccgCodes.value = await servicesService.getCcgCodesFromCache()
  publisherConfigNames.value = await servicesService.getPublisherConfigNamesFromCache()
  ccgNameCache.value = await servicesService.getCcgNamesFromCache()
  await refreshSummaries()
})

async function refreshSummaries() {
  statusLastRefreshed.value = new Date()
  transformErrorSummaries.value = null
  selectedSummary.value = null
  selectExchangeErrorDetail.value = null

  try {
    const result = await exchangeAuditService.getTransformErrorSummaries()
    transformErrorSummaries.value = (result || []).sort((a, b) => a.service.name.toLowerCase().localeCompare(b.service.name.toLowerCase()))
    applyFiltering()
    if (result.length === 0) {
      alert('No transform errors found')
    }
  } catch (error) {
    console.error('Failed to load transform errors', error)
  }
}

function selectSummary(summary) {
  selectedSummary.value = summary
  selectExchangeErrorDetail.value = null
  loadExchange(summary.exchangeIds[0])
}

async function loadExchange(exchangeId) {
  if (!selectedSummary.value || !exchangeId) return
  try {
    const data = await exchangeAuditService.getInboundTransformAudits(
      selectedSummary.value.service.uuid,
      selectedSummary.value.systemId,
      exchangeId
    )
    selectExchangeErrorDetail.value = data && data.length > 0 ? data[0] : null
  } catch (error) {
    console.error('Failed to retrieve transform audit', error)
  }
}

async function rerunAll(summary) {
  busyPosting.value = true
  try {
    await exchangeAuditService.rerunAllExchangesInError(summary.service.uuid, summary.systemId)
    alert('Successfully posted to exchange')
    await refreshSummaries()
  } catch (error) {
    console.error('Failed to post to exchange', error)
    alert('Failed to post to exchange: ' + (error.response?.data || error.message))
  } finally {
    busyPosting.value = false
  }
}

function applyFiltering() {
  if (!transformErrorSummaries.value) return
  filteredErrorSummaries.value = []

  const services = []
  const hmErrorsByServiceId = {}

  for (const summary of transformErrorSummaries.value) {
    const service = summary.service
    if (!services.find(s => s.uuid === service.uuid)) {
      services.push(service)
    }
    if (!hmErrorsByServiceId[service.uuid]) {
      hmErrorsByServiceId[service.uuid] = []
    }
    hmErrorsByServiceId[service.uuid].push(summary)
  }

  const filteredServices = servicesService.applyFiltering(services, true)

  for (const service of filteredServices) {
    const errorsForService = hmErrorsByServiceId[service.uuid]
    if (errorsForService) {
      filteredErrorSummaries.value.push(...errorsForService)
    }
  }
}

function clearFilters() {
  servicesService.clearFilters()
  applyFiltering()
}

function checkAll() {
  if (!transformErrorSummaries.value) return
  for (const summary of transformErrorSummaries.value) {
    summary.checked = true
  }
}

const getCheckedCount = computed(() => {
  if (!filteredErrorSummaries.value) return 0
  let count = 0
  for (const summary of filteredErrorSummaries.value) {
    if (summary.checked) count++
  }
  return count
})

async function requeueChecked() {
  const checked = []
  for (const summary of filteredErrorSummaries.value) {
    if (summary.checked) checked.push(summary)
  }

  if (checked.length === 0) {
    alert('No services checked')
    return
  }

  busyPosting.value = true
  try {
    await exchangeAuditService.requeueServicesInError(checked)
    alert('Successfully posted to exchange')
    await refreshSummaries()
  } catch (error) {
    console.error('Failed to post to exchange', error)
    alert('Failed to post to exchange: ' + (error.response?.data || error.message))
  } finally {
    busyPosting.value = false
  }
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
</script>

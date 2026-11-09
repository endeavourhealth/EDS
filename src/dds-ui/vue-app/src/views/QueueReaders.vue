<template>
  <div class="module">
    <!-- Header -->
    <div class="module-heading">
      Queue Reader Status {{ getDateDiffSimpleFromNow(statusLastRefreshed) }}
      <div class="pull-right">
        <div class="form-inline">
          <label for="showExecutionTime" class="control-label">Execution Time</label>
          <input type="checkbox" id="showExecutionTime" v-model="showExecutionTime">

          <label for="showOdsCode" class="control-label">ODS Code</label>
          <input type="checkbox" id="showOdsCode" v-model="showOdsCode">

          <label for="showPublisherConfig" class="control-label">Publisher Config</label>
          <input type="checkbox" id="showPublisherConfig" v-model="showPublisherConfig">

          <label for="showDataDate" class="control-label">Data Date</label>
          <input type="checkbox" id="showDataDate" v-model="showDataDate">

          <label for="showJarDate" class="control-label">Jar Date</label>
          <input type="checkbox" id="showJarDate" v-model="showJarDate">

          <label for="showStartDate" class="control-label">Start Date</label>
          <input type="checkbox" id="showStartDate" v-model="showStartDate">

          <label for="showMissingQueueReadersOnEmptyQueues" class="control-label">Warnings on Empty Queues</label>
          <input type="checkbox" id="showMissingQueueReadersOnEmptyQueues" v-model="showMissingQueueReadersOnEmptyQueues">

          <button class="p-button p-button-success p-button-sm" :disabled="refreshingQueueReaderStatus" @click="refreshStatus">
            Refresh<span class="fa fa-sm fa-refresh" v-if="refreshingQueueReaderStatus"></span>
          </button>
          <button class="p-button p-button-warning p-button-sm" :disabled="refreshingQueueReaderStatus" @click="deployAll" style="margin-left: 10px;">
            Deploy All
          </button>
        </div>
      </div>
    </div>

    <!-- Alert -->
    <div v-if="refreshingRabbitQueueStatus" class="alert alert-info">Loading RabbitMQ queue status...</div>
    <div v-else-if="!rabbitNodes?.length" class="alert alert-warning">No Rabbit nodes found.</div>

    <!-- Routing Table Section -->
    <div v-if="routingExchangeNames.length > 0" class="panel panel-primary">
      <div class="panel-heading">Queue Routing Overview</div>
      <div class="panel-body">
        <table class="table table-striped table-condensed">
          <thead>
            <tr>
              <th style="width: 150px;">Exchange</th>
              <th v-for="(desc, key) in routingQueueDescs" :key="key">{{ desc }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="exchangeName in routingExchangeNames" :key="exchangeName">
              <td><strong>{{ exchangeName }}</strong></td>
              <td v-for="(routingKey, index) in rowIndexes" :key="`${exchangeName}-${index}`">
                <template v-if="getRoutingKeyForExchangeAndIndex(exchangeName, index).length > 0">
                  <span class="p-tag p-tag-info">{{ getRoutingKeyForExchangeAndIndex(exchangeName, index)[0] }}</span>
                  <div style="font-size: 11px; color: #666;">
                    Queue Size: {{ getQueueSize(exchangeName, getRoutingKeyForExchangeAndIndex(exchangeName, index)[0]) ?? '-' }}
                  </div>
                </template>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Per-Host Status Section -->
    <div v-if="hostNames.length > 0" class="panel panel-info">
      <div class="panel-heading">
        Host Status ({{ hostNames.length }} hosts)
        <small class="pull-right text-muted">Colour-coded by host | Click status for exchange audit</small>
      </div>
      <div class="panel-body" v-if="!refreshingQueueReaderStatus && queueReaderStatusList?.length > 0">
        <div v-for="hostName in hostNames" :key="hostName" style="margin-bottom: 20px;">
          <!-- Host Header -->
          <div style="background-color: {{ hostNameColourMap[hostName] }}; padding: 10px; border-radius: 4px; margin-bottom: 10px;">
            <strong>{{ hostName }}</strong>
            <span class="pull-right">
              CPU: {{ getCpuUsage(hostName) || '-' }} | Memory: {{ getMemoryUsage(hostName) || '-' }}
              <br/>
              Allocation: <code style="font-family: monospace;">{{ getMemoryUsageGraph(hostName) }}</code>
            </span>
          </div>

          <!-- Status Table for Host -->
          <table class="table table-striped table-condensed" style="margin-left: 20px;">
            <thead>
              <tr>
                <th>Queue Name</th>
                <th>Status</th>
                <th>Last Updated</th>
                <th>Execution Time</th>
                <th>Memory</th>
                <th>Needs Restart?</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="status in getStatusesForHostName(hostName)" :key="status.queueName"
                  :style="getCellColourForHostName(status.hostName)">
                <td><strong>{{ status.queueName }}</strong>{{ getInstanceNumber(status) }}</td>
                <td>
                  <span v-if="status.isBusy" class="label label-warning">Busy</span>
                  <span v-else class="label label-success">Idle</span>
                  <span v-if="isStatusDead(status)" class="p-tag p-tag-danger" :title="status.isBusyDetail" style="margin-right: 5px;">{{ status.isBusyDetail }}</span>
                </td>
                <td>
                  <span v-if="isStatusDead(status)">
                    <span class="p-tag p-tag-danger" :title="status.isBusyDetail">
                      <i class="fa fa-warning" @click="warningButtonClicked(status)" style="cursor:pointer;"></i>
                      {{ getStatusAgeDesc(status) }}
                    </span>
                  </span>
                  <span v-else>
                    <span v-if="needsRestart(status)" class="fa fa-clock-o" :title="getNeedsRestartDesc(status)"></span>
                    <span v-if="status.isBusy" class="fa fa-play" :title="status.isBusyDetail" @click="playButtonClicked(status)" style="cursor:pointer;"></span>
                    <span v-if="!status.isBusy" class="fa fa-pause" title="Application running but idle"></span>
                    <span v-if="showExecutionTime">{{ getExecutionTime(status) }}</span>
                    <span v-if="showOdsCode && status.isBusyOdsCode">{{ status.isBusyOdsCode }}</span>
                    <span v-if="showPublisherConfig && status.isBusyPublisherConfigName">{{ status.isBusyPublisherConfigName }}</span>
                    <span v-if="showDataDate && status.isBusyDataDate">{{ status.isBusyDataDate }}</span>
                  </span>
                  <span v-if="showJarDate && status.dtJar">{{ formatYYYYMMDDHHMMSS(status.dtJar) }}</span>
                  <span v-if="showStartDate && status.dtStarted">{{ formatYYYYMMDDHHMMSS(status.dtStarted) }}</span>
                </td>
                <td>{{ status.maxHeapDesc || '-' }} / {{ status.currentHeapDesc || '-' }}</td>
                <td v-if="needsRestart(status)">
                  <span class="p-tag p-tag-warning">Yes</span>
                  <div style="font-size: 10px;">{{ getNeedsRestartDesc(status) }}</div>
                </td>
                <td v-else>-</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="refreshingQueueReaderStatus" class="text-center">
      <i class="fa fa-spinner fa-spin"></i> Loading queue reader status...
    </div>

    <!-- Empty State -->
    <div v-else-if="!queueReaderStatusList?.length && !refreshingQueueReaderStatus" class="alert alert-info text-center">
      No queue readers found. Deploy all or check RabbitMQ configuration.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as queueReaderApi from '@/services/queueReaderStatus.service.js'
import * as rabbitApi from '@/services/rabbit.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'

const formatHHMMSS = DateTimeFormatter.formatHHMMSS
const formatYYYYMMDDHHMMSS = DateTimeFormatter.formatYYYYMMDDHHMMSS
const getDateDiffSimpleFromNow = DateTimeFormatter.getDateDiffSimpleFromNow

const routingExchangeNames = ref([])
const rowIndexes = ref([])
const exchangeRoutingKeyMap = ref({})
const routingQueueDescs = ref({})
const rabbitNodes = ref([])
const statusLastRefreshed = ref(null)
const queueReaderStatusList = ref([])
const queueReaderStatusMapByQueue = ref({})
const queueReaderStatusMapByHostName = ref({})
const queueReaderCpuMapByHostName = ref({})
const queueReaderPhysicalMemoryMapByHostName = ref({})
const queueReaderMemoryGraphMapByHostName = ref({})
const refreshingQueueReaderStatus = ref(false)
const hostNames = ref([])
const rabbitQueueStatus = ref({})
const refreshingRabbitQueueStatus = ref(false)

const showExecutionTime = ref(false)
const showOdsCode = ref(false)
const showPublisherConfig = ref(false)
const showDataDate = ref(false)
const showJarDate = ref(false)
const showStartDate = ref(false)
const showMissingQueueReadersOnEmptyQueues = ref(false)

const presetColours = [
  '#f5b7b1', '#d2b4de', '#aed6f1', '#a2d9ce', '#abebc6', '#fad7a0 ', '#edbb99',
  '#e6b0aa', '#d7bde2', '#a9cce3', '#a3e4d7', '#a9dfbf', '#f9e79f', '#f5cba7',
  '#e5e7e9', '#ccd1d1', '#abb2b9', '#d5dbdb', '#aeb6bf', '#f7f9f9'
]

const hostNameColourMap = ref({})

onMounted(() => {
  cacheRabbitConfig()
  cacheRabbitNodes()
})

function cacheRabbitNodes() {
  rabbitApi.getRabbitNodes().then(data => {
    rabbitNodes.value = data
    refreshRabbitQueues()
  }).catch(err => console.error('Failed to load Rabbit nodes', err))
}

function refreshRabbitQueues() {
  if (!rabbitNodes.value?.length) return
  refreshingRabbitQueueStatus.value = true
  rabbitApi.getRabbitQueues(rabbitNodes.value[0].address).then(result => {
    rabbitQueueStatus.value = {}
    result.forEach(q => { rabbitQueueStatus.value[q.name] = q })
    refreshingRabbitQueueStatus.value = false
  }).catch(err => { console.error('Failed to load Rabbit queues', err); refreshingRabbitQueueStatus.value = false })
}

function refreshStatus() {
  refreshQueueReaders()
  refreshRabbitQueues()
}

function cacheRabbitConfig() {
  rabbitApi.getRoutings().then(result => {
    populateRoutingMap(result)
    refreshQueueReaders()
  }).catch(err => console.error('Failed to load Rabbit routing', err))
}

function populateRoutingMap(routings) {
  routingExchangeNames.value = []
  rowIndexes.value = []
  exchangeRoutingKeyMap.value = {}
  routingQueueDescs.value = {}

  routings.forEach(r => {
    if (!routingExchangeNames.value.includes(r.exchangeName)) {
      routingExchangeNames.value.push(r.exchangeName)
    }
    const keysForExchange = exchangeRoutingKeyMap.value[r.exchangeName] || []
    keysForExchange.push(r.routeKey)
    exchangeRoutingKeyMap.value[r.exchangeName] = keysForExchange.sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase()))
    routingQueueDescs.value[combineIntoQueueName(r.exchangeName, r.routeKey)] = r.description
  })

  const maxQueues = Math.max(...routingExchangeNames.value.map(ex => exchangeRoutingKeyMap.value[ex]?.length || 0), 0)
  rowIndexes.value = Array.from({ length: maxQueues }, (_, i) => i)
}

function refreshQueueReaders() {
  refreshingQueueReaderStatus.value = true
  statusLastRefreshed.value = new Date()
  queueReaderApi.getQueueReaderStatus('queuereader').then(result => {
    processQueueReaderResults(result)
    refreshingQueueReaderStatus.value = false
  }).catch(err => { console.error('Failed get Queue Reader status', err); refreshingQueueReaderStatus.value = false })
}

function processQueueReaderResults(results) {
  queueReaderStatusMapByQueue.value = {}
  queueReaderStatusMapByHostName.value = {}
  queueReaderCpuMapByHostName.value = {}
  queueReaderPhysicalMemoryMapByHostName.value = {}

  const maxInterestingInstanceNumberPerApp = {}
  results.forEach(r => {
    if (r.applicationInstanceNumber > 1 && !isStatusDead(r)) {
      const currentNum = maxInterestingInstanceNumberPerApp[r.applicationInstanceName]
      if (!currentNum || r.applicationInstanceNumber > currentNum) {
        maxInterestingInstanceNumberPerApp[r.applicationInstanceName] = r.applicationInstanceNumber
      }
    }
  })

  queueReaderStatusList.value = results.filter(r => {
    if (r.applicationInstanceNumber > 1) {
      const maxInteresting = maxInterestingInstanceNumberPerApp[r.applicationInstanceName]
      return !maxInteresting || r.applicationInstanceNumber <= maxInteresting
    }
    return true
  }).sort((a, b) => a.applicationInstanceName.localeCompare(b.applicationInstanceName) || a.applicationInstanceNumber - b.applicationInstanceNumber)

  const statusByTimestamp = [...queueReaderStatusList.value].sort((a, b) => new Date(a.timestmp) - new Date(b.timestmp))
  for (let i = statusByTimestamp.length - 1; i >= 0; i--) {
    const s = statusByTimestamp[i]
    if (!queueReaderCpuMapByHostName.value[s.hostName] && s.cpuLoad) queueReaderCpuMapByHostName.value[s.hostName] = s.cpuLoad + '%'
    if (!queueReaderPhysicalMemoryMapByHostName.value[s.hostName] && s.physicalMemoryDesc) queueReaderPhysicalMemoryMapByHostName.value[s.hostName] = s.physicalMemoryDesc
  }

  queueReaderStatusList.value.forEach(s => {
    const q = queueReaderStatusMapByQueue.value[s.queueName] || []
    q.push(s); queueReaderStatusMapByQueue.value[s.queueName] = q
    const h = queueReaderStatusMapByHostName.value[s.hostName] || []
    h.push(s); queueReaderStatusMapByHostName.value[s.hostName] = h
  })

  calculateDistinctHostNames()
  calculateMemoryGraphs()
  calculateColours()
}

function calculateDistinctHostNames() {
  hostNames.value = [...new Set(queueReaderStatusList.value.map(s => s.hostName))].sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase()))
}

function calculateMemoryGraphs() {
  queueReaderMemoryGraphMapByHostName.value = {}
  const memorySizeMap = {}, memoryUsedMap = {}
  queueReaderStatusList.value.forEach(s => {
    if (memorySizeMap[s.hostName] === undefined) memorySizeMap[s.hostName] = s.physicalMemoryMb
    if (!isStatusDead(s)) {
      memoryUsedMap[s.hostName] = (memoryUsedMap[s.hostName] || 0) + s.maxHeapMb
    }
  })

  hostNames.value.forEach(host => {
    const size = memorySizeMap[host], used = memoryUsedMap[host] || 0
    if (!size) return queueReaderMemoryGraphMapByHostName.value[host] = ''
    const max = Math.round(size / 1024), current = Math.round((used / size) * max)
    let graph = 'O'.repeat(Math.min(current, max)) + (current > max ? 'X'.repeat(current - max) : '')
    queueReaderMemoryGraphMapByHostName.value[host] = graph.padEnd(max, '_')
  })
}

function calculateColours() {
  hostNameColourMap.value = {}
  hostNames.value.forEach((h, i) => {
    hostNameColourMap.value[h] = i >= presetColours.length ? '#101010' : presetColours[i]
  })
}

function combineIntoQueueName(exchangeName, routingKey) { return exchangeName + '-' + routingKey }

function getStatusMemoryDesc(status) { return status.maxHeapDesc ? ` [${status.maxHeapDesc}]` : '' }
function isStatusDead(status) { return new Date(status.timestmp).getTime() < (statusLastRefreshed.value?.getTime() || 0) - 120000 }
function getStatusAgeDesc(status) { return DateTimeFormatter.getDateDiffDesc(new Date(status.timestmp), statusLastRefreshed.value, 2) }
function getExecutionTime(status) { return status.isBusySince ? DateTimeFormatter.getDateDiffDesc(new Date(status.isBusySince), new Date(status.timestmp), 1) : '' }
function getCpuUsage(hostName) {
  return queueReaderCpuMapByHostName.value[hostName]
}

function getMemoryUsage(hostName) {
  return queueReaderPhysicalMemoryMapByHostName.value[hostName]
}

function getMemoryUsageGraph(hostName) {
  return queueReaderMemoryGraphMapByHostName.value[hostName]
}

function getCellColourForHostName(hostName) { return { backgroundColor: hostNameColourMap.value[hostName] || '#e5e7e9' } }

function getStatusesForExchangeAndKey(exchangeName, routingKey) {
  const statuses = queueReaderStatusMapByQueue.value[combineIntoQueueName(exchangeName, routingKey)] || []
  return statuses.filter(s => shouldShowStatus(s))
}

function getQueueSize(exchangeName, routingKey) {
  const q = rabbitQueueStatus.value[combineIntoQueueName(exchangeName, routingKey)]
  return q?.messages_ready ?? null
}

function getStatusesForHostName(hostName) {
  const statuses = queueReaderStatusMapByHostName.value[hostName] || []
  return statuses.filter(s => shouldShowStatus(s))
}

function shouldShowStatus(status) {
  if (showMissingQueueReadersOnEmptyQueues.value) return true

  const queueSize = getQueueSizeForQueueName(status.queueName)
  if (queueSize && (queueSize > 0 || (queueSize === 0 && status.isBusy))) return true
  if (!isStatusDead(status)) return true
  return false
}

function getQueueSizeForQueueName(queueName) {
  if (!rabbitQueueStatus.value) return null
  const queueStatus = rabbitQueueStatus.value[queueName]
  if (queueStatus) return queueStatus.messages_ready
  return null
}

function playButtonClicked(status) {
  console.log('View exchanges for', status.isBusyOdsCode)
}

function warningButtonClicked(status) {
  console.log('View exchanges for warning', status.isBusyDetail)
}

function needsRestart(status) { return status.dtJar && status.dtStarted && status.dtJar > status.dtStarted }

function getInstanceNumber(status) { return status.applicationInstanceNumber > 1 ? ` #${status.applicationInstanceNumber}` : '' }

function getRoutingKeyForExchangeAndIndex(exchangeName, queueIndex) {
  const keys = exchangeRoutingKeyMap.value[exchangeName] || []
  return queueIndex < keys.length ? [keys[queueIndex]] : []
}

function deployAll() {
  queueReaderApi.queueReaderCommand().catch(err => console.error('Deploy failed', err))
}


</script>

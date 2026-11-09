<template>
  <div class="module">
    <!-- Header -->
    <div class="module-heading">
      SFTP Reader Status {{ getDateDiffSimpleFromNow(statusLastRefreshed) }}
      <div class="pull-right">
        <button class="p-button p-button-success p-button-sm" @click="refreshScreen" :disabled="refreshingStatus || refreshingApplicationStatus">Refresh All</button>
        <button class="p-button p-button-warning p-button-sm" @click="togglePauseAll" style="margin-left: 10px;">Pause All</button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="refreshingStatus || refreshingApplicationStatus" class="text-center">
      <i class="fa fa-spinner fa-spin"></i> Loading SFTP reader status...
    </div>

    <!-- Application Status Section -->
    <div v-else-if="applicationStatus?.length > 0" class="panel panel-info">
      <div class="panel-heading">SFTP Reader Application Status</div>
      <table class="table table-striped table-condensed">
        <thead>
          <tr><th>Instance</th><th>Status</th><th>Last Updated</th></tr>
        </thead>
        <tbody>
          <tr v-for="app in applicationStatus" :key="app.applicationInstanceName">
            <td>{{ app.applicationInstanceName }}{{ app.applicationInstanceNumber > 1 ? ` #${app.applicationInstanceNumber}` : '' }}</td>
            <td>
              <span v-if="isApplicationDead(app)" class="p-tag p-tag-danger">DEAD</span>
              <span v-else class="p-tag p-tag-success">Running</span>
            </td>
            <td>{{ getDateDiffSimpleFromNow(app.timestmp) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Configuration Status Section -->
    <div v-else-if="configurations?.length > 0" class="panel panel-primary">
      <div class="panel-heading">SFTP Reader Configurations</div>
      <table class="table table-striped table-condensed">
        <thead>
          <tr>
            <th>Configuration ID</th>
            <th>Friendly Name</th>
            <th>Last Checked</th>
            <th>Error Checking</th>
            <th>Last Extract</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="config in configurations" :key="config.configurationId">
            <td><strong>{{ config.configurationId }}</strong></td>
            <td>{{ config.friendlyName }}</td>
            <td>{{ statusMap[config.configurationId]?.latestPollingStart ? formatYYYYMMDDHHMMSS(statusMap[config.configurationId].latestPollingStart) : 'Never' }}</td>
            <td>{{ statusMap[config.configurationId]?.latestPollingException ? 'Y' : 'N' }}</td>
            <td>{{ statusMap[config.configurationId]?.latestBatchReceived ? formatYYYYMMDDHHMMSS(statusMap[config.configurationId].latestBatchReceived) : 'Never' }}</td>
            <td>
              <span v-if="statusMap[config.configurationId]?.warning" class="p-tag p-tag-warning">Warning</span>
              <span v-else class="p-tag p-tag-success">OK</span>
            </td>
            <td>
              <button @click="togglePause(config)" :class="statusMap[config.configurationId]?.paused ? 'p-button p-button-success p-button-sm' : 'p-button p-button-warning p-button-sm'" :label="statusMap[config.configurationId]?.paused ? 'Resume' : 'Pause'">{{ statusMap[config.configurationId]?.paused ? 'Resume' : 'Pause' }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Empty State -->
    <div v-else class="alert alert-info text-center">
      No SFTP reader configurations found.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as sftpReaderService from '@/services/sftpReader.service.js'
import * as queueReaderStatusService from '@/services/queueReaderStatus.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'

const formatYYYYMMDDHHMMSS = DateTimeFormatter.formatYYYYMMDDHHMMSS
const getDateDiffSimpleFromNow = DateTimeFormatter.getDateDiffSimpleFromNow

const configurations = ref([])
const statusMap = ref({})
const statusLastRefreshed = ref(null)
const applicationStatus = ref([])
const applicationStatusLastRefreshed = ref(null)
const refreshingStatus = ref(false)
const refreshingApplicationStatus = ref(false)

onMounted(() => { refreshScreen() })

async function refreshScreen() {
  await Promise.all([refreshInstances(true), refreshApplicationStatus()])
}

async function refreshInstances(fullRefresh) {
  if (fullRefresh) {
    configurations.value = null
    statusMap.value = {}
  }
  
  try {
    const result = await sftpReaderService.getSftpReaderInstances()
    configurations.value = [...result].sort((a, b) => a.configurationId.localeCompare(b.configurationId))
    
    if (fullRefresh) {
      for (const config of configurations.value) {
        refreshStatus(config)
      }
    }
  } catch (err) { console.error('Failed get SFTP Reader instances', err) }
}

async function refreshStatus(configuration) {
  const configurationId = configuration.configurationId
  try {
    const result = await sftpReaderService.getSftpReaderStatus(configurationId)
    calculateErrors(result)
    calculateIfWarning(result)
    statusMap.value[configurationId] = result
  } catch (err) { console.error(`Failed to get status for ${configurationId}`, err) }
}

async function refreshApplicationStatus() {
  refreshingApplicationStatus.value = true
  applicationStatusLastRefreshed.value = new Date()
  
  try {
    const result = await queueReaderStatusService.getQueueReaderStatus('sftpreader')
    applicationStatus.value = [...result].sort((a, b) => a.applicationInstanceName.localeCompare(b.applicationInstanceName))
  } catch (err) { console.error('Failed get application status', err) }
  finally { refreshingApplicationStatus.value = false }
}

function calculateErrors(status) {
  if (!status.completeBatchContents) status.completeBatchContents = []
  
  const ok = [], dpaError = [], error = []
  
  for (const content of status.completeBatchContents) {
    if (content.notified) {
      ok.push(content)
    } else {
      const errorMsg = content.error
      if (errorMsg && (errorMsg.includes('No DPA found') || errorMsg.includes('no DPA exists'))) {
        dpaError.push(content)
      } else {
        error.push(content)
      }
    }
  }
  
  status.okBatches = ok
  status.dpaErrorBatches = dpaError
  status.errorBatches = error
}

function calculateIfWarning(status) {
  if (isLastPollAttemptTooOld(status) || !status.latestPollingStart || status.latestPollingException ||
      isLastExtractTooOld(status) || !status.latestBatchId || status.errorBatches?.length > 0 || status.dpaErrorBatches?.length > 0) {
    status.warning = true
  } else {
    status.warning = false
  }
}

function isLastPollAttemptTooOld(status) {
  if (!status.latestPollingStart) return true
  const pollingFrequencySec = status.pollFrequencySeconds
  const lastPolledDiffMs = (statusLastRefreshed.value?.getTime() || Date.now()) - status.latestPollingStart
  return lastPolledDiffMs > pollingFrequencySec * 1000 * 2
}

function isLastExtractTooOld(status) {
  if (!status.latestBatchReceived) return true
  const dataFrequencyDays = status.dataFrequencyDays
  const lastExtractDiffMs = (statusLastRefreshed.value?.getTime() || Date.now()) - status.latestBatchReceived
  return lastExtractDiffMs > dataFrequencyDays * 24 * 60 * 60 * 1000 * 1.5
}

async function togglePause(configuration) {
  try {
    await sftpReaderService.togglePause(configuration.configurationId)
    refreshInstances(false)
  } catch (err) { console.error('Failed to toggle pause', err) }
}

async function togglePauseAll() {
  try {
    await sftpReaderService.togglePauseAll()
    refreshInstances(false)
  } catch (err) { console.error('Failed to toggle pause all', err) }
}

function isApplicationDead(status) {
  const statusTime = new Date(status.timestmp).getTime()
  const warningTime = (applicationStatusLastRefreshed.value?.getTime() || Date.now()) - 120000
  return statusTime < warningTime
}
</script>

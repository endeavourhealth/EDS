<template>
  <div class="module">
    <!-- Header -->
    <div class="module-heading">
      Remote Filing Status {{ getDateDiffSimpleFromNow(statusLastRefreshed) }}
      <div class="pull-right">
        <button class="p-button p-button-success p-button-sm" @click="refreshScreen" :disabled="refreshingStatus">Refresh All</button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="refreshingStatus" class="text-center">
      <i class="fa fa-spinner fa-spin"></i> Loading remote filing status...
    </div>

    <!-- Remote Filers Table -->
    <table v-else-if="remoteFilers?.length > 0" class="table table-striped table-condensed">
      <thead>
        <tr>
          <th>Filer Name</th>
          <th>Subscriber ID</th>
          <th>Status</th>
          <th>Last Checked</th>
          <th>Queued Messages</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="filer in remoteFilers" :key="filer.subscriberId">
          <td><strong>{{ filer.filerName }}</strong></td>
          <td>{{ filer.subscriberId }}</td>
          <td>
            <span v-if="isStale(filerStatusMap[filer.subscriberId])" class="p-tag p-tag-warning">STALE</span>
            <span v-else-if="filerStatusMap[filer.subscriberId]?.status === 'error'" class="p-tag p-tag-danger">ERROR</span>
            <span v-else class="p-tag p-tag-success">OK</span>
          </td>
          <td>{{ getDateDiffSimpleFromNow(filerStatusMap[filer.subscriberId]?.lastCheckedTime) }}</td>
          <td>{{ filerStatusMap[filer.subscriberId]?.queuedMessageCount || 0 }}</td>
          <td>
            <button v-if="filerStatusMap[filer.subscriberId]?.queuedMessages?.length > 0" 
                    class="p-button p-button-info p-button-sm" 
                    @click="viewQueuedMessage(filerStatusMap[filer.subscriberId].queuedMessages[0])">View Messages</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Empty State -->
    <div v-else class="alert alert-info text-center">
      No remote filers found.
    </div>

    <!-- Queued Message Dialog -->
    <div v-if="showTechnicalDetails" class="modal-overlay" style="display: flex; align-items: center; justify-content: center; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 1000;" @click.self="showTechnicalDetails = false">
      <div class="modal-content" style="background: white; border-radius: 6px; padding: 20px; max-width: 800px; width: 80%; max-height: 80vh; overflow-y: auto; position: relative;">
        <div class="modal-header" style="margin-bottom: 15px; font-weight: bold;">Queued Message Data</div>
        <pre style="white-space: pre-wrap; font-size: 12px;">{{ queuedMessageData }}</pre>
        <div class="modal-footer" style="margin-top: 15px; text-align: right;">
          <button class="p-button p-button-sm" @click="showTechnicalDetails = false">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as remoteFilingService from '@/services/remoteFiling.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'

const getDateDiffSimpleFromNow = DateTimeFormatter.getDateDiffSimpleFromNow

const remoteFilers = ref([])
const filerStatusMap = ref({})
const statusLastRefreshed = ref(null)
const refreshingStatus = ref(false)
const selectedFilerId = ref(null)
const queuedMessageData = ref('')
const showTechnicalDetails = ref(false)

onMounted(() => { refreshScreen() })

async function refreshScreen() {
  statusLastRefreshed.value = new Date()
  refreshingStatus.value = true
  try {
    const result = await remoteFilingService.getRemoteFilers()
    remoteFilers.value = result || []
    
    // Get status for each filer
    for (const filer of remoteFilers.value) {
      const statusResult = await remoteFilingService.getRemoteFilerStatus(filer.subscriberId)
      filerStatusMap.value[filer.subscriberId] = statusResult
    }
  } catch (err) { console.error('Failed to load remote filing data', err) }
  finally { refreshingStatus.value = false }
}

async function viewQueuedMessage(queuedMessageId) {
  try {
    const result = await remoteFilingService.getQueuedMessage(queuedMessageId)
    queuedMessageData.value = result?.data || 'No data available'
    showTechnicalDetails.value = true
  } catch (err) { console.error('Failed to load queued message', err) }
}

function isStale(status) {
  if (!status?.lastCheckedTime) return true
  const lastChecked = new Date(status.lastCheckedTime).getTime()
  const now = statusLastRefreshed.value?.getTime() || Date.now()
  return (now - lastChecked) > 3600000 // 1 hour threshold
}
</script>

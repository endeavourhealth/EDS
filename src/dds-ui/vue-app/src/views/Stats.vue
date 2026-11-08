<template>
  <div class="module">
    <!-- Header -->
    <div class="module-heading">
      Storage Statistics {{ getDateDiffSimpleFromNow(statusLastRefreshed) }}
      <div class="pull-right">
        <button class="p-button p-button-success p-button-sm" @click="refreshScreen" :disabled="refreshingStatus">
          Refresh All
        </button>
        <button class="p-button p-button-secondary p-button-sm" @click="downloadPatientCounts" style="margin-left: 10px;">
          Download Patient Counts
        </button>
      </div>
    </div>

    <!-- Filters -->
    <div class="form-inline" style="margin-bottom: 20px;">
      <label>Service ID:</label>
      <input v-model="serviceFilter" type="text" class="form-control input-sm" placeholder="Optional filter" />
      
      <label style="margin-left: 10px;">System ID:</label>
      <input v-model="systemFilter" type="text" class="form-control input-sm" placeholder="Optional filter" />
      
      <button @click="refreshScreen" :disabled="refreshingStatus" class="p-button p-button-primary p-button-sm" style="margin-left: 10px;">Apply</button>
    </div>

    <!-- Loading State -->
    <div v-if="refreshingStatus" class="text-center">
      <i class="fa fa-spinner fa-spin"></i> Loading storage statistics...
    </div>

    <!-- Storage Stats Table -->
    <table v-else-if="storageStats?.length > 0" class="table table-striped table-condensed">
      <thead>
        <tr>
          <th>Service</th>
          <th>System</th>
          <th>Total Records</th>
          <th>Storage Size</th>
          <th>Last Updated</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="stat in storageStats" :key="`${stat.service}-${stat.system}`">
          <td><strong>{{ stat.service }}</strong></td>
          <td>{{ stat.system || '-' }}</td>
          <td>{{ stat.totalRecords?.toLocaleString() || '0' }}</td>
          <td>{{ stat.storageSize || '-' }}</td>
          <td>{{ getDateDiffSimpleFromNow(stat.lastUpdated) }}</td>
        </tr>
      </tbody>
    </table>

    <!-- Empty State -->
    <div v-else class="alert alert-info text-center">
      No storage statistics found.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as statsService from '@/services/stats.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'

const getDateDiffSimpleFromNow = DateTimeFormatter.getDateDiffSimpleFromNow

const storageStats = ref([])
const statusLastRefreshed = ref(null)
const refreshingStatus = ref(false)
const serviceFilter = ref('')
const systemFilter = ref('')

onMounted(() => { refreshScreen() })

async function refreshScreen() {
  statusLastRefreshed.value = new Date()
  refreshingStatus.value = true
  try {
    const result = await statsService.getStorageStatistics(serviceFilter.value, systemFilter.value)
    storageStats.value = result || []
  } catch (err) { console.error('Failed to load storage statistics', err) }
  finally { refreshingStatus.value = false }
}

async function downloadPatientCounts() {
  try {
    await statsService.downloadPatientCounts()
  } catch (err) { console.error('Failed to download patient counts', err) }
}
</script>

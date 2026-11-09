<template>
  <div class="module">
    <!-- Header -->
    <div class="module-heading">
      Frailty API Status {{ getDateDiffSimpleFromNow(statusLastRefreshed) }}
      <div class="pull-right">
        <button class="p-button p-button-success p-button-sm" @click="refreshScreen" :disabled="refreshingStatus">Refresh All</button>
        <button class="p-button p-button-info p-button-sm" @click="downloadMonthlyStats" style="margin-left: 10px;">Download Monthly Stats</button>
      </div>
    </div>

    <!-- Filters -->
    <div style="margin-bottom: 20px;">
      <label>Minutes Back:</label>
      <input v-model.number="minutesBack" type="number" class="p-inputtext" style="width: 80px;" />
      
      <label style="margin-left: 10px;">Group By:</label>
      <select v-model="groupBy" class="p-inputtext" style="width: 150px;">
        <option value="subscriber">Subscriber</option>
        <option value="flagCode">Flag Code</option>
      </select>
      
      <button @click="refreshScreen" :disabled="refreshingStatus" class="p-button p-button-secondary p-button-sm" style="margin-left: 10px;">Apply</button>
    </div>

    <!-- Loading State -->
    <div v-if="refreshingStatus" class="text-center">
      <i class="fa fa-spinner fa-spin"></i> Loading Frailty API stats...
    </div>

    <!-- Stats Table -->
    <table v-else-if="recentStats?.length > 0" class="table table-striped table-condensed">
      <thead>
        <tr>
          <th>{{ groupBy === 'subscriber' ? 'Subscriber' : 'Flag Code' }}</th>
          <th>Total Messages</th>
          <th>Error Count</th>
          <th>Zero Message Minutes</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="stat in recentStats" :key="stat[groupBy]">
          <td><strong>{{ stat[groupBy] }}</strong></td>
          <td>{{ stat.totalMessages || 0 }}</td>
          <td>{{ stat.errorCount || 0 }}</td>
          <td>{{ stat.zeroMessageMinutes || 0 }} min</td>
          <td>
            <span v-if="hasOutage(stat)" class="p-tag p-tag-danger">OUTAGE DETECTED</span>
            <span v-else class="p-tag p-tag-success">OK</span>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Empty State -->
    <div v-else class="alert alert-info text-center">
      No Frailty API stats found.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as frailtyApiService from '@/services/frailtyApi.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'

const getDateDiffSimpleFromNow = DateTimeFormatter.getDateDiffSimpleFromNow

const recentStats = ref([])
const subscriberFlags = ref([])
const statusLastRefreshed = ref(null)
const refreshingStatus = ref(false)
const minutesBack = ref(60)
const groupBy = ref('subscriber')
const flagCode = ref('')

onMounted(() => { refreshScreen() })

async function refreshScreen() {
  statusLastRefreshed.value = new Date()
  refreshingStatus.value = true
  try {
    const result = await frailtyApiService.getFrailtyApiStats(minutesBack.value, groupBy.value, flagCode.value)
    recentStats.value = result || []
    
    // Get subscriber flags for filtering
    const flagsResult = await frailtyApiService.getFrailtySubscriberFlags()
    subscriberFlags.value = flagsResult || []
  } catch (err) { console.error('Failed to load Frailty API stats', err) }
  finally { refreshingStatus.value = false }
}

async function downloadMonthlyStats() {
  try {
    await frailtyApiService.downloadFrailtyMonthlyStats()
  } catch (err) { console.error('Failed to download monthly stats', err) }
}

function hasOutage(stats) {
  // Detect sustained outages (5+ min of zero messages)
  return stats?.zeroMessageMinutes >= 5
}
</script>

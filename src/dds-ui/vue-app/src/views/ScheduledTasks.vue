<template>
  <div class="module">
    <!-- Header -->
    <div class="module-heading">
      Scheduled Tasks Status {{ getDateDiffSimpleFromNow(statusLastRefreshed) }}
      <div class="pull-right">
        <button class="p-button p-button-success p-button-sm" @click="refreshScreen" :disabled="refreshingStatus">Refresh All</button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="refreshingStatus" class="text-center">
      <i class="fa fa-spinner fa-spin"></i> Loading scheduled tasks...
    </div>

    <!-- Tasks Table -->
    <table v-else-if="summary?.length > 0" class="table table-striped table-condensed">
      <thead>
        <tr>
          <th>Task Name</th>
          <th>Last Run Time</th>
          <th>Frequency (Minutes)</th>
          <th>Days Since Last Run</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="task in summary" :key="task.taskName">
          <td><strong>{{ task.taskName }}</strong></td>
          <td>{{ task.lastRunTime ? getDateDiffSimpleFromNow(task.lastRunTime) : 'Never' }}</td>
          <td>{{ getRuleFrequency(task.taskName) }}</td>
          <td>{{ getDaysSinceLastRun(task) }}</td>
          <td>
            <span v-if="isOverdue(task, getRuleForTask(task.taskName))" class="p-tag p-tag-danger">OVERDUE</span>
            <span v-else class="p-tag p-tag-success">OK</span>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Empty State -->
    <div v-else class="alert alert-info text-center">
      No scheduled tasks found.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as scheduledTasksService from '@/services/scheduledTasks.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'

const getDateDiffSimpleFromNow = DateTimeFormatter.getDateDiffSimpleFromNow

const rules = ref([])
const summary = ref([])
const statusLastRefreshed = ref(null)
const refreshingStatus = ref(false)

onMounted(() => { refreshScreen() })

async function refreshScreen() {
  statusLastRefreshed.value = new Date()
  refreshingStatus.value = true
  try {
    const rulesResult = await scheduledTasksService.getScheduledTaskRules()
    rules.value = rulesResult || []
    
    const summaryResult = await scheduledTasksService.getScheduledTaskSummary()
    summary.value = summaryResult || []
  } catch (err) { console.error('Failed to load scheduled tasks', err) }
  finally { refreshingStatus.value = false }
}

function isOverdue(task, rule) {
  if (!task?.lastRunTime || !rule?.frequencyMinutes) return false
  const lastRun = new Date(task.lastRunTime).getTime()
  const now = statusLastRefreshed.value?.getTime() || Date.now()
  const diffMs = now - lastRun
  const thresholdMs = rule.frequencyMinutes * 60 * 1000 * 1.5 // 1.5x frequency as threshold
  return diffMs > thresholdMs
}
</script>

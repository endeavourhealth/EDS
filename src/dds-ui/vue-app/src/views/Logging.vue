<template>
  <div class="module">
    <!-- Header -->
    <div class="module-heading">
      Monitoring {{ getDateDiffSimpleFromNow(statusLastRefreshed) }}
      <div class="pull-right">
        <button class="p-button p-button-success p-button-sm" @click="refreshScreen" :disabled="refreshingStatus">
          Refresh All
        </button>
      </div>
    </div>

    <!-- Filters -->
    <div class="form-inline" style="margin-bottom: 20px;">
      <label>Service ID:</label>
      <input v-model="serviceFilter" type="text" class="form-control input-sm" placeholder="Optional filter" />
      
      <label style="margin-left: 10px;">Level:</label>
      <select v-model="levelFilter" class="form-control input-sm" style="width: 150px;">
        <option value="">All Levels</option>
        <option value="ERROR">ERROR</option>
        <option value="WARN">WARN</option>
        <option value="INFO">INFO</option>
        <option value="DEBUG">DEBUG</option>
      </select>
      
      <button @click="refreshScreen" :disabled="refreshingStatus" class="p-button p-button-primary p-button-sm" style="margin-left: 10px;">Apply</button>
    </div>

    <!-- Loading State -->
    <div v-if="refreshingStatus" class="text-center">
      <i class="fa fa-spinner fa-spin"></i> Loading log events...
    </div>

    <!-- Log Events Table -->
    <table v-else-if="logEvents?.length > 0" class="table table-striped table-condensed">
      <thead>
        <tr>
          <th>Timestamp</th>
          <th>Level</th>
          <th>Service</th>
          <th>Message</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="event in logEvents" :key="event.id">
          <td>{{ getDateDiffSimpleFromNow(event.timestamp) }}</td>
          <td><span :class="'label label-' + (event.level === 'ERROR' ? 'danger' : event.level === 'WARN' ? 'warning' : 'info')">{{ event.level }}</span></td>
          <td>{{ event.service || '-' }}</td>
          <td>{{ event.message?.substring(0, 100) }}{{ event.message?.length > 100 ? '...' : '' }}</td>
          <td>
            <button v-if="event.stackTraceAvailable" @click="viewStackTrace(event.id)" class="p-button p-button-info p-button-sm">View Stack Trace</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Empty State -->
    <div v-else class="alert alert-info text-center">
      No log events found.
    </div>

    <!-- Stack Trace Dialog -->
    <div v-if="selectedEventId" class="modal-overlay" style="display: flex; align-items: center; justify-content: center; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 1000;" @click.self="selectedEventId = null">
      <div class="modal-content" style="background: white; border-radius: 6px; padding: 20px; max-width: 800px; width: 80%; max-height: 80vh; overflow-y: auto; position: relative;">
        <div class="modal-header" style="margin-bottom: 15px; font-weight: bold;">Stack Trace</div>
        <pre style="white-space: pre-wrap; font-size: 12px;">{{ stackTrace }}</pre>
        <div class="modal-footer" style="margin-top: 15px; text-align: right;">
          <button class="p-button p-button-sm" @click="selectedEventId = null">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as loggingService from '@/services/logging.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'

const getDateDiffSimpleFromNow = DateTimeFormatter.getDateDiffSimpleFromNow

const logEvents = ref([])
const selectedEventId = ref(null)
const stackTrace = ref('')
const statusLastRefreshed = ref(null)
const refreshingStatus = ref(false)
const serviceFilter = ref('')
const levelFilter = ref('')
const currentPage = ref(1)

onMounted(() => { refreshScreen() })

async function refreshScreen() {
  statusLastRefreshed.value = new Date()
  refreshingStatus.value = true
  try {
    const result = await loggingService.getLoggingEvents(serviceFilter.value, levelFilter.value, currentPage.value)
    logEvents.value = result || []
  } catch (err) { console.error('Failed to load log events', err) }
  finally { refreshingStatus.value = false }
}

async function viewStackTrace(eventId) {
  selectedEventId.value = eventId
  try {
    const result = await loggingService.getStackTrace(eventId)
    stackTrace.value = result?.stackTrace || 'No stack trace available'
  } catch (err) { console.error('Failed to load stack trace', err) }
}
</script>

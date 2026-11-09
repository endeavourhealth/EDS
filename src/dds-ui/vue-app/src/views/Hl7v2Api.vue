<template>
  <div class="module">
    <!-- Header -->
    <div class="module-heading">
      HL7v2 API Status {{ getDateDiffSimpleFromNow(statusLastRefreshed) }}
      <div class="pull-right">
        <button class="p-button p-button-success p-button-sm" @click="refreshScreen" :disabled="refreshingStatus">Refresh All</button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="refreshingStatus" class="text-center">
      <i class="fa fa-spinner fa-spin"></i> Loading HL7v2 API status...
    </div>

    <!-- Channel Status Section -->
    <div v-else-if="channels?.length > 0" class="panel panel-primary">
      <div class="panel-heading">HL7v2 API Channels</div>
      <table class="table table-striped table-condensed">
        <thead>
          <tr><th>Channel ID</th><th>Status</th></tr>
        </thead>
        <tbody>
          <tr v-for="channel in channels" :key="channel.id">
            <td><strong>{{ channel.id }}</strong></td>
            <td>
              <span v-if="channel.status === 'running'" class="p-tag p-tag-success">Running</span>
              <span v-else class="p-tag p-tag-danger">{{ channel.status }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Application Status Section -->
    <div v-if="applicationStatus?.length > 0" class="panel panel-info">
      <div class="panel-heading">Message Sender Application</div>
      <table class="table table-striped table-condensed">
        <thead>
          <tr><th>Instance</th><th>Status</th><th>Last Updated</th></tr>
        </thead>
        <tbody>
          <tr v-for="app in applicationStatus" :key="app.applicationInstanceName">
            <td>{{ app.applicationInstanceName }}{{ app.applicationInstanceNumber > 1 ? ` #${app.applicationInstanceNumber}` : '' }}</td>
            <td>
              <span v-if="!isDead(app)" class="p-tag p-tag-success">Running</span>
              <span v-else class="p-tag p-tag-danger">DEAD</span>
            </td>
            <td>{{ getDateDiffSimpleFromNow(app.timestmp) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Empty State -->
    <div v-else class="alert alert-info text-center">
      No HL7v2 API channels found.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as hl7v2ApiService from '@/services/hl7v2Api.service.js'
import * as queueReaderStatusService from '@/services/queueReaderStatus.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'

const getDateDiffSimpleFromNow = DateTimeFormatter.getDateDiffSimpleFromNow

const channels = ref([])
const applicationStatus = ref([])
const statusLastRefreshed = ref(null)
const refreshingStatus = ref(false)

onMounted(() => { refreshScreen() })

async function refreshScreen() {
  statusLastRefreshed.value = new Date()
  refreshingStatus.value = true
  try {
    const result = await hl7v2ApiService.getHl7v2ApiStatus()
    channels.value = result?.channels || []
    
    // Get application status for message sender
    const appResult = await queueReaderStatusService.getQueueReaderStatus('hl7v2_msg_sender')
    applicationStatus.value = appResult || []
  } catch (err) { console.error('Failed to load HL7v2 API status', err) }
  finally { refreshingStatus.value = false }
}

function isDead(app) {
  return app?.status === 'stopped'
}
</script>

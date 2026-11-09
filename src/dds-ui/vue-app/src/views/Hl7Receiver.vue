<template>
  <div class="module">
    <!-- Header -->
    <div class="module-heading">
      HL7 Receiver Status {{ getDateDiffSimpleFromNow(statusLastRefreshed) }}
      <div class="pull-right">
        <button class="p-button p-button-success p-button-sm" @click="refreshScreen" :disabled="refreshingStatus">Refresh All</button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="refreshingStatus" class="text-center">
      <i class="fa fa-spinner fa-spin"></i> Loading HL7 receiver status...
    </div>

    <!-- Channel Table -->
    <table v-else-if="channels?.length > 0" class="table table-striped table-condensed table-hover">
      <thead>
        <tr>
          <th>Channel ID</th>
          <th>Status</th>
          <th>Last Message Received</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="channel in channels" :key="channel.id">
          <td><strong>{{ channel.id }}</strong></td>
          <td>
            <span v-if="channel.status === 'running'" class="p-tag p-tag-success">Running</span>
            <span v-else-if="channel.status === 'paused'" class="p-tag p-tag-warning">Paused</span>
            <span v-else class="p-tag p-tag-danger">{{ channel.status }}</span>
          </td>
          <td>{{ channel.lastMessageReceived ? formatYYYYMMDDHHMMSS(channel.lastMessageReceived) : '-' }}</td>
          <td>
            <button v-if="channel.status === 'running'" class="p-button p-button-warning p-button-sm" @click="togglePause(channel.id, true)">Pause</button>
            <button v-else class="p-button p-button-success p-button-sm" @click="togglePause(channel.id, false)">Resume</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Empty State -->
    <div v-else class="alert alert-info text-center">
      No HL7 receiver channels found.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as hl7ReceiverApi from '@/services/hl7Receiver.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'

const formatYYYYMMDDHHMMSS = DateTimeFormatter.formatYYYYMMDDHHMMSS
const getDateDiffSimpleFromNow = DateTimeFormatter.getDateDiffSimpleFromNow

const channels = ref([])
const statusLastRefreshed = ref(null)
const refreshingStatus = ref(false)

onMounted(() => { refreshScreen() })

async function refreshScreen() {
  statusLastRefreshed.value = new Date()
  refreshingStatus.value = true
  try {
    const result = await hl7ReceiverApi.getHl7ReceiverChannelStatus()
    channels.value = result || []
  } catch (err) { console.error('Failed to load HL7 receiver channels', err) }
  finally { refreshingStatus.value = false }
}

async function togglePause(channelId, pause) {
  try {
    await hl7ReceiverApi.pauseHl7ReceiverChannel(channelId, pause)
    refreshScreen()
  } catch (err) { console.error('Failed to toggle pause', err) }
}
</script>

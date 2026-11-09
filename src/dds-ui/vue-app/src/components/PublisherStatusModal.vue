<template>
  <div v-if="visible" class="modal-overlay" style="display: flex; align-items: center; justify-content: center; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 1000;" @click.self="close">
    <div class="modal-content" style="background: white; border-radius: 6px; padding: 20px; max-width: 80%; width: 90%; max-height: 90vh; overflow-y: auto; position: relative;">
      <div class="modal-header" style="margin-bottom: 15px; font-weight: bold;">{{ publisherService.odsCode + ' ' + publisherService.name }}</div>
      <div class="row">
        <div class="col-md-12">
          <div v-for="tagName in getTagNames(publisherService)" :key="tagName">
            {{ tagName }} {{ getTagValueDesc(tagName, publisherService) }}
          </div>
          <div>&nbsp;</div>
        </div>
      </div>

      <div :class="getPanelClass(system)" v-for="system in publisherService.systemStatus" :key="system.uuid">
        <div class="panel-heading">
          {{ system.name }}
        </div>
        <div class="panel-body">
          <div v-if="system.processingInError">
            Inbound transform error: <b>{{ system.processingInErrorMessage }}</b>
            <span class="label label-danger" title="Inbound error">Error</span>
          </div>

          <div class="table">
            <table class="table table-striped table-condensed">
              <thead>
                <tr>
                  <th class="col-md-4">Event</th>
                  <th class="col-md-4">Last Timestamp</th>
                  <th class="col-md-4">Last Extract Cut-off</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Extract received</td>
                  <td>
                    <span v-if="system.lastReceivedTimestamp">{{ formatYYYYMMDDHHMM(system.lastReceivedTimestamp) }}</span>
                    <span v-else>Never</span>
                  </td>
                  <td>
                    <span v-if="system.lastReceivedExtractCutoff">{{ formatYYYYMMDDHHMM(system.lastReceivedExtractCutoff) }}</span>
                    <span v-else>Never</span>
                  </td>
                </tr>
                <tr>
                  <td>Inbound transform completed</td>
                  <td>
                    <span v-if="system.lastProcessedInTimestamp">{{ formatYYYYMMDDHHMM(system.lastProcessedInTimestamp) }}</span>
                    <span v-else>Never</span>
                  </td>
                  <td>
                    <span v-if="system.lastProcessedInExtractCutoff">{{ formatYYYYMMDDHHMM(system.lastProcessedInExtractCutoff) }}</span>
                    <span v-else>Never</span>
                    <span class="label label-danger" v-if="system.inboundBehindDays > 1" :title="system.inboundBehindWarning">Behind</span>
                    <span class="label label-warning" v-if="system.inboundBehindDays > 0 && system.inboundBehindDays <= 1" :title="system.inboundBehindWarning">Behind</span>
                  </td>
                </tr>
                <tr>
                  <td>Outbound transform completed</td>
                  <td>
                    <span v-if="system.lastProcessedOutTimestamp">{{ formatYYYYMMDDHHMM(system.lastProcessedOutTimestamp) }}</span>
                    <span v-else>Never</span>
                  </td>
                  <td>
                    <span v-if="system.lastProcessedOutExtractCutoff">{{ formatYYYYMMDDHHMM(system.lastProcessedOutExtractCutoff) }}</span>
                    <span v-else>Never</span>
                    <span class="label label-danger" v-if="system.outboundBehindDays > 1" :title="system.outboundBehindWarning">Behind</span>
                    <span class="label label-warning" v-if="system.outboundBehindDays > 0 && system.outboundBehindDays <= 1" :title="system.outboundBehindWarning">Behind</span>
                  </td>
                </tr>
                <tr v-if="system.writeOutError">
                  <td>Outbound write error</td>
                  <td>
                    {{ system.writeOutErrorMessage }}
                    <span class="label label-danger" title="Inbound error">Error</span>
                  </td>
                  <td></td>
                </tr>
                <tr>
                  <td>Outbound writing completed</td>
                  <td>
                    <span v-if="system.lastWriteOutTimestamp">{{ formatYYYYMMDDHHMM(system.lastWriteOutTimestamp) }}</span>
                    <span v-else>Never</span>
                  </td>
                  <td>
                    <span v-if="system.lastWriteOutExtractCutoff">{{ formatYYYYMMDDHHMM(system.lastWriteOutExtractCutoff) }}</span>
                    <span v-else>Never</span>
                    <span class="label label-danger" v-if="system.outboundWriteBehindDays > 1" :title="system.outboundWriteBehindWarning">Behind</span>
                    <span class="label label-warning" v-if="system.outboundWriteBehindDays > 0 && system.outboundWriteBehindDays <= 1" :title="system.outboundWriteBehindWarning">Behind</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="system.writeOutError">
            Outbound write error: <b>{{ system.writeOutErrorMessage }}</b>
            <span class="label label-danger" title="Outbound write error">Error</span>
          </div>
        </div>
      </div>
      <div class="modal-footer" style="margin-top: 15px; text-align: right;">
        <button type="button" class="p-button p-button-sm" @click="close">Close</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'
import * as servicesService from '@/services/services.service.js'

const formatYYYYMMDDHHMM = DateTimeFormatter.formatYYYYMMDDHHMM

const props = defineProps({
  publisherService: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close'])

const visible = ref(true)

function close() {
  visible.value = false
  emit('close')
}

function getTagNames(publisher) {
  const ret = []
  const tags = publisher.tags
  if (!tags) {
    return ret
  }

  const allTagNames = servicesService.getTagNamesFromCache()
  for (const tagName of allTagNames) {
    if (tags.hasOwnProperty(tagName)) {
      ret.push(tagName)
    }
  }

  return ret
}

function getTagValueDesc(tagName, publisher) {
  const tags = publisher.tags
  if (!tags) {
    return ''
  }

  const ret = tags[tagName]
  if (!ret) {
    ret = ''
  }
  return ret
}

function getPanelClass(systemStatus) {
  if (!systemStatus.lastReceivedExtractCutoff) {
    return 'panel panel-info'
  }

  if (systemStatus.processingInError
    || !systemStatus.lastProcessedInExtractCutoff
    || !systemStatus.lastProcessedOutExtractCutoff
    || systemStatus.writeOutError) {
    return 'panel panel-danger'
  }

  if (systemStatus.inboundBehindDays > 1
    || systemStatus.outboundBehindDays > 1
    || systemStatus.outboundWriteBehindDays > 1) {
    return 'panel panel-danger'
  }

  if (systemStatus.inboundBehindDays > 0
    || systemStatus.outboundBehindDays > 0
    || systemStatus.outboundWriteBehindDays > 0) {
    return 'panel panel-warning'
  }

  return 'panel panel-success'
}
</script>

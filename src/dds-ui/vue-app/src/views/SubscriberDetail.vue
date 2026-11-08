<template>
  <div class="module">
    <div class="module-heading">
      {{ subscriberName }} details
      <span v-if="statusLastRefreshed"> as of {{ formatHHMMSS(statusLastRefreshed) }}</span>
      <span v-if="status && filteredServices"> ({{ filteredServices.length }} shown of {{ status.publisherServices.length }})</span>
      <div class="pull-right">
        <div>
          <button class="p-button p-button-secondary p-button-sm" :disabled="refreshingStatus" @click="saveToCsv">Save to CSV</button>
          <button class="p-button p-button-success p-button-sm" :disabled="refreshingStatus" @click="refreshScreen">Refresh</button>
          <button class="p-button p-button-secondary p-button-sm" @click="close">Close</button>
        </div>
      </div>
    </div>
    <div class="module-body">
      <div class="module" style="margin-bottom: 1rem;">
        <div class="module-body">
          <div class="form-group">
            <div class="col-sm-2">
              <label for="publisherNameFilter" class="control-label">Name/ODS</label>
              <input type="text" id="publisherNameFilter" class="form-control" placeholder="Regex" v-model="subscribersService.publisherNameFilter" @input="applyFiltering">
            </div>

            <div class="col-sm-2">
              <label for="systemNameFilter" class="control-label">System</label>
              <select id="systemNameFilter" class="form-control" v-model="subscribersService.systemNameFilter" @change="applyFiltering">
                <option value=""></option>
                <option v-for="systemName in cachedSystemNames" :key="systemName" :value="systemName">{{ systemName }}</option>
              </select>
            </div>

            <div class="col-sm-2">
              <label for="statusFilter" class="control-label">State</label>
              <select id="statusFilter" class="form-control" v-model="subscribersService.statusFilter" @change="applyFiltering">
                <option value=""></option>
                <option value="up-to-date">Up-to-date and OK</option>
                <option value="inbound-error">Inbound Error</option>
                <option value="any-behind">Behind</option>
                <option value="severe-behind">Severely Behind</option>
                <option value="any-issue">Any Issue</option>
              </select>
            </div>

            <div class="col-sm-2">
              <label for="sortFilter" class="control-label">Sorting</label>
              <select id="sortFilter" class="form-control" v-model="subscribersService.sortFilter" @change="applyFiltering">
                <option value="NameAsc">Name Asc</option>
                <option value="NameDesc">Name Desc</option>
                <option value="IDAsc">ID Asc</option>
                <option value="IDDesc">ID Desc</option>
                <option value="LastDataAsc">Last Data Asc</option>
                <option value="LastDataDesc">Last Data Desc</option>
              </select>
            </div>

            <div class="col-sm-3">
              <div>
                <label for="showDatesFilter" class="control-label">Show Dates</label>
                <input type="checkbox" id="showDatesFilter" v-model="subscribersService.showDates" @change="applyFiltering">
              </div>
              <div>
                <label for="showFullSubscriberConfigDetails" class="control-label">Show Subscriber Configuration</label>
                <input type="checkbox" id="showFullSubscriberConfigDetails" v-model="subscribersService.showFullSubscriberConfigDetails" @change="applyFiltering">
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="module" style="margin-bottom: 1rem;" v-if="subscribersService.showFullSubscriberConfigDetails && status">
        <div class="module-body">
          <div class="form-group">
            <div class="row">
              <div class="col-sm-3">
                <label for="statusName" class="control-label">Name</label>
                <input type="text" id="statusName" name="statusName" class="form-control" disabled v-model="status.name">
              </div>
              <div class="col-sm-9">
                <label for="statusDescription" class="control-label">Description</label>
                <input type="text" id="statusDescription" name="statusDescription" class="form-control" disabled v-model="status.description">
              </div>
            </div>

            <div class="row">
              <div class="col-sm-3">
                <label for="statusLocation" class="control-label">Location</label>
                <input type="text" id="statusLocation" name="statusLocation" class="form-control" disabled v-model="status.subscriberLocation">
              </div>
              <div class="col-sm-3">
                <label for="statusRemoteSubscriberId" class="control-label">Remote Subscriber ID</label>
                <input type="text" id="statusRemoteSubscriberId" name="statusRemoteSubscriberId" class="form-control" disabled v-model="status.remoteSubscriberId">
              </div>
              <div class="col-sm-3">
                <label for="statusSchema" class="control-label">Schema</label>
                <input type="text" id="statusSchema" name="statusSchema" class="form-control" disabled v-model="status.schema">
              </div>
              <div class="col-sm-3">
                <label for="statusCohort" class="control-label">Cohort</label>
                <input type="text" id="statusCohort" name="statusCohort" class="form-control" disabled v-model="status.cohortDesc">
              </div>
            </div>

            <div class="row">
              <div class="col-sm-3">
                <label for="statusDeidentified" class="control-label">PI</label>
                <input type="text" id="statusDeidentified" name="statusDeidentified" class="form-control" disabled :value="!status.deidentified">
              </div>
              <div class="col-sm-3">
                <label for="statusExcludeTestPatients" class="control-label">Excludes Test Patients</label>
                <input type="text" id="statusExcludeTestPatients" name="statusExcludeTestPatients" class="form-control" disabled :value="status.excludeTestPatients">
              </div>
              <div class="col-sm-3">
                <label for="statusExcludePatientsWithoutNhsNumber" class="control-label">Exclude Records Without NHS Number</label>
                <input type="text" id="statusExcludePatientsWithoutNhsNumber" name="statusExcludePatientsWithoutNhsNumber" class="form-control" disabled :value="status.excludePatientsWithoutNhsNumber">
              </div>
              <div class="col-sm-3">
                <label for="statusExcludeNhsNumberRegex" class="control-label">Exclude NHS Numbers Regex</label>
                <input type="text" id="statusExcludeNhsNumberRegex" name="statusExcludeNhsNumberRegex" class="form-control" disabled v-model="status.excludeNhsNumberRegex">
              </div>
            </div>

            <div class="row">
              <div class="col-sm-3">
                <label for="statusSubscriberDatabase" class="control-label">Subscriber Database</label>
                <input type="text" id="statusSubscriberDatabase" name="statusSubscriberDatabase" class="form-control" disabled v-model="status.subscriberDatabase">
              </div>
              <div class="col-sm-3">
                <label for="statusSubscriberDatabaseName" class="control-label">Subscriber Database Name</label>
                <input type="text" id="statusSubscriberDatabaseName" name="statusSubscriberDatabaseName" class="form-control" disabled v-model="status.subscriberDatabaseName">
              </div>
              <div class="col-sm-3">
                <label for="statusSubscriberTransformDatabase" class="control-label">Subscriber Transform Database</label>
                <input type="text" id="statusSubscriberTransformDatabase" name="statusSubscriberTransformDatabase" class="form-control" disabled v-model="status.subscriberTransformDatabase">
              </div>
              <div class="col-sm-3">
                <label for="statusSubscriberTransformDatabaseName" class="control-label">Subscriber Transform Database Name</label>
                <input type="text" id="statusSubscriberTransformDatabaseName" name="statusSubscriberTransformDatabaseName" class="form-control" disabled v-model="status.subscriberTransformDatabaseName">
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="row">
        <div class="col-md-12">
          <div class="table">
            <table class="table table-striped table-condensed">
              <thead>
                <tr>
                  <th class="col-md-3">Publisher</th>
                  <th class="col-md-1">ODS Code</th>
                  <th class="col-md-1">System</th>
                  <th class="col-md-1">Last Received</th>
                  <th class="col-md-1">Last Data In</th>
                  <th class="col-md-1">Last Data Out</th>
                  <th class="col-md-1">Last Data Written</th>
                  <th class="col-md-1"></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="publisher in filteredServices" :key="publisher.uuid" class="hover-box show-child-on-hover">
                  <td>
                    <a @click="editPublisher(publisher.uuid)" style="cursor:pointer;">{{ publisher.name }}</a>
                    <span v-if="publisher.alias" class="fa fa-info-circle fa-right-pad text-muted" :title="publisher.alias"></span>
                  </td>
                  <td>{{ publisher.odsCode }}</td>
                  <td>
                    <div v-for="system in publisher.systemStatus" :key="system.uuid">
                      {{ system.name }}
                    </div>
                  </td>
                  <td>
                    <div v-for="system in publisher.systemStatus" :key="system.uuid">
                      <div v-if="subscribersService.showDates" :title="'Extract received ' + formatYYYYMMDDHHMM(system.lastReceivedTimestamp)">
                        {{ formatYYYYMMDDHHMM(system.lastReceivedExtractCutoff) }}
                      </div>
                      <div v-if="!subscribersService.showDates" :title="'Extract received ' + formatYYYYMMDDHHMM(system.lastReceivedTimestamp)">
                        {{ getDateDiff(system.lastReceivedExtractCutoff) }}
                      </div>
                    </div>
                  </td>
                  <td>
                    <div v-for="system in publisher.systemStatus" :key="system.uuid">
                      <span v-if="subscribersService.showDates" :title="'Transform completed ' + formatYYYYMMDDHHMM(system.lastProcessedInTimestamp)">
                        {{ formatYYYYMMDDHHMM(system.lastProcessedInExtractCutoff) }}
                      </span>
                      <span v-if="!subscribersService.showDates" :title="'Transform completed ' + formatYYYYMMDDHHMM(system.lastProcessedInTimestamp)">
                        {{ getDateDiff(system.lastProcessedInExtractCutoff) }}
                      </span>

                      <span class="p-tag p-tag-danger" v-if="system.processingInError" :title="'Inbound error: ' + system.processingInErrorMessage">Error</span>

                      <span class="p-tag p-tag-danger" v-if="system.inboundBehindDays > 1" :title="system.inboundBehindWarning">Behind</span>
                      <span class="p-tag p-tag-warning" v-if="system.inboundBehindDays > 0 && system.inboundBehindDays <= 1" :title="system.inboundBehindWarning">Behind</span>
                    </div>
                  </td>
                  <td>
                    <div v-for="system in publisher.systemStatus" :key="system.uuid">
                      <span v-if="subscribersService.showDates" :title="'Transform completed ' + formatYYYYMMDDHHMM(system.lastProcessedOutTimestamp)">
                        {{ formatYYYYMMDDHHMM(system.lastProcessedOutExtractCutoff) }}
                      </span>
                      <span v-if="!subscribersService.showDates" :title="'Transform completed ' + formatYYYYMMDDHHMM(system.lastProcessedOutTimestamp)">
                        {{ getDateDiff(system.lastProcessedOutExtractCutoff) }}
                      </span>

                      <span class="p-tag p-tag-danger" v-if="system.outboundBehindDays > 1" :title="system.outboundBehindWarning">Behind</span>
                      <span class="p-tag p-tag-warning" v-if="system.outboundBehindDays > 0 && system.outboundBehindDays <= 1" :title="system.outboundBehindWarning">Behind</span>
                    </div>
                  </td>
                  <td>
                    <div v-for="system in publisher.systemStatus" :key="system.uuid">
                      <span v-if="subscribersService.showDates" :title="'Transform completed ' + formatYYYYMMDDHHMM(system.lastWriteOutTimestamp)">
                        {{ formatYYYYMMDDHHMM(system.lastWriteOutExtractCutoff) }}
                      </span>
                      <span v-if="!subscribersService.showDates" :title="'Transform completed ' + formatYYYYMMDDHHMM(system.lastWriteOutTimestamp)">
                        {{ getDateDiff(system.lastWriteOutExtractCutoff) }}
                      </span>

                      <span class="p-tag p-tag-danger" v-if="system.writeOutError" :title="'Inbound error: ' + system.writeOutErrorMessage">Error</span>

                      <span class="p-tag p-tag-danger" v-if="system.outboundWriteBehindDays > 1" :title="system.outboundWriteBehindWarning">Behind</span>
                      <span class="p-tag p-tag-warning" v-if="system.outboundWriteBehindDays > 0 && system.outboundWriteBehindDays <= 1" :title="system.outboundWriteBehindWarning">Behind</span>
                    </div>
                  </td>
                  <td>
                    <button class="p-button p-button-secondary p-button-sm child-to-show" @click="viewPublisherExchanges(publisher.uuid)">Exchanges</button>
                    <button class="p-button p-button-secondary p-button-sm child-to-show" @click="viewPublisherDetails(publisher)">Status</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <PublisherStatusModal
      v-if="showPublisherModal"
      :publisher-service="selectedPublisher"
      @close="showPublisherModal = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as subscriberApi from '@/services/subscriber.service.js'
import { subscribersService } from '@/services/subscriber.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'
import PublisherStatusModal from '@/components/PublisherStatusModal.vue'

const route = useRoute()
const router = useRouter()

const formatYYYYMMDDHHMM = DateTimeFormatter.formatYYYYMMDDHHMM
const formatHHMMSS = DateTimeFormatter.formatHHMMSS

const subscriberName = ref(route.params.subscriberName)
const statusLastRefreshed = ref(null)
const status = ref(null)
const filteredServices = ref([])
const cachedSystemNames = ref([])
const refreshingStatus = ref(false)
const dayDuration = 1000 * 60 * 60 * 24
const dateDiffCache = ref({})
const showPublisherModal = ref(false)
const selectedPublisher = ref(null)

onMounted(() => {
  refreshScreen()
})

function refreshScreen() {
  dateDiffCache.value = {}
  refreshSubscribers()
}

async function refreshSubscribers() {
  statusLastRefreshed.value = new Date()
  refreshingStatus.value = true

  try {
    const result = await subscriberApi.getSubscriberDetails(subscriberName.value)
    status.value = result
    cacheSystemNames()
    calculateWarnings()
    applyFiltering()
  } catch (error) {
    console.error('Failed get subscriber details', error)
  } finally {
    refreshingStatus.value = false
  }
}

function calculateWarnings() {
  if (!status.value) {
    return
  }

  for (const publisher of status.value.publisherServices) {
    for (const systemStatus of publisher.systemStatus) {
      let inboundBehindDays = 0
      let inboundBehindWarning = null
      let outboundBehindDays = 0
      let outboundBehindWarning = null
      let outboundWriteBehindDays = 0
      let outboundWriteBehindWarning = null

      if (systemStatus.lastReceivedExtractCutoff) {
        if (systemStatus.lastProcessedInExtractCutoff) {
          const msBehind = systemStatus.lastReceivedExtractCutoff - systemStatus.lastProcessedInExtractCutoff
          const behindDesc = DateTimeFormatter.getDateDiffDescMs(systemStatus.lastProcessedInExtractCutoff, systemStatus.lastReceivedExtractCutoff, 2)
          inboundBehindDays = msBehind / dayDuration
          inboundBehindWarning = 'Inbound transform ' + behindDesc + ' behind'
        } else {
          inboundBehindDays = 999
          inboundBehindWarning = 'No inbound transform complete'
        }

        if (systemStatus.lastProcessedOutExtractCutoff) {
          const msBehind = systemStatus.lastReceivedExtractCutoff - systemStatus.lastProcessedOutExtractCutoff
          const behindDesc = DateTimeFormatter.getDateDiffDescMs(systemStatus.lastProcessedOutExtractCutoff, systemStatus.lastReceivedExtractCutoff, 2)
          outboundBehindDays = msBehind / dayDuration
          outboundBehindWarning = 'Outbound transform ' + behindDesc + ' behind'
        } else {
          outboundBehindDays = 999
          outboundBehindWarning = 'No outbound transform complete'
        }

        if (systemStatus.lastWriteOutExtractCutoff) {
          const msBehind = systemStatus.lastReceivedExtractCutoff - systemStatus.lastWriteOutExtractCutoff
          const behindDesc = DateTimeFormatter.getDateDiffDescMs(systemStatus.lastWriteOutExtractCutoff, systemStatus.lastReceivedExtractCutoff, 2)
          outboundWriteBehindDays = msBehind / dayDuration
          outboundWriteBehindWarning = 'Outbound write ' + behindDesc + ' behind'
        } else {
          outboundWriteBehindDays = 999
          outboundWriteBehindWarning = 'No outbound writing complete'
        }
      }

      systemStatus.inboundBehindDays = inboundBehindDays
      systemStatus.inboundBehindWarning = inboundBehindWarning
      systemStatus.outboundBehindDays = outboundBehindDays
      systemStatus.outboundBehindWarning = outboundBehindWarning
      systemStatus.outboundWriteBehindDays = outboundWriteBehindDays
      systemStatus.outboundWriteBehindWarning = outboundWriteBehindWarning
    }
  }
}

function close() {
  router.push('/subscribers')
}

function cacheSystemNames() {
  if (cachedSystemNames.value.length > 0) {
    return
  }

  if (!status.value) {
    return
  }

  const list = []

  for (const publisher of status.value.publisherServices) {
    for (const systemStatus of publisher.systemStatus) {
      const systemName = systemStatus.name
      if (list.indexOf(systemName) === -1) {
        list.push(systemName)
      }
    }
  }

  list.sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase()))
  cachedSystemNames.value = list
}

function getDateDiff(fromMs) {
  if (!fromMs) {
    return 'n/a'
  }

  if (dateDiffCache.value[fromMs]) {
    return dateDiffCache.value[fromMs]
  }

  const from = new Date(fromMs)
  const now = new Date()
  const ret = DateTimeFormatter.getDateDiffDesc(from, now, 2)
  dateDiffCache.value[fromMs] = ret
  return ret
}

function getNonNullOdsCode(publisher) {
  if (publisher && publisher.odsCode) {
    return publisher.odsCode
  } else {
    return ''
  }
}

function getLastDataCutoff(publisher) {
  let ret

  if (publisher && publisher.systemStatus) {
    for (const systemStatus of publisher.systemStatus) {
      const cutoff = systemStatus.lastReceivedExtractCutoff
      if (cutoff && (!ret || cutoff < ret)) {
        ret = cutoff
      }
    }
  }

  if (!ret) {
    ret = 0
  }
  return ret
}

function applyFiltering() {
  if (!status.value) {
    return
  }

  let filtered = []

  let validNameFilterRegex
  if (subscribersService.publisherNameFilter) {
    try {
      new RegExp(subscribersService.publisherNameFilter)
      validNameFilterRegex = subscribersService.publisherNameFilter.toLowerCase().trim()
    } catch (e) {
    }
  }

  for (const publisher of status.value.publisherServices) {
    if (subscribersService.statusFilter) {
      let include = false

      for (const systemStatus of publisher.systemStatus) {
        if (subscribersService.statusFilter === 'up-to-date') {
          if (!systemStatus.processingInError
            && systemStatus.inboundBehindDays === 0
            && systemStatus.outboundBehindDays === 0
            && systemStatus.outboundWriteBehindDays === 0) {
            include = true
          }
        } else if (subscribersService.statusFilter === 'inbound-error') {
          if (systemStatus.processingInError) {
            include = true
          }
        } else if (subscribersService.statusFilter === 'any-behind') {
          if (systemStatus.inboundBehindDays > 0
            || systemStatus.outboundBehindDays > 0
            || systemStatus.outboundWriteBehindDays > 0) {
            include = true
          }
        } else if (subscribersService.statusFilter === 'severe-behind') {
          if (systemStatus.inboundBehindDays > 1
            || systemStatus.outboundBehindDays > 1
            || systemStatus.outboundWriteBehindDays > 1) {
            include = true
          }
        } else if (subscribersService.statusFilter === 'any-issue') {
          if (systemStatus.processingInError
            || systemStatus.inboundBehindDays > 0
            || systemStatus.outboundBehindDays > 0
            || systemStatus.outboundWriteBehindDays > 0) {
            include = true
          }
        }
      }

      if (!include) {
        continue
      }
    }

    if (validNameFilterRegex) {
      const name = publisher.name
      const alias = publisher.alias
      const id = publisher.odsCode
      const uuid = publisher.uuid

      let include = false
      if (name && name.toLowerCase().match(validNameFilterRegex)) {
        include = true
      }
      if (alias && alias.toLowerCase().match(validNameFilterRegex)) {
        include = true
      }
      if (id && id.toLowerCase().match(validNameFilterRegex)) {
        include = true
      }
      if (uuid && uuid.toLowerCase() === validNameFilterRegex) {
        include = true
      }

      if (!include) {
        continue
      }
    }

    if (subscribersService.systemNameFilter) {
      let include = false

      for (const systemStatus of publisher.systemStatus) {
        if (systemStatus.name === subscribersService.systemNameFilter) {
          include = true
          break
        }
      }

      if (!include) {
        continue
      }
    }

    filtered.push(publisher)
  }

  filtered.sort((a, b) => a.name.toLowerCase().localeCompare(b.name.toLowerCase()))

  if (subscribersService.sortFilter === 'NameAsc') {
    // no extra sorting
  } else if (subscribersService.sortFilter === 'NameDesc') {
    filtered.reverse()
  } else if (subscribersService.sortFilter === 'IDAsc') {
    filtered.sort((a, b) => getNonNullOdsCode(a).localeCompare(getNonNullOdsCode(b)))
  } else if (subscribersService.sortFilter === 'IDDesc') {
    filtered.sort((a, b) => getNonNullOdsCode(a).localeCompare(getNonNullOdsCode(b)))
    filtered.reverse()
  } else if (subscribersService.sortFilter === 'LastDataAsc') {
    filtered.sort((a, b) => getLastDataCutoff(a) - getLastDataCutoff(b))
  } else if (subscribersService.sortFilter === 'LastDataDesc') {
    filtered.sort((a, b) => getLastDataCutoff(b) - getLastDataCutoff(a))
  }

  filteredServices.value = filtered
}

function saveToCsv() {
  const lines = []
  let line

  line = '"Publisher Name",' +
    '"ODS Code",' +
    '"System",' +
    '"Inbound Error",' +
    '"Last Extract Received",' +
    '"Last Extract Cutoff",' +
    '"Complete Inbound Extract Cutoff",' +
    '"Complete Outbound Extract Cutoff",' +
    '"Complete Outbound Write Extract Cutoff",' +
    '"Outbound Error"'
  lines.push(line)

  for (const publisher of filteredServices.value) {
    for (const system of publisher.systemStatus) {
      const cols = []

      cols.push(publisher.name)
      cols.push(publisher.odsCode)
      cols.push(system.name)

      if (system.processingInError) {
        cols.push(system.processingInErrorMessage)
      } else {
        cols.push('')
      }

      if (system.lastReceivedTimestamp) {
        const d = new Date()
        d.setTime(system.lastReceivedTimestamp)
        cols.push(formatDate(d))

        d.setTime(system.lastReceivedExtractCutoff)
        cols.push(formatDate(d))

        if (system.lastProcessedInExtractCutoff) {
          d.setTime(system.lastProcessedInExtractCutoff)
          cols.push(formatDate(d))
        } else {
          cols.push('None')
        }

        if (system.lastProcessedOutExtractCutoff) {
          d.setTime(system.lastProcessedOutExtractCutoff)
          cols.push(formatDate(d))
        } else {
          cols.push('None')
        }

        if (system.lastWriteOutExtractCutoff) {
          d.setTime(system.lastWriteOutExtractCutoff)
          cols.push(formatDate(d))
        } else {
          cols.push('None')
        }
      } else {
        cols.push('Never')
        cols.push('')
        cols.push('')
        cols.push('')
        cols.push('')
      }

      if (system.writeOutError) {
        cols.push(system.writeOutErrorMessage)
      } else {
        cols.push('')
      }

      line = '"' + cols.join('","') + '"'
      lines.push(line)
    }
  }

  const csvStr = lines.join('\r\n')
  const filename = subscriberName.value + '_status.csv'
  const blob = new Blob([csvStr], { type: 'text/plain' })

  let url = window.URL.createObjectURL(blob)
  let a = document.createElement('a')
  document.body.appendChild(a)
  a.setAttribute('style', 'display: none')
  a.href = url
  a.download = filename
  a.click()
  window.URL.revokeObjectURL(url)
  a.remove()
}

function formatDate(d) {
  if (!d) return '<missing date>'
  return d.getFullYear() + '-' +
    ('0' + (d.getMonth() + 1)).slice(-2) + '-' +
    ('0' + d.getDate()).slice(-2) + ' ' +
    ('0' + d.getHours()).slice(-2) + ':' +
    ('0' + d.getMinutes()).slice(-2)
}

function editPublisher(serviceUuid) {
  console.log('Edit publisher', serviceUuid)
}

function viewPublisherExchanges(serviceUuid) {
  console.log('View exchanges for', serviceUuid)
}

function viewPublisherDetails(publisher) {
  selectedPublisher.value = publisher
  showPublisherModal.value = true
}
</script>

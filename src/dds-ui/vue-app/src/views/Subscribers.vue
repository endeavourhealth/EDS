<template>
  <div class="module">
    <div class="module-heading">
      Subscriber Status {{ getDateDiffSimpleFromNow(statusLastRefreshed) }}

      <div class="pull-right">
        <div class="form-inline">
          <button class="p-button p-button-secondary p-button-sm" :disabled="refreshingStatus" @click="saveToCsv">Save to CSV</button>
          <button class="p-button p-button-success p-button-sm" :disabled="refreshingStatus" @click="refreshScreen">Refresh&nbsp;<i class="fa fa-refresh" v-if="refreshingStatus"></i></button>
        </div>
      </div>
    </div>
    <div class="module-body">
      <div class="row">
        <div class="col-md-12">
          <div class="table">
            <table class="table table-striped table-condensed">
              <thead>
                <tr>
                  <th class="col-md-2">Subscriber</th>
                  <th class="col-md-1">
                    Details
                    <span class="fa fa-info-circle fa-right-pad text-muted" title="Internal or Remote / Compass version / PI or not"></span>
                  </th>
                  <th class="col-md-1">Publishers</th>
                  <th class="col-md-1">
                    Receipt Status
                    <span class="fa fa-info-circle fa-right-pad text-muted" title="&lt;1d / &lt;2d / 2d+"></span>
                  </th>
                  <th class="col-md-1">Inbound Err</th>
                  <th class="col-md-1">
                    Inbound Status
                    <span class="fa fa-info-circle fa-right-pad text-muted" title="OK / 1d behind received / 2d+ behind received"></span>
                  </th>
                  <th class="col-md-1">
                    Outbound Status
                    <span class="fa fa-info-circle fa-right-pad text-muted" title="OK / 1d behind received / 2d+ behind received"></span>
                  </th>
                  <th class="col-md-1">
                    Outbound Write
                    <span class="fa fa-info-circle fa-right-pad text-muted" title="OK / 1d behind received / 2d+ behind received"></span>
                  </th>
                  <th class="col-md-1">Outbound Err</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="subscriber in subscribers" :key="subscriber.name" class="hover-box show-child-on-hover" @click="viewSubscriberDetails(subscriber)">
                  <td>
                    <div>{{ subscriber.name }}</div>
                    <div class="text-muted">
                      {{ subscriber.description }}
                    </div>
                  </td>
                  <td>
                    <span :title="getRemoteSubscriberIdDesc(subscriber)">{{ getSubscriberLocationDesc(subscriber) }}</span>
                    &nbsp;/&nbsp;
                    <span>{{ getSubscriberSchemaDesc(subscriber) }}</span>
                    &nbsp;/&nbsp;
                    <span>{{ getSubscriberPiDesc(subscriber) }}</span>
                  </td>
                  <td>
                    {{ padNumber(subscriber.numPublishers) }}
                  </td>
                  <td>
                    <div class="row">
                      <div class="col-md-2">{{ padNumber(subscriber.receivedOneDay) }}</div>
                      <div class="col-md-2">{{ padNumber(subscriber.receivedTwoDays) }}</div>
                      <div class="col-md-2">{{ padNumber(subscriber.receivedMoreDays) }}</div>
                      <div class="col-md-2"></div>
                    </div>
                  </td>
                  <td>
                    {{ padNumber(subscriber.inboundError) }}
                    <span class="p-tag p-tag-danger" v-if="subscriber.inboundError > 0" title="Inbound processing errors">Error</span>
                  </td>
                  <td>
                    <div class="row">
                      <div class="col-md-2">{{ padNumber(subscriber.inboundUpToDate) }}</div>
                      <div class="col-md-2">{{ padNumber(subscriber.inboundOneDay) }}</div>
                      <div class="col-md-2">{{ padNumber(subscriber.inboundMoreDays) }}</div>
                      <div class="col-md-2">
                        <span class="p-tag p-tag-danger" v-if="subscriber.inboundMoreDays > 0" title="Inbound processing 2d+ behind">Warning</span>
                      </div>
                    </div>
                  </td>
                  <td>
                    <div class="row">
                      <div class="col-md-2">{{ padNumber(subscriber.outboundUpToDate) }}</div>
                      <div class="col-md-2">{{ padNumber(subscriber.outboundOneDay) }}</div>
                      <div class="col-md-2">{{ padNumber(subscriber.outboundMoreDays) }}</div>
                      <div class="col-md-2">
                        <span class="p-tag p-tag-danger" v-if="subscriber.outboundMoreDays > 0" title="Outbound processing 2d+ behind">Warning</span>
                      </div>
                    </div>
                  </td>
                  <td>
                    <div class="row">
                      <div class="col-md-2">{{ padNumber(subscriber.writeOutUpToDate) }}</div>
                      <div class="col-md-2">{{ padNumber(subscriber.writeOutOneDay) }}</div>
                      <div class="col-md-2">{{ padNumber(subscriber.writeOutMoreDays) }}</div>
                      <div class="col-md-2">
                        <span class="p-tag p-tag-danger" v-if="subscriber.writeOutMoreDays > 0" title="Outbound write 2d+ behind">Warning</span>
                      </div>
                    </div>
                  </td>
                  <td>
                    {{ padNumber(subscriber.outboundError) }}
                    <span class="p-tag p-tag-danger" v-if="subscriber.outboundError > 0" title="Outbound writing errors">Error</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as subscriberApi from '@/services/subscriber.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'

const getDateDiffSimpleFromNow = DateTimeFormatter.getDateDiffSimpleFromNow

const subscribers = ref([])
const statusLastRefreshed = ref(null)
const refreshingStatus = ref(false)

onMounted(() => {
  refreshScreen()
})

function refreshScreen() {
  refreshSubscribers()
}

async function refreshSubscribers() {
  statusLastRefreshed.value = new Date()
  refreshingStatus.value = true

  try {
    const result = await subscriberApi.getSubscribersInstances()
    subscribers.value = [...result].sort((a, b) => a.name.toLowerCase().localeCompare(b.name.toLowerCase()))
  } catch (error) {
    console.error('Failed get subscriber details', error)
  } finally {
    refreshingStatus.value = false
  }
}

function padNumber(value) {
  let ret = '' + value
  while (ret.length < 3) {
    ret += '\xa0'
  }
  return ret
}

function getSubscriberLocationDesc(sub) {
  if (!sub.subscriberLocation) {
    return '?'
  } else if (sub.subscriberLocation === 'Internal') {
    return 'I'
  } else if (sub.subscriberLocation === 'Remote') {
    return 'R'
  } else if (sub.subscriberLocation === 'InternalAndRemote') {
    return 'IR'
  } else {
    return 'Unknown location ' + sub.subscriberLocation
  }
}

function getSubscriberSchemaDesc(sub) {
  if (!sub.schema) {
    return '?'
  } else if (sub.schema === 'CompassV1') {
    return 'v1'
  } else if (sub.schema === 'CompassV2') {
    return 'v2'
  } else {
    return 'Unknown schema ' + sub.schema
  }
}

function getSubscriberPiDesc(sub) {
  if (sub.deidentified) {
    return 'pseudo'
  } else {
    return 'PI'
  }
}

function getRemoteSubscriberIdDesc(sub) {
  if (!sub.remoteSubscriberId) {
    return null
  } else {
    return 'Remote subscriber ID ' + sub.remoteSubscriberId
  }
}

function viewSubscriberDetails(sub) {
  const subscriberName = sub.name
  console.log('view detail for ' + subscriberName)
}

function saveToCsv() {
  const lines = []
  let line

  line = '"Subscriber Name",' +
    '"Description",' +
    '"Location",' +
    '"Remote Subscriber ID",' +
    '"Schema",' +
    '"PI",' +
    '"Excludes Test Patients",' +
    '"Excludes Blank NHS Number Patients",' +
    '"Exclude NHS Number Regex",' +
    '"Cohort",' +
    '"Subscriber DB",' +
    '"Subscriber DB Name",' +
    '"Subscriber Transform DB",' +
    '"Subscriber Transform DB Name",' +
    '"Publishers",' +
    '"Inbound Errors",' +
    '"Transform In Up-to-date",' +
    '"Transform In 1d Behind",' +
    '"Transform In 2d+ Behind",' +
    '"Transform Out Up-to-date",' +
    '"Transform Out 1d Behind",' +
    '"Transform Out 2d+ Behind",' +
    '"Write Out Up-to-date",' +
    '"Write Out 1d Behind",' +
    '"Write Out 2d+ Behind",' +
    '"Outbound Errors"'
  lines.push(line)

  for (const config of subscribers.value) {
    const cols = []

    cols.push(config.name)
    cols.push(config.description)
    cols.push(getSubscriberLocationDesc(config))
    if (config.remoteSubscriberId) {
      cols.push(config.remoteSubscriberId)
    } else {
      cols.push('')
    }
    cols.push(getSubscriberSchemaDesc(config))
    if (config.deidentified) {
      cols.push('N')
    } else {
      cols.push('Y')
    }
    if (config.excludeTestPatients) {
      cols.push('Y')
    } else {
      cols.push('N')
    }
    if (config.excludePatientsWithoutNhsNumber) {
      cols.push('Y')
    } else {
      cols.push('N')
    }
    if (config.excludeNhsNumberRegex) {
      cols.push(config.excludeNhsNumberRegex)
    } else {
      cols.push('')
    }
    cols.push(config.cohortDesc)
    if (config.subscriberDatabase) {
      cols.push(config.subscriberDatabase)
    } else {
      cols.push('')
    }
    if (config.subscriberDatabaseName) {
      cols.push(config.subscriberDatabaseName)
    } else {
      cols.push('')
    }
    cols.push(config.subscriberTransformDatabase)
    cols.push(config.subscriberTransformDatabaseName)
    cols.push(config.numPublishers)
    cols.push(config.inboundError)
    cols.push(config.inboundUpToDate)
    cols.push(config.inboundOneDay)
    cols.push(config.inboundMoreDays)
    cols.push(config.outboundUpToDate)
    cols.push(config.outboundOneDay)
    cols.push(config.outboundMoreDays)
    cols.push(config.writeOutUpToDate)
    cols.push(config.writeOutOneDay)
    cols.push(config.writeOutMoreDays)
    cols.push(config.outboundError)

    line = '"' + cols.join('","') + '"'
    lines.push(line)
  }

  const csvStr = lines.join('\r\n')
  const filename = 'subscriber_status.csv'
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
</script>

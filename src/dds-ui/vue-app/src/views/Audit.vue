<template>
  <div class="module">
    <div class="module-heading">
      Audit Monitor
      <button class="p-button p-button-success p-button-sm" @click="refresh">Refresh</button>
    </div>
    <div class="module-body">
      <div class="container-fluid">
        <div class="row">
          <div class="col-md-12">
            <div class="panel panel-primary">
              <div class="panel-heading">Recent Audit Events <span class="fa fa-sm fa-refresh" v-if="!auditEvents"></span></div>
              <div class="panel-body" style="overflow:hidden; height:100%; min-height: 500px;">
                <div class="row">
                  <div class="form-group col-md-2">
                    <label for="module">Application</label>
                    <div class="input-group">
                      <span class="input-group-addon"><i class="fa fa-th-large fa-lg fa-fw"></i></span>
                      <select id="module" class="form-control" v-model="module" @change="refresh">
                        <option value="" disabled>Select Application...</option>
                        <option v-for="mod of (modules || [])" :key="mod" :value="mod">{{ mod }}</option>
                      </select>
                    </div>
                  </div>
                  <div class="form-group col-md-2">
                    <label for="user">User</label>
                    <div class="input-group">
                      <span class="input-group-addon"><i class="fa fa-user fa-lg fa-fw"></i></span>
                      <select id="user" class="form-control" v-model="user" @change="refresh">
                        <option value="" disabled>Select User...</option>
                        <option v-for="usr of (users || [])" :key="usr.uuid" :value="usr.uuid">{{ usr.username }}</option>
                      </select>
                    </div>
                  </div>
                  <div class="form-group col-md-2">
                    <label for="month">Month</label>
                    <input type="month" id="month" class="form-control" :value="monthStr" @change="onMonthChange">
                  </div>
                  <div class="form-group col-md-2" v-if="user && module">
                    <label for="submodule">Domain</label>
                    <div class="input-group">
                      <span class="input-group-addon"><i class="fa fa-th fa-lg fa-fw"></i></span>
                      <select id="submodule" class="form-control" v-model="submodule" @change="refresh">
                        <option value="">All</option>
                        <option v-for="sub of (submodules || [])" :key="sub" :value="sub">{{ sub }}</option>
                      </select>
                    </div>
                  </div>
                  <div class="form-group col-md-2" v-if="user && module && submodule">
                    <label for="action">Action</label>
                    <div class="input-group">
                      <span class="input-group-addon"><i class="fa fa-crosshairs fa-lg fa-fw"></i></span>
                      <select id="action" class="form-control" v-model="action" @change="refresh">
                        <option value="">All</option>
                        <option v-for="act of (actions || [])" :key="act" :value="act">{{ act }}</option>
                      </select>
                    </div>
                  </div>
                </div>
                <div class="row">
                  <div class="col-md-12">
                    <div class="table scroll-box-500">
                      <table class="table table-striped table-condensed">
                        <thead>
                          <tr>
                            <th>Timestamp</th>
                            <th>Module</th>
                            <th>Submodule</th>
                            <th>Action</th>
                            <th>Details</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-if="!filteredEvents || filteredEvents.length === 0">
                            <td colspan="5" class="text-center">No audit events available</td>
                          </tr>
                          <tr v-for="event of (filteredEvents || [])" :key="event.timestamp + '_' + event.module + '_' + event.action">
                            <td>{{ formatYYYYMMDDHHMMSS(event.timestamp) }}</td>
                            <td>{{ event.module }}</td>
                            <td>{{ event.subModule }}</td>
                            <td>{{ event.action }}</td>
                            <td v-if="!event.data || event.data === ''">None</td>
                            <td v-if="event.data && event.data !== ''">
                              <a @click="viewData(event)" style="cursor:pointer;">{{ getDataTitle(event.data) }}</a>
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <AuditEventDialog v-if="showEventDialog" :title="eventDialogTitle" :audit-event="selectedEvent" @close="showEventDialog = false" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import * as auditService from '@/services/audit.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'
import AuditEventDialog from '@/components/AuditEventDialog.vue'

const formatYYYYMMDDHHMMSS = DateTimeFormatter.formatYYYYMMDDHHMMSS

const module = ref('')
const user = ref('')
const month = ref(new Date())
const submodule = ref('')
const action = ref('')
const modules = ref([])
const users = ref([])
const submodules = ref([])
const actions = ref([])
const auditEvents = ref([])
const showEventDialog = ref(false)
const eventDialogTitle = ref('')
const selectedEvent = ref(null)

const monthStr = computed(() => {
  const d = month.value
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
})

onMounted(async () => {
  month.value = new Date()
  month.value.setDate(1)
  await loadModules()
  await loadUsers()
  await loadActions()
  await refresh()
})

const filteredEvents = computed(() => {
  if (!auditEvents.value) return []
  if (!submodule.value) return auditEvents.value
  return auditEvents.value.filter(event => {
    if (event.subModule !== submodule.value) return false
    if (action.value && event.action !== action.value) return false
    return true
  })
})

async function loadUsers() {
  try {
    users.value = await auditService.getUsers()
  } catch (error) {
    console.error('Error loading users', error)
  }
}

async function loadModules() {
  try {
    modules.value = await auditService.getModules()
  } catch (error) {
    console.error('Error loading modules', error)
  }
}

async function loadSubmodules() {
  if (!module.value) {
    submodules.value = []
    return
  }
  try {
    submodules.value = await auditService.getSubmodules(module.value)
  } catch (error) {
    console.error('Error loading sub modules', error)
  }
}

async function loadActions() {
  try {
    actions.value = await auditService.getActions()
  } catch (error) {
    console.error('Error loading actions', error)
  }
}

function onMonthChange(event) {
  const parts = event.target.value.split('-')
  month.value = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, 1)
  refresh()
}

async function refresh() {
  if (!user.value || !module.value || !month.value) return
  await loadSubmodules()
  await getAuditEvents()
}

async function getAuditEvents() {
  try {
    auditEvents.value = await auditService.getUserAudit(module.value, user.value, month.value, null)
  } catch (error) {
    console.error('Error loading audit events', error)
    auditEvents.value = []
  }
}

function getDataTitle(data) {
  if (!data || data === '') return 'None'
  const arrayOfLines = data.match(/[^\r\n]+/g)
  return arrayOfLines ? arrayOfLines[0] : 'None'
}

function viewData(event) {
  selectedEvent.value = event
  eventDialogTitle.value = getDataTitle(event.data)
  showEventDialog.value = true
}
</script>

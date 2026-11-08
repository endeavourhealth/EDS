<template>
  <div class="module">
    <div class="module-heading">
      Config Manager
        <div class="pull-right">
          <button type="button" class="p-button p-button-secondary p-button-sm" :disabled="refreshing" @click="newRecord" title="Add Record">Add Record</button>
          <button type="button" class="p-button p-button-secondary p-button-sm" :disabled="refreshing" @click="refreshStatusWithValidation" title="Refresh">Refresh&nbsp;<i class="fa fa-refresh" v-if="refreshing"></i></button>
        </div>
    </div>
    <div class="module-body">
      <div class="container-fluid" v-if="records">
        <div style="display: flex; gap: 1.5rem;">
          <div style="flex: 0 0 300px;">
            <div class="form-group">
              <label>App ID:</label>
              <select id="appIdCombo" v-model="filterAppId" @change="applyFiltering">
                <option value=""></option>
                <option v-for="appId of getAppIds" :key="appId" :value="appId">{{ appId }}</option>
              </select>
              &nbsp;&nbsp;&nbsp;
              <label for="includeGlobal" class="control-label">Include Global</label>
              <input type="checkbox" id="includeGlobal" v-model="filterIncludeGlobal" @change="applyFiltering">
            </div>

            <div class="form-group">
              <label for="searchText" class="control-label">Search</label>
              <input type="text" id="searchText" placeholder="Regex" v-model="filterSearchText" @input="applyFiltering">
              &nbsp;&nbsp;&nbsp;
              <label for="searchAppId" class="control-label">App ID</label>
              <input type="checkbox" id="searchAppId" v-model="filterSearchOnAppId" @change="applyFiltering">
              &nbsp;&nbsp;&nbsp;
              <label for="searchConfigId" class="control-label">Config ID</label>
              <input type="checkbox" id="searchConfigId" v-model="filterSearchOnConfigId" @change="applyFiltering">
            </div>

            <div style="height: 500px; overflow: auto; border: 2px solid #04466B; border-radius: 4px; background-color: #ffffff; padding: 4px;">
              <table class="table table-striped table-condensed">
                <thead>
                  <tr>
                    <th>App ID</th>
                    <th>Config ID</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="!filteredRecords || filteredRecords.length === 0">
                    <td colspan="2" class="text-center">No config records found</td>
                  </tr>
                  <tr v-for="record in (filteredRecords || [])" :key="record.appId + '_' + record.configId" class="hover-box show-child-on-hover" @click="selectRecord(record)" :class="{ 'selection': record === selectedRecord }">
                    <td>{{ record.appId }}</td>
                    <td>
                      <span v-if="isNewRecord(record) || isChangedRecord(record)" class="fa fa-save"></span>
                      {{ record.configId }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div style="margin-top: 10px;">
              {{ (records || []).length }} records found
            </div>
            <div>
              ({{ (filteredRecords || []).length }} shown)
            </div>
          </div>

          <div style="flex: 1;" v-if="selectedRecord">
            <div>
              <div class="form-group">
                <label class="control-label" style="text-align: left; display: block;">App ID</label>
                <input type="text" class="form-control" v-model="selectedRecord.appId" :disabled="!isNewRecord(selectedRecord)" name="appId" style="border: 1px solid #ced4da; overflow-x: auto; white-space: nowrap; text-align: left;">
              </div>
              <div class="form-group">
                <label class="control-label" style="text-align: left; display: block;">Config ID</label>
                <input type="text" class="form-control" v-model="selectedRecord.configId" :disabled="!isNewRecord(selectedRecord)" name="configId" style="border: 1px solid #ced4da; overflow-x: auto; white-space: nowrap; text-align: left;">
              </div>
              <div class="form-group">
                <div style="max-height: 500px; overflow-y: auto; overflow-x: auto; border: 2px solid #04466B; border-radius: 4px; background-color: #ffffff; padding: 4px;">
                  <textarea id="configData" v-model="selectedRecord.configData" name="configData" style="font-family:monospace; white-space: pre-wrap; width: 100%; height: 480px; box-sizing: border-box; padding: 0.5rem 0.75rem; border: none; background-color: transparent; color: #04466B; resize: none; outline: none; display: block; line-height: 1.5;" @input="configChanged(selectedRecord)"></textarea>
                </div>
              </div>
              <div v-if="validJsonMessage" class="form-group">
                <span class="p-tag p-tag-success">{{ validJsonMessage }}</span>
              </div>
              <div v-if="invalidJsonMessage" class="form-group">
                <span class="p-tag p-tag-danger">{{ invalidJsonMessage }}</span>
              </div>
              <div class="form-group">
                <button type="button" class="p-button p-button-secondary p-button-sm" @click="validateJSON" title="Validate JSON">Validate JSON</button>
                <button type="button" class="p-button p-button-secondary p-button-sm" @click="validateXML" title="Validate XML">Validate XML</button>
                <button type="button" class="p-button p-button-secondary p-button-sm" :disabled="!hasHistory(selectedRecord)" @click="retrieveHistory(selectedRecord)" title="View History">View History</button>
                <button type="button" class="p-button p-button-secondary p-button-sm" @click="copyRecord" title="Copy Record">Copy Record</button>
                <button type="button" class="p-button p-button-success p-button-sm" @click="saveRecord" title="Save Record">Save Record</button>
                <button type="button" class="p-button p-button-danger p-button-sm" @click="deleteRecord" title="Delete Record">Delete Record</button>
              </div>
              <div v-if="hasHistory(selectedRecord)">
                <div style="flex: 0 0 150px;">
                  <div class="table scroll-box-300">
                    <table class="table table-striped table-condensed">
                      <thead>
                        <tr>
                          <th>Changed</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="history of getHistory" :key="history.dtChanged" class="hover-box show-child-on-hover" @click="selectHistory(history)" :class="{ 'selection': history === selectedHistory }">
                          <td>{{ formatYYYYMMDDHHMMSS(history.dtChanged) }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
                <div style="flex: 1;" v-if="selectedHistory">
                  <div>
                    <div style="display: flex; gap: 1rem;">
                      <div class="form-group" style="flex: 1;">
                        <label class="control-label">Changed From</label>
                        <textarea disabled rows="12" class="form-control" v-model="selectedHistory.changedFrom" wrap="off" style="font-family:monospace;"></textarea>
                      </div>
                      <div class="form-group" style="flex: 1;">
                        <label class="control-label">Changed To</label>
                        <textarea disabled rows="12" class="form-control" v-model="selectedHistory.changedTo" wrap="off" style="font-family:monospace;"></textarea>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import * as configManagerService from '@/services/configManager.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'

const formatYYYYMMDDHHMMSS = DateTimeFormatter.formatYYYYMMDDHHMMSS

const records = ref([])
const selectedRecord = ref(null)
const newRecords = ref([])
const changedRecords = ref([])
const validJsonMessage = ref(null)
const invalidJsonMessage = ref(null)
const filteredRecords = ref([])
const historyMap = ref({})
const selectedHistory = ref(null)
const refreshing = ref(false)

const filterAppId = ref('')
const filterIncludeGlobal = ref(false)
const filterSearchText = ref('')
const filterSearchOnAppId = ref(false)
const filterSearchOnConfigId = ref(true)

onMounted(async () => {
  await refreshStatus()
})

async function refreshStatus() {
  refreshing.value = true
  newRecords.value = []
  changedRecords.value = []
  selectedRecord.value = null
  validJsonMessage.value = null
  invalidJsonMessage.value = null
  filteredRecords.value = []
  historyMap.value = {}
  selectedHistory.value = null

  try {
    const result = await configManagerService.getRecords()
    console.log('ConfigManager: API response:', result)
    if (result && result.length > 0) {
      console.log('ConfigManager: First record sample:', result[0])
    }
    records.value = (result || []).sort((a, b) => {
      const appCompare = a.appId.toLowerCase().localeCompare(b.appId.toLowerCase())
      if (appCompare !== 0) return appCompare
      return a.configId.toLowerCase().localeCompare(b.configId.toLowerCase())
    })
    applyFiltering()
  } catch (error) {
    console.error('Failed to get config records', error)
  } finally {
    refreshing.value = false
  }
}

function refreshStatusWithValidation() {
  if (newRecords.value.length > 0 || changedRecords.value.length > 0) {
    if (!confirm('Refreshing will lose unsaved changes. Continue?')) return
  }
  refreshStatus()
}

const getAppIds = computed(() => {
  const ret = []
  if (records.value) {
    for (const record of records.value) {
      if (!ret.includes(record.appId)) {
        ret.push(record.appId)
      }
    }
  }
  return ret
})

function selectRecord(record) {
  selectedRecord.value = record
  validJsonMessage.value = null
  invalidJsonMessage.value = null
  selectedHistory.value = null
}

function isNewRecord(record) {
  return newRecords.value.includes(record)
}

function isChangedRecord(record) {
  return changedRecords.value.includes(record)
}

function configChanged(record) {
  validJsonMessage.value = null
  invalidJsonMessage.value = null
  if (!changedRecords.value.includes(record)) {
    changedRecords.value.push(record)
  }
}

async function saveRecord() {
  const record = selectedRecord.value
  if (!record.appId || record.appId === '<app_id>') {
    alert('App ID not set')
    return
  }
  if (!record.configId || record.configId === '<config_id>') {
    alert('Config ID not set')
    return
  }
  if (!record.configData) {
    alert('Config data not set')
    return
  }

  for (const otherRecord of records.value) {
    if (otherRecord !== record) {
      if (otherRecord.appId === record.appId && otherRecord.configId === record.configId) {
        alert('Duplicate record')
        return
      }
    }
  }

  try {
    await configManagerService.saveRecord(record)
    alert('Saved config record')
    const newIdx = newRecords.value.indexOf(record)
    if (newIdx > -1) newRecords.value.splice(newIdx, 1)
    const changedIdx = changedRecords.value.indexOf(record)
    if (changedIdx > -1) changedRecords.value.splice(changedIdx, 1)
  } catch (error) {
    console.error('Failed to save config record', error)
    alert('Failed to save config record: ' + (error.response?.data || error.message))
  }
}

async function deleteRecord() {
  if (!selectedRecord.value) return
  if (!confirm('Are you sure you want to delete this config record?')) return

  try {
    await configManagerService.deleteRecord(selectedRecord.value)
    alert('Delete config record')
    await refreshStatus()
  } catch (error) {
    console.error('Failed to delete config record', error)
    alert('Failed to delete config record: ' + (error.response?.data || error.message))
  }
}

function copyRecord() {
  if (!selectedRecord.value) return
  newRecordImpl(selectedRecord.value)
}

function newRecord() {
  newRecordImpl(null)
}

function newRecordImpl(copyFrom) {
  const record = {
    appId: '<app_id>',
    configId: '<config_id>',
    configData: ''
  }

  if (copyFrom) {
    record.appId = copyFrom.appId
    record.configId = copyFrom.configId + ' (copy)'
    record.configData = copyFrom.configData
  }

  records.value.push(record)
  newRecords.value.push(record)
  filteredRecords.value.push(record)
  selectedRecord.value = record
}

function validateJSON() {
  validJsonMessage.value = null
  invalidJsonMessage.value = null
  const str = selectedRecord.value.configData

  try {
    const obj = JSON.parse(str)
    const newStr = JSON.stringify(obj, null, 4)
    if (newStr === str) {
      validJsonMessage.value = 'Valid JSON'
    } else {
      selectedRecord.value.configData = newStr
      configChanged(selectedRecord.value)
      validJsonMessage.value = 'Valid JSON (reformatted)'
    }
  } catch (e) {
    invalidJsonMessage.value = 'Invalid JSON: ' + e.message
  }
}

function validateXML() {
  validJsonMessage.value = null
  invalidJsonMessage.value = null
  const str = selectedRecord.value.configData

  try {
    const parser = new DOMParser()
    const doc = parser.parseFromString(str, 'text/xml')
    const errors = doc.getElementsByTagName('parsererror')
    if (errors.length > 0) {
      invalidJsonMessage.value = errors[0].getElementsByTagName('div')[0]?.innerHTML || 'Invalid XML'
    } else {
      validJsonMessage.value = 'Valid XML'
    }
  } catch (e) {
    invalidJsonMessage.value = 'Invalid XML: ' + e.message
  }
}

function hasHistory(record) {
  const key = record.appId + '_' + record.configId
  return historyMap.value.hasOwnProperty(key)
}

const getHistory = computed(() => {
  if (!selectedRecord.value) return []
  const key = selectedRecord.value.appId + '_' + selectedRecord.value.configId
  return historyMap.value[key] || []
})

async function retrieveHistory(record) {
  const key = record.appId + '_' + record.configId
  selectedHistory.value = null

  try {
    const result = await configManagerService.getHistory(record.appId, record.configId)
    historyMap.value[key] = (result || []).reverse()
  } catch (error) {
    console.error('Failed to get config history', error)
  }
}

function selectHistory(history) {
  selectedHistory.value = history
}

function applyFiltering() {
  filteredRecords.value = []
  if (!records.value) return

  let validSearchRegex
  if (filterSearchText.value) {
    try {
      new RegExp(filterSearchText.value)
      validSearchRegex = filterSearchText.value.toLowerCase().trim()
    } catch (e) {
    }
  }

  for (const record of records.value) {
    if (filterAppId.value) {
      if (filterAppId.value !== record.appId && (record.appId !== 'global' || !filterIncludeGlobal.value)) {
        continue
      }
    }

    if (validSearchRegex) {
      let matches = false
      if (filterSearchOnAppId.value && record.appId && record.appId.toLowerCase().match(validSearchRegex)) matches = true
      if (filterSearchOnConfigId.value && record.configId && record.configId.toLowerCase().match(validSearchRegex)) matches = true
      if (!matches) continue
    }

    filteredRecords.value.push(record)
  }

  if (selectedRecord.value && !filteredRecords.value.includes(selectedRecord.value)) {
    selectedRecord.value = null
  }
}
</script>

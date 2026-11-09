<template>
  <div class="module">
    <div class="container-fluid">
      <div class="module-heading">
        System Config
        <div class="pull-right">
          <button class="p-button p-button-secondary p-button-sm" :label="refreshing ? 'Refreshing...' : 'Refresh'" :disabled="refreshing" @click="refresh">{{ refreshing ? 'Refreshing...' : 'Refresh' }}</button>
          <button class="p-button p-button-success p-button-sm" @click="addSystem">Add New</button>
        </div>
      </div>
      <div class="module-body" v-if="!editing">
        <div class="table">
          <table class="table table-striped table-condensed">
            <thead>
              <tr>
                <th class="col-md-6">Title</th>
                <th class="col-md-6">Last Modified</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in systems" :key="item.uuid" class="hover-box show-child-on-hover">
                <td>
                  <a @click="editSystem(item)" style="cursor:pointer;">{{ item.name }}</a>
                </td>
                <td>{{ formatYYYYMMDDHHMM(item.lastModified) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="module-body" v-if="editing && libraryItem">
        <div class="module" style="margin-bottom: 1rem;">
          <div class="row">
            <div class="form-group col-md-6">
              <label for="name">System Name</label>
              <div class="input-group">
                <span class="input-group-addon"><i class="fa fa-edit fa-lg fa-fw"></i></span>
                <input type="text" class="form-control" id="name" v-model="libraryItem.name" placeholder="Please enter a system name" name="name">
              </div>
            </div>
            <div class="form-group col-md-6">
              <label for="description">System Description</label>
              <div class="input-group">
                <span class="input-group-addon"><i class="fa fa-edit fa-lg fa-fw"></i></span>
                <textarea class="form-control" id="description" v-model="libraryItem.description" placeholder="Please enter a description for this system" name="description"></textarea>
              </div>
            </div>
          </div>
          <div class="row">
            <div class="form-group col-md-6">
              <label>Transaction Frequency</label>
              <div class="input-group">
                <span class="input-group-addon"><i class="fa fa-calendar fa-lg fa-fw"></i></span>
                <select class="form-control" v-model="techInterface.frequency" name="frequency">
                  <option v-for="freq of frequencies" :key="freq" :value="freq">{{ freq }}</option>
                </select>
              </div>
            </div>
          </div>
          <div class="row">
            <div class="form-group col-md-6">
              <label>Message Type</label>
              <div class="input-group">
                <span class="input-group-addon"><i class="fa fa-question fa-lg fa-fw"></i></span>
                <select class="form-control" v-model="techInterface.messageType" name="messageType">
                  <option v-for="type of types" :key="type" :value="type">{{ type }}</option>
                </select>
              </div>
            </div>
          </div>
          <div class="row">
            <div class="form-group col-md-6">
              <label>Message Format</label>&nbsp;
              <button class="p-button p-button-secondary p-button-sm" @click="newFormat">New Format</button>
              <div class="input-group">
                <span class="input-group-addon"><i class="fa fa-code fa-lg fa-fw"></i></span>
                <select class="form-control" v-model="techInterface.messageFormat" name="messageFormat">
                  <option v-for="format of formats" :key="format" :value="format">{{ format }}</option>
                </select>
              </div>
            </div>
          </div>
          <div class="row">
            <div class="form-group col-md-6">
              <label>Message Format Version</label>
              <div class="input-group">
                <span class="input-group-addon"><i class="fa fa-sort-numeric-asc fa-lg fa-fw"></i></span>
                <input type="text" class="form-control" id="messageFormatVersion" v-model="techInterface.messageFormatVersion" placeholder="Please enter a message format version" name="messageVersion">
              </div>
            </div>
          </div>
        </div>
        <div class="pull-right" style="margin-top: 15px;">
          <button class="p-button p-button-danger p-button-sm" @click="deleteSystem" v-if="!isNew">Delete System</button>
          <button class="p-button p-button-success p-button-sm" @click="save(false)">Save</button>
          <button class="p-button p-button-warning p-button-sm" @click="save(true)">Save & Close</button>
          <button class="p-button p-button-danger p-button-sm" @click="cancel">Cancel</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as libraryService from '@/services/library.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'

const formatYYYYMMDDHHMM = DateTimeFormatter.formatYYYYMMDDHHMM
const router = useRouter()

const systems = ref([])
const refreshing = ref(false)
const editing = ref(false)
const libraryItem = ref(null)
const formats = ref([])
const types = ref(['Patient Record', 'Demographics', 'Appointments', 'Summary', 'Discharge', 'Episode'])
const frequencies = ref(['Transactional', 'Hourly', 'Daily'])

const techInterface = computed(() => {
  if (!libraryItem.value || !libraryItem.value.system || !libraryItem.value.system.technicalInterface || libraryItem.value.system.technicalInterface.length === 0) {
    if (libraryItem.value && libraryItem.value.system) {
      libraryItem.value.system.technicalInterface = []
    }
    return {
      uuid: '',
      name: '',
      frequency: '',
      messageType: '',
      messageFormat: '',
      messageFormatVersion: ''
    }
  }
  return libraryItem.value.system.technicalInterface[0]
})

const isNew = computed(() => {
  return !libraryItem.value || !libraryItem.value.uuid
})

onMounted(async () => {
  await refresh()
})

async function refresh() {
  refreshing.value = true
  try {
    const result = await libraryService.getSystemList()
    systems.value = result || []
  } catch (error) {
    console.error('Error getting systems', error)
  } finally {
    refreshing.value = false
  }
}

function addSystem() {
  libraryItem.value = {
    uuid: null,
    name: '',
    description: '',
    folderUuid: null,
    system: {
      uuid: null,
      name: '',
      technicalInterface: []
    }
  }
  editing.value = true
  loadFormats()
}

function editSystem(system) {
  libraryService.getLibraryItem(system.uuid).then((result) => {
    libraryItem.value = result
    editing.value = true
    loadFormats()
  }).catch((error) => {
    console.error('Error loading system', error)
  })
}

async function loadFormats() {
  try {
    const systems = await libraryService.getSystems()
    formats.value = []
    if (systems) {
      for (const existingSystem of systems) {
        if (existingSystem.technicalInterface) {
          for (const techInterface of existingSystem.technicalInterface) {
            if (techInterface.messageFormat && !formats.value.includes(techInterface.messageFormat)) {
              formats.value.push(techInterface.messageFormat)
            }
          }
        }
      }
    }
    formats.value.sort()
  } catch (error) {
    console.error('Failed to retrieve system formats', error)
  }
}

function newFormat() {
  const newFormat = prompt('Enter the new format name')
  if (newFormat == null) return
  if (newFormat.length === 0) {
    alert('Format cannot be empty')
    return
  }
  if (formats.value.includes(newFormat)) {
    alert('Format already exists')
    return
  }
  formats.value.push(newFormat)
  techInterface.value.messageFormat = newFormat
}

async function save(close) {
  if (!libraryItem.value) return

  techInterface.value.name = libraryItem.value.name

  try {
    const result = await libraryService.saveLibraryItem(libraryItem.value)
    libraryItem.value.uuid = result.uuid
    alert('Item saved')
    if (close) {
      cancel()
    }
  } catch (error) {
    console.error('Error saving', error)
    alert('Error saving: ' + (error.response?.data || error.message))
  }
}

function deleteSystem() {
  if (!confirm('Are you sure you want to delete the System?')) return

  libraryService.deleteSystem(libraryItem.value.uuid).then((result) => {
    if (result) {
      alert(result)
    } else {
      alert('System deleted')
      cancel()
    }
  }).catch((error) => {
    console.error('Failed to delete System', error)
    alert('Failed to delete System: ' + (error.response?.data || error.message))
  })
}

function cancel() {
  editing.value = false
  libraryItem.value = null
}
</script>

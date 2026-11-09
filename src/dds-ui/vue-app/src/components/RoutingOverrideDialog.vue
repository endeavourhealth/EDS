<template>
  <div v-if="visible" class="modal-overlay" style="display: flex; align-items: center; justify-content: center; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 1000;" @click.self="close">
    <div class="modal-content" style="background: white; border-radius: 6px; padding: 20px; max-width: 600px; width: 90%; max-height: 90vh; overflow-y: auto; position: relative;">
      <div class="modal-header" style="margin-bottom: 15px; font-weight: bold;">Routing Override</div>
      <form class="container-fluid">
        <div class="row">
          <div class="form-group col-md-12">
            <label class="control-label">Publisher</label>
            <input type="text" class="form-control" :value="serviceDesc" disabled>
            <button type="button" class="p-button p-button-primary p-button-sm" @click="selectPublisher">Search Publishers</button>
          </div>
        </div>
        <div class="row">
          <div class="form-group col-md-12">
            <label class="control-label">Exchange</label>
            <select class="form-control" v-model="override.exchangeName" name="exchangeNameCombo">
              <option value=""></option>
              <option v-for="exchangeName in routingExchangeNames" :key="exchangeName" :value="exchangeName">{{ exchangeName }}</option>
            </select>
          </div>
        </div>
        <div class="row">
          <div class="form-group col-md-12">
            <label class="control-label">Routing Key</label>
            <select class="form-control" v-model="override.routingKey" name="routingKeyCombo">
              <option v-for="routingKey in getRoutingKeysForExchange" :key="routingKey" :value="routingKey">{{ routingKey }}</option>
            </select>
          </div>
        </div>
      </form>
      <div class="modal-footer" style="margin-top: 15px; text-align: right;">
        <button type="button" class="p-button p-button-sm" @click="close">Cancel</button>
        <button type="button" class="p-button p-button-primary p-button-sm" @click="save">Ok</button>
      </div>
    </div>
  </div>
  <div v-if="showServicePicker" class="modal-overlay" style="display: flex; align-items: center; justify-content: center; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 1000;" @click.self="closeServicePicker">
    <div class="modal-content" style="background: white; border-radius: 6px; padding: 20px; max-width: 700px; width: 90%; max-height: 90vh; overflow-y: auto; position: relative;">
      <div class="modal-header" style="margin-bottom: 15px; font-weight: bold;">Select Publisher</div>
      <div class="form-group">
        <label>Search:</label>
        <input type="text" class="form-control" v-model="servicePickerSearch" placeholder="Name, ID, or UUID">
      </div>
      <div class="table" style="max-height: 400px; overflow-y: auto;">
        <table class="table table-striped table-condensed">
          <thead>
            <tr>
              <th class="col-md-4">Name</th>
              <th class="col-md-2">ID</th>
              <th class="col-md-4">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="service in filteredServices" :key="service.uuid" class="hover-box">
              <td>{{ service.name }}</td>
              <td>{{ service.localIdentifier }}</td>
              <td>
                <button type="button" class="p-button p-button-success p-button-sm" @click="selectService(service)">Select</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="modal-footer" style="margin-top: 15px; text-align: right;">
        <button type="button" class="p-button p-button-sm" @click="closeServicePicker">Cancel</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import * as servicesService from '@/services/services.service.js'

const props = defineProps({
  routingExchangeNames: { type: Array, required: true },
  routingMap: { type: Object, required: true },
  serviceMap: { type: Object, required: true },
  override: { type: Object, required: true }
})

const emit = defineEmits(['close', 'saved'])

const visible = ref(true)
const showServicePicker = ref(false)
const override = ref({ ...props.override })
const serviceDesc = ref('')
const servicePickerSearch = ref('')
const allServices = ref([])

watch(() => props.override, (newVal) => {
  override.value = { ...newVal }
  populateServiceDesc()
})

watch(() => props.serviceMap, () => {
  populateServiceDesc()
}, { deep: true })

async function populateServiceDesc() {
  if (!override.value.serviceId || !props.serviceMap[override.value.serviceId]) {
    serviceDesc.value = ''
    return
  }
  const service = props.serviceMap[override.value.serviceId]
  serviceDesc.value = service.localIdentifier + ', ' + service.name
}

const getRoutingKeysForExchange = computed(() => {
  if (!override.value.exchangeName) return []
  const routings = props.routingMap[override.value.exchangeName]
  if (!routings) return []
  return routings.map(r => r.routeKey)
})

const filteredServices = computed(() => {
  if (!servicePickerSearch.value) return allServices.value
  const search = servicePickerSearch.value.toLowerCase()
  return allServices.value.filter(s =>
    s.name.toLowerCase().includes(search) ||
    s.localIdentifier.toLowerCase().includes(search) ||
    s.uuid.toLowerCase().includes(search)
  )
})

async function selectPublisher() {
  try {
    allServices.value = await servicesService.getAllServices()
    showServicePicker.value = true
  } catch (error) {
    console.error('Failed to load services', error)
  }
}

function closeServicePicker() {
  showServicePicker.value = false
  servicePickerSearch.value = ''
}

async function selectService(service) {
  override.value.serviceId = service.uuid
  serviceDesc.value = service.localIdentifier + ', ' + service.name
  closeServicePicker()
}

function save() {
  if (!override.value.serviceId) {
    alert('No service selected')
    return
  }
  if (!override.value.exchangeName) {
    alert('No exchange selected')
    return
  }
  if (!override.value.routingKey) {
    alert('No routing key selected')
    return
  }
  emit('saved', { ...override.value })
  close()
}

function close() {
  visible.value = false
  emit('close')
}
</script>

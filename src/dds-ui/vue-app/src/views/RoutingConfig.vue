<template>
  <div class="module">
    <div class="container-fluid">
      <div class="module-heading">
        Routing Config
        <div class="pull-right">
          <button class="p-button p-button-success p-button-sm" @click="sync" title="Sync to RabbitMQ">Sync to RabbitMQ</button>
          <button class="p-button p-button-success p-button-sm" @click="newRouting" title="New Routing">New Routing</button>
          <button class="p-button p-button-success p-button-sm" @click="refresh" title="Refresh">Refresh</button>
        </div>
      </div>
      <div class="module-body">
        <div>
          <div class="row">
            <div class="form-group col-md-6">
              <div class="module" style="margin-bottom: 1rem;">
                <div class="module-heading">Queue Status</div>
                <div class="module-body">
                  <div v-if="rabbitNodes">
                    Nodes:
                    <span v-for="node in rabbitNodes" :key="node.address">
                      {{ node.address }} ({{ node.ping }}ms)
                      <span v-if="node === quickestNode" class="p-tag p-tag-success">Quickest</span>
                      <span v-else class="p-tag p-tag-secondary">Slow</span>
                    </span>
                  </div>
                  <div v-for="exchangeName in routingExchangeNames" :key="exchangeName" style="margin-top: 10px;">
                    <label style="font-weight: bold;">{{ exchangeName }}</label>
                    <div v-for="routing in getRoutingsForExchange(exchangeName)" :key="routing.routeKey" style="margin-left: 20px;">
                      <label class="small">
                        <i :class="getRouteGroupStatusIconClass(routing)"></i>
                        {{ routing.routeKey }}
                        <span v-if="routing.description" class="fa fa-info-circle fa-right-pad text-muted" :title="routing.description"></span>
                      </label>
                    </div>
                    <div v-for="binding in getBindingsForExchange(exchangeName)" :key="binding.routing_key" style="margin-left: 20px;">
                      <label class="small" v-if="!bindingExistsInConfig(binding)">
                        <i class="fa fa-minus-circle text-danger"></i>
                        {{ binding.routing_key }}
                      </label>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="form-group col-md-6">
              <div class="module" style="margin-bottom: 1rem;">
                <div class="module-heading">
                  Routing Overrides
                  <button class="p-button p-button-success p-button-sm pull-right" @click="addNewOverride">New Override</button>
                </div>
                <div class="module-body">
                  <div class="table" v-if="routingOverrides">
                    <table class="table table-striped table-condensed">
                      <thead>
                        <tr>
                          <th class="col-md-5">Publisher</th>
                          <th class="col-md-3">Exchange</th>
                          <th class="col-md-3">Routing Key</th>
                          <th class="col-md-1"></th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="routingOverride in routingOverrides" :key="routingOverride.serviceId + '_' + routingOverride.routingKey" class="hover-box show-child-on-hover">
                          <td>{{ getServiceDesc(routingOverride) }}</td>
                          <td>{{ routingOverride.exchangeName }}</td>
                          <td>{{ routingOverride.routingKey }}</td>
                          <td>
                            <button class="p-button p-button-danger p-button-sm" @click="deleteRoutingOverride(routingOverride)"><span class="fa fa-remove"></span></button>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-for="exchangeName in routingExchangeNames" :key="'exchange-' + exchangeName">
            <div class="module" style="margin-bottom: 1rem;">
              <div class="module-heading">
                Exchange: {{ exchangeName }}
                <button class="p-button p-button-success p-button-sm pull-right" @click="newRouting(exchangeName)">New Routing</button>
              </div>
              <div class="module-body">
                <div class="row">
                  <div class="form-group col-md-12">
                    <div class="table">
                      <table class="table table-striped table-condensed">
                        <thead>
                          <tr>
                            <th class="col-md-5">Description</th>
                            <th class="col-md-4">Regex Filter</th>
                            <th class="col-md-2">Routing Key</th>
                            <th class="col-md-1"></th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="item in getRoutingsForExchange(exchangeName)" :key="item.routeKey" class="hover-box show-child-on-hover">
                            <td>
                              <a @click="edit(item)" style="cursor:pointer;">{{ item.description }}</a>
                            </td>
                            <td>{{ item.regex }}</td>
                            <td>{{ item.routeKey }}</td>
                            <td>
                              <button class="p-button p-button-secondary p-button-sm" @click="moveUp(item)"><span class="fa fa-arrow-up"></span></button>
                              <button class="p-button p-button-secondary p-button-sm" @click="moveDown(item)"><span class="fa fa-arrow-down"></span></button>
                              <button class="p-button p-button-danger p-button-sm" @click="deleteRouting(item)"><span class="fa fa-remove"></span></button>
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
    <QueueEditDialog v-if="showQueueEditDialog" :routing="editingRouting" @close="showQueueEditDialog = false" @saved="onRoutingSaved" />
    <RoutingOverrideDialog v-if="showRoutingOverrideDialog" :routing-exchange-names="routingExchangeNames" :routing-map="routingMap" :service-map="serviceMapById" :override="editingOverride" @close="showRoutingOverrideDialog = false" @saved="onOverrideSaved" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as rabbitService from '@/services/rabbit.service.js'
import * as servicesService from '@/services/services.service.js'
import QueueEditDialog from '@/components/QueueEditDialog.vue'
import RoutingOverrideDialog from '@/components/RoutingOverrideDialog.vue'

const rabbitNodes = ref([])
const quickestNode = ref(null)
const routingExchangeNames = ref([])
const routingMap = ref({})
const bindingMap = ref({})
const routingOverrides = ref([])
const serviceMapById = ref({})
const showQueueEditDialog = ref(false)
const showRoutingOverrideDialog = ref(false)
const editingRouting = ref(null)
const editingOverride = ref(null)

onMounted(async () => {
  await refresh()
})

async function refresh() {
  await getRabbitNodes()
  await getServices()
  await getRoutingOverrides()
}

async function getRabbitNodes() {
  try {
    rabbitNodes.value = await rabbitService.getRabbitNodes()
    if (rabbitNodes.value && rabbitNodes.value.length > 0) {
      await getRabbitNodePings()
    }
  } catch (error) {
    console.error('Error getting rabbit node list', error)
  }
}

async function getRabbitNodePings() {
  quickestNode.value = null
  const promises = rabbitNodes.value.map(async (node) => {
    try {
      const result = await rabbitService.pingRabbitNode(node.address)
      if (!quickestNode.value || result.ping < quickestNode.value.ping) {
        quickestNode.value = result
      }
      const idx = rabbitNodes.value.findIndex(n => n.address === result.address)
      if (idx >= 0) {
        rabbitNodes.value[idx].ping = result.ping
      }
    } catch (error) {
      console.error('Error pinging node', node.address, error)
    }
  })
  await Promise.all(promises)

  if (quickestNode.value) {
    await getRouteGroups()
    await getRabbitBindings()
  }
}

async function getRouteGroups() {
  try {
    const routings = await rabbitService.getRoutings()
    populateRoutingMap(routings)
  } catch (error) {
    console.error('Failed to load route groups', error)
  }
}

function populateRoutingMap(routings) {
  routingExchangeNames.value = []
  routingMap.value = {}

  for (const r of routings) {
    addToRoutingMap(r)
  }
}

function addToRoutingMap(routing) {
  const exchangeName = routing.exchangeName
  if (!routingMap.value[exchangeName]) {
    routingMap.value[exchangeName] = []
    routingExchangeNames.value.push(exchangeName)
  }
  routingMap.value[exchangeName].push(routing)
}

function getRoutingsForExchange(exchangeName) {
  return routingMap.value[exchangeName] || []
}

async function getRabbitBindings() {
  try {
    const bindings = await rabbitService.getRabbitBindings(quickestNode.value.address)
    populateBindingMap(bindings)
  } catch (error) {
    console.error('Failed to load rabbit bindings', error)
  }
}

function populateBindingMap(bindings) {
  bindingMap.value = {}
  for (const r of bindings) {
    addToBindingMap(r)
  }
}

function addToBindingMap(binding) {
  const exchangeName = binding.source
  if (!bindingMap.value[exchangeName]) {
    bindingMap.value[exchangeName] = []
  }
  bindingMap.value[exchangeName].push(binding)
}

function getBindingsForExchange(exchangeName) {
  return bindingMap.value[exchangeName] || []
}

function getRouteGroupStatusIconClass(routeGroup) {
  const bindings = getBindingsForExchange(routeGroup.exchangeName)
  if (!bindings || bindings.length === 0) {
    return 'fa fa-plus-circle text-danger'
  }
  const found = bindings.find(b => b.routing_key === routeGroup.routeKey)
  return found ? 'fa fa-check-circle text-success' : 'fa fa-plus-circle text-danger'
}

function bindingExistsInConfig(binding) {
  const routings = getRoutingsForExchange(binding.source)
  return routings.some(r => r.routeKey === binding.routing_key)
}

async function sync() {
  if (!confirm('Are you sure you want to synchronise RabbitMQ with the defined route groups?')) return
  try {
    await rabbitService.synchronizeRabbit(quickestNode.value.address)
    alert('RabbitMQ synchronized')
    await getRabbitBindings()
  } catch (error) {
    console.error('Failed to synchronize', error)
    alert('Failed to synchronize: ' + (error.response?.data || error.message))
  }
}

function edit(item) {
  editingRouting.value = JSON.parse(JSON.stringify(item))
  showQueueEditDialog.value = true
}

function newRouting(exchangeName) {
  editingRouting.value = {
    regex: '',
    routeKey: '',
    description: '',
    exchangeName: exchangeName || ''
  }
  showQueueEditDialog.value = true
}

async function onRoutingSaved(routing) {
  const exchangeName = routing.exchangeName
  if (!routingMap.value[exchangeName]) {
    routingMap.value[exchangeName] = []
    if (!routingExchangeNames.value.includes(exchangeName)) {
      routingExchangeNames.value.push(exchangeName)
    }
  }
  const existingIdx = routingMap.value[exchangeName].findIndex(r => r.routeKey === routing.routeKey)
  if (existingIdx >= 0) {
    routingMap.value[exchangeName][existingIdx] = routing
  } else {
    routingMap.value[exchangeName].push(routing)
  }
  await saveRoutings()
  showQueueEditDialog.value = false
}

async function deleteRouting(item) {
  if (!confirm('Are you sure you want to delete the route group?')) return
  const exchangeName = item.exchangeName
  const list = routingMap.value[exchangeName]
  const index = list.indexOf(item)
  if (index >= 0) {
    list.splice(index, 1)
  }
  await saveRoutings()
}

async function saveRoutings() {
  const allRoutings = []
  for (const exchangeName of routingExchangeNames.value) {
    const routings = routingMap.value[exchangeName]
    allRoutings.push(...routings)
  }
  try {
    await rabbitService.saveRoutings(allRoutings)
  } catch (error) {
    console.error('Failed to save route group', error)
    alert('Failed to save route group: ' + (error.response?.data || error.message))
  }
}

function moveUp(routing) {
  move(routing, true)
}

function moveDown(routing) {
  move(routing, false)
}

function move(routing, up) {
  const exchangeName = routing.exchangeName
  const list = routingMap.value[exchangeName]
  const index = list.indexOf(routing)
  if (up) {
    if (index === 0) return
    list.splice(index, 1)
    list.splice(index - 1, 0, routing)
  } else {
    if (index + 1 === list.length) return
    list.splice(index, 1)
    list.splice(index + 1, 0, routing)
  }
  saveRoutings()
}

async function getServices() {
  try {
    const services = await servicesService.getAllServices()
    serviceMapById.value = {}
    for (const service of services) {
      serviceMapById.value[service.uuid] = service
    }
  } catch (error) {
    console.error('Failed to load services', error)
  }
}

async function getRoutingOverrides() {
  try {
    routingOverrides.value = await rabbitService.getOverrides()
  } catch (error) {
    console.error('Failed to get routing overrides', error)
  }
}

async function saveRoutingOverrides() {
  try {
    await rabbitService.saveOverrides(routingOverrides.value)
  } catch (error) {
    console.error('Failed to save routing overrides', error)
  }
}

function getServiceDesc(override) {
  if (!override.serviceId || !serviceMapById.value[override.serviceId]) return ''
  const service = serviceMapById.value[override.serviceId]
  return service.localIdentifier + ', ' + service.name
}

function addNewOverride() {
  editingOverride.value = {
    serviceId: '',
    exchangeName: '',
    routingKey: ''
  }
  showRoutingOverrideDialog.value = true
}

async function onOverrideSaved(override) {
  routingOverrides.value.push(override)
  await saveRoutingOverrides()
  showRoutingOverrideDialog.value = false
}

async function deleteRoutingOverride(override) {
  if (!confirm('Are you sure you want to delete the routing override?')) return
  const index = routingOverrides.value.indexOf(override)
  if (index >= 0) {
    routingOverrides.value.splice(index, 1)
  }
  await saveRoutingOverrides()
}
</script>

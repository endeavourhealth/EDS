<template>
  <div v-if="visible" class="modal-overlay" style="display: flex; align-items: center; justify-content: center; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 1000;" @click.self="close">
    <div class="modal-content" style="background: white; border-radius: 6px; padding: 20px; max-width: 600px; width: 90%; max-height: 90vh; overflow-y: auto; position: relative;">
      <div class="modal-header" style="margin-bottom: 15px; font-weight: bold;">Deploy JAR</div>
      <div class="form-group">
        <div class="checkbox">
          <label>
            <input type="checkbox" v-model="selectAll" @change="toggleSelectAll">
            Select All
          </label>
        </div>
      </div>

      <div style="max-height: 300px; overflow-y: auto; border: 1px solid #ccc; padding: 10px;">
        <div v-for="instance in instances" :key="instance.configId" class="checkbox">
          <label>
            <input type="checkbox" v-model="instance.selected">
            {{ instance.configId }} - {{ instance.queueName }} ({{ instance.hostName }})
          </label>
        </div>
      </div>
      <div class="modal-footer" style="margin-top: 15px; text-align: right;">
        <button type="button" class="p-button p-button-primary p-button-sm" @click="deploy" :disabled="deploying">
          Deploy
        </button>
        <button type="button" class="p-button p-button-sm" @click="close">Cancel</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import * as api from '@/services/api.js'

const props = defineProps({
  instances: { type: Array, required: true }
})

const emit = defineEmits(['close', 'deployed'])

const visible = ref(true)
const selectAll = ref(false)
const deploying = ref(false)

const localInstances = ref(props.instances.map(inst => ({ ...inst, selected: false })))

watch(localInstances, (val) => {
  selectAll.value = val.length > 0 && val.every(inst => inst.selected)
}, { deep: true })

function toggleSelectAll() {
  localInstances.value.forEach(inst => {
    inst.selected = selectAll.value
  })
}

function close() {
  visible.value = false
  emit('close')
}

async function deploy() {
  const selected = localInstances.value.filter(inst => inst.selected)
  if (selected.length === 0) {
    alert('Please select at least one instance')
    return
  }

  deploying.value = true
  const configIds = selected.map(inst => inst.configId)

  try {
    await api.queueReaderCommand(configIds)
    emit('deployed')
  } catch (err) {
    console.error('Failed to deploy', err)
  } finally {
    deploying.value = false
    close()
  }
}
</script>

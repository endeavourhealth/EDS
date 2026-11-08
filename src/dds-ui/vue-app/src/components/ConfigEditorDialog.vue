<template>
  <div v-if="visible" class="modal-overlay" style="display: flex; align-items: center; justify-content: center; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 1000;" @click.self="close">
    <div class="modal-content" style="background: white; border-radius: 6px; padding: 20px; max-width: 500px; width: 90%; max-height: 90vh; overflow-y: auto; position: relative;">
      <div class="modal-header" style="margin-bottom: 15px; font-weight: bold;">Edit Memory Configuration</div>
      <div class="form-horizontal">
        <div class="form-group">
          <label class="control-label col-sm-4">Config ID</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" :value="configId" disabled>
          </div>
        </div>

        <div class="form-group">
          <label class="control-label col-sm-4">Queue Type</label>
          <div class="col-sm-8">
            <input type="text" class="form-control" :value="queueType" disabled>
          </div>
        </div>

        <div class="form-group">
          <div class="col-sm-12">
            <label>
              <input type="checkbox" v-model="useDefaults">
              Use queue type defaults
            </label>
          </div>
        </div>

        <div class="form-group" v-if="!useDefaults">
          <label class="control-label col-sm-4">Max Heap (MB)</label>
          <div class="col-sm-8">
            <input type="number" v-model="maxHeapMb" class="form-control" :disabled="useDefaults">
          </div>
        </div>

        <div class="form-group" v-if="!useDefaults">
          <label class="control-label col-sm-4">Min Heap (MB)</label>
          <div class="col-sm-8">
            <input type="number" v-model="minHeapMb" class="form-control" :disabled="useDefaults">
          </div>
        </div>
      </div>
      <div class="modal-footer" style="margin-top: 15px; text-align: right;">
        <button type="button" class="p-button p-button-primary p-button-sm" @click="save" :disabled="saving">Save</button>
        <button type="button" class="p-button p-button-sm" @click="close">Cancel</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import * as api from '@/services/api.js'

const props = defineProps({
  configId: { type: String, required: true },
  queueType: { type: String, required: true },
  memoryDefaults: { type: Object, required: true }
})

const emit = defineEmits(['close', 'saved'])

const visible = ref(true)
const useDefaults = ref(true)
const maxHeapMb = ref(props.memoryDefaults?.maxHeapMb || '')
const minHeapMb = ref(props.memoryDefaults?.minHeapMb || '')
const saving = ref(false)

watch(useDefaults, (val) => {
  if (val && props.memoryDefaults) {
    maxHeapMb.value = props.memoryDefaults.maxHeapMb
    minHeapMb.value = props.memoryDefaults.minHeapMb
  }
})

function close() {
  visible.value = false
  emit('close')
}

async function save() {
  if (useDefaults.value) {
    saving.value = true
    try {
      await api.deleteMemoryConfig(props.configId)
      emit('saved')
    } catch (err) {
      console.error('Failed to save memory config', err)
    } finally {
      saving.value = false
      close()
    }
  } else {
    if (isNaN(maxHeapMb.value) || isNaN(minHeapMb.value)) {
      alert('Please enter valid numbers')
      return
    }
    saving.value = true
    try {
      await api.saveMemoryConfig({
        configId: props.configId,
        maxHeapMb: parseInt(maxHeapMb.value),
        minHeapMb: parseInt(minHeapMb.value)
      })
      emit('saved')
    } catch (err) {
      console.error('Failed to save memory config', err)
    } finally {
      saving.value = false
      close()
    }
  }
}
</script>

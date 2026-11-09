<template>
  <div v-if="visible" class="modal-overlay" style="display: flex; align-items: center; justify-content: center; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 1000;" @click.self="close">
    <div class="modal-content" style="background: white; border-radius: 6px; padding: 20px; max-width: 600px; width: 90%; max-height: 90vh; overflow-y: auto; position: relative;">
      <div class="modal-header" style="margin-bottom: 15px; font-weight: bold;">{{ (isEdit ? 'Edit' : 'Add') + ' Queue Reader Server' }}</div>
      <form class="form-horizontal">
        <div class="form-group">
          <label class="control-label col-sm-3">Server Number</label>
          <div class="col-sm-9">
            <input type="text" v-model="serverNumber" class="form-control" placeholder="e.g. 1" :disabled="isEdit">
          </div>
        </div>

        <div class="form-group">
          <label class="control-label col-sm-3">IP Address</label>
          <div class="col-sm-9">
            <input type="text" v-model="ipAddress" class="form-control" placeholder="e.g. 10.0.0.1">
          </div>
        </div>

        <div class="form-group">
          <label class="control-label col-sm-3">AWS Instance ID</label>
          <div class="col-sm-9">
            <input type="text" v-model="awsInstanceId" class="form-control" placeholder="e.g. i-1234567890abcdef0">
          </div>
        </div>

        <div class="form-group">
          <label class="control-label col-sm-3">Queues</label>
          <div class="col-sm-9">
            <div style="max-height: 200px; overflow-y: auto; border: 1px solid #ccc; padding: 10px;">
              <div v-for="queue in allQueues" :key="queue" class="checkbox">
                <label>
                  <input type="checkbox" :value="queue" v-model="selectedQueues">
                  {{ queue }}
                </label>
              </div>
            </div>
          </div>
        </div>
      </form>
      <div class="modal-footer" style="margin-top: 15px; text-align: right;">
        <button type="button" class="p-button p-button-primary p-button-sm" @click="save" :disabled="saving">
          {{ isEdit ? 'Update' : 'Add' }}
        </button>
        <button type="button" class="p-button p-button-sm" @click="close">Cancel</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as api from '@/services/api.js'

const props = defineProps({
  server: { type: Object, default: null },
  allQueues: { type: Array, default: () => [] }
})

const emit = defineEmits(['close', 'saved'])

const visible = ref(true)
const serverNumber = ref('')
const ipAddress = ref('')
const awsInstanceId = ref('')
const selectedQueues = ref([])
const saving = ref(false)

const isEdit = ref(!!props.server)

onMounted(() => {
  if (props.server) {
    serverNumber.value = props.server.serverNumber
    ipAddress.value = props.server.ipAddress
    awsInstanceId.value = props.server.awsInstanceId || ''
    selectedQueues.value = props.server.queues ? props.server.queues.trim().split(/\s+/).filter(q => q) : []
  }
})

function close() {
  visible.value = false
  emit('close')
}

async function save() {
  if (!serverNumber.value || !ipAddress.value) {
    alert('Please enter server number and IP address')
    return
  }

  saving.value = true
  const serverData = {
    serverNumber: serverNumber.value,
    ipAddress: ipAddress.value,
    awsInstanceId: awsInstanceId.value,
    queues: selectedQueues.value.join(' ')
  }

  try {
    if (isEdit.value) {
      await api.editServer(serverNumber.value, serverData)
    } else {
      await api.addServer(serverData)
    }
    emit('saved')
  } catch (err) {
    console.error('Failed to save server', err)
  } finally {
    saving.value = false
    close()
  }
}
</script>

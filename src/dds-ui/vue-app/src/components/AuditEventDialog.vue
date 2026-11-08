<template>
  <div v-if="visible" class="modal-overlay" style="display: flex; align-items: center; justify-content: center; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 1000;" @click.self="close">
    <div class="modal-content" style="background: white; border-radius: 6px; padding: 20px; max-width: 700px; width: 90%; max-height: 90vh; overflow-y: auto; position: relative;">
      <div class="modal-header" style="margin-bottom: 15px; font-weight: bold;">Audit event : {{ title }}</div>
      <form class="container-fluid">
        <div class="row">
          <div class="form-group col-md-12">
            <label class="control-label">Event Details</label>
            <pre class="stackTrace">{{ getFormattedEventData() }}</pre>
          </div>
        </div>
      </form>
      <div class="modal-footer" style="margin-top: 15px; text-align: right;">
        <button type="button" class="p-button p-button-sm" @click="close">Close</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  auditEvent: { type: Object, required: true }
})

const emit = defineEmits(['close'])

const visible = ref(true)

function getFormattedEventData() {
  if (!props.auditEvent.data) return ''
  const arrayOfLines = props.auditEvent.data.match(/[^\r\n]+/g)
  if (!arrayOfLines || arrayOfLines.length === 0) return ''

  let formattedData = ''
  for (let i = 1; i < arrayOfLines.length; i += 2) {
    formattedData += arrayOfLines[i] + ' : ' + (arrayOfLines[i + 1] || '') + '\n'
  }
  return formattedData
}

function close() {
  visible.value = false
  emit('close')
}
</script>

<style scoped>
.stackTrace {
  white-space: pre-wrap;
  word-wrap: break-word;
  background-color: #f5f5f5;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  max-height: 400px;
  overflow-y: auto;
}
</style>

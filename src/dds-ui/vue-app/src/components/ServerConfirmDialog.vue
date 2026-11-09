<template>
  <p-dialog
    v-model:visible="visible"
    :header="title"
    :style="{ width: '450px' }"
    :modal="true"
    :closable="true"
    @hide="close"
  >
    <div class="confirm-dialog-body">
      <i class="fa-solid fa-triangle-exclamation" style="font-size: 2rem; color: var(--yellow-600); margin-bottom: 1rem;"></i>
      <p style="margin-bottom: 1rem; color: var(--text-color);">{{ message }}</p>
      
      <div v-if="requireSecondConfirm" class="second-confirm-box">
        <p style="color: var(--red-600); font-weight: 600; margin-bottom: 0.5rem;">
          <i class="fa-solid fa-circle-exclamation"></i> FINAL CONFIRMATION REQUIRED
        </p>
        <p style="color: var(--text-color); margin: 0;">Are you absolutely sure? This will {{ finalAction }}.</p>
      </div>
    </div>

    <template #footer>
      <p-button label="Cancel" @click="close" severity="secondary" :rounded="true" />
      <p-button 
        v-if="!requireSecondConfirm || secondConfirmEntered" 
        :label="confirmLabel" 
        @click="confirm" 
        :severity="confirmSeverity" 
        :rounded="true" 
        :disabled="actionInProgress"
      />
      <p-button 
        v-else
        label="Yes, Confirm" 
        @click="enterSecondConfirm" 
        severity="danger" 
        :rounded="true"
      />
    </template>
  </p-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: 'Confirm Action' },
  message: { type: String, default: 'Are you sure?' },
  requireSecondConfirm: { type: Boolean, default: false },
  finalAction: { type: String, default: 'proceed' },
  confirmLabel: { type: String, default: 'Confirm' },
  confirmSeverity: { type: String, default: 'danger' }
})

const emit = defineEmits(['update:visible', 'confirmed', 'closed'])

const visible = ref(props.visible)
const actionInProgress = ref(false)
const secondConfirmEntered = ref(false)

watch(() => props.visible, (val) => {
  visible.value = val
  secondConfirmEntered.value = false
})

watch(visible, (val) => {
  emit('update:visible', val)
})

function enterSecondConfirm() {
  secondConfirmEntered.value = true
  confirm()
}

function confirm() {
  actionInProgress.value = true
  emit('confirmed', () => {
    actionInProgress.value = false
    close()
  })
}

function close() {
  actionInProgress.value = false
  secondConfirmEntered.value = false
  emit('closed')
  emit('update:visible', false)
}
</script>

<style scoped>
.confirm-dialog-body {
  text-align: center;
  padding: 0.5rem 0;
}

.second-confirm-box {
  background-color: var(--red-50);
  border: 1px solid var(--red-200);
  border-radius: var(--border-radius-md);
  padding: 1rem;
  margin-top: 1rem;
}
</style>

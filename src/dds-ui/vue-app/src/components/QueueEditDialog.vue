<template>
  <div v-if="visible" class="modal-overlay" style="display: flex; align-items: center; justify-content: center; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 1000;" @click.self="close">
    <div class="modal-content" style="background: white; border-radius: 6px; padding: 20px; max-width: 600px; width: 90%; max-height: 90vh; overflow-y: auto; position: relative;">
      <div class="modal-header" style="margin-bottom: 15px; font-weight: bold;">Route Group</div>
      <form class="container-fluid">
        <div class="row">
          <div class="form-group col-md-12">
            <label class="control-label">Exchange</label>
            <input type="text" class="form-control" placeholder="Exchange name" v-model="routing.exchangeName" name="exchange" :disabled="!allowEditingExchange">
          </div>
        </div>
        <div class="row">
          <div class="form-group col-md-12">
            <label class="control-label">Description</label>
            <input type="text" class="form-control" placeholder="Description" v-model="routing.description" name="description">
          </div>
        </div>
        <div class="row">
          <div class="form-group col-md-12">
            <label class="control-label">Regex Filter</label>
            <input type="text" class="form-control" placeholder="Filter" v-model="routing.regex" name="regex">
          </div>
        </div>
        <div class="row">
          <div class="form-group col-md-12">
            <label class="control-label"></label>
            <button type="button" class="p-button p-button-secondary p-button-sm" @click="addFilter('[A-Z]')">A-Z</button>
            <button type="button" class="p-button p-button-secondary p-button-sm" @click="addFilter('[0-9]')">0-9</button>
            <button type="button" class="p-button p-button-secondary p-button-sm" @click="addFilter('[A-Z,0-9]')">A-9</button>
          </div>
        </div>
        <div class="row">
          <div class="form-group col-md-12">
            <label class="control-label">Routing Key</label>
            <input type="text" class="form-control" placeholder="Route Key" v-model="routing.routeKey" name="routeKey">
          </div>
        </div>
      </form>
      <div class="modal-footer" style="margin-top: 15px; text-align: right;">
        <button type="button" class="p-button p-button-sm" @click="close">Cancel</button>
        <button type="button" class="p-button p-button-primary p-button-sm" @click="save">Ok</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  routing: { type: Object, required: true },
  allowEditingExchange: { type: Boolean, default: true }
})

const emit = defineEmits(['close', 'saved'])

const visible = ref(true)
const routing = ref({ ...props.routing })

watch(() => props.routing, (newVal) => {
  routing.value = { ...newVal }
})

function addFilter(filter) {
  routing.value.regex += filter
}

function save() {
  emit('saved', { ...routing.value })
  close()
}

function close() {
  visible.value = false
  emit('close')
}
</script>

<template>
  <div class="module">
    <div class="module-heading">
      {{ title || 'Page Under Construction' }}
    </div>
    <div class="panel panel-info">
      <div class="panel-body text-center">
        <i class="fa fa-wrench fa-3x text-muted"></i>
        <h4 class="text-muted mt-3">This page is being migrated to Vue 3</h4>
        <p>Please check back later or use the Queue Server Admin tab.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: 'EDS Page',
  },
})
</script>

<style scoped>
.text-muted { color: #95a5a6; }
.mt-3 { margin-top: 1rem; }
</style>

<template>
  <div class="module">
    <!-- Header -->
    <div class="module-heading">
      Database Statistics {{ getDateDiffSimpleFromNow(statusLastRefreshed) }}
      <div class="pull-right">
        <button class="p-button p-button-success p-button-sm" @click="refreshScreen" :disabled="refreshingStatus">Refresh All</button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="refreshingStatus" class="text-center">
      <i class="fa fa-spinner fa-spin"></i> Loading database statistics...
    </div>

    <!-- Database Tree View -->
    <div v-else-if="databaseServers?.length > 0">
      <div v-for="server in databaseServers" :key="server.host" style="margin-bottom: 20px;">
        <!-- Host Header (clickable to expand/collapse) -->
        <div @click="toggleExpand(server.host)" style="cursor:pointer; background-color: #f5f5f5; padding: 10px; border-radius: 4px; margin-bottom: 10px;">
          <strong>
            <i :class="expandedHosts.includes(server.host) ? 'fa fa-chevron-down' : 'fa fa-chevron-right'" style="margin-right: 10px;"></i>
            {{ server.host }} ({{ server.type }})
          </strong>
        </div>

        <!-- Databases Table (shown when host is expanded) -->
        <table v-if="expandedHosts.includes(server.host)" class="table table-striped table-condensed" style="margin-left: 30px;">
          <thead>
            <tr>
              <th>Database Name</th>
              <th>Size (MB)</th>
              <th>Last Updated</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="db in databasesByHost[server.host]" :key="db.name">
              <td><strong>{{ db.name }}</strong></td>
              <td>{{ db.sizeMb?.toLocaleString() || '-' }} MB</td>
              <td>{{ getDateDiffSimpleFromNow(db.lastUpdated) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="alert alert-info text-center">
      No database servers found.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as databaseStatsService from '@/services/databaseStats.service.js'
import { DateTimeFormatter } from '@/utils/DateTimeFormatter.js'

const getDateDiffSimpleFromNow = DateTimeFormatter.getDateDiffSimpleFromNow

const databaseServers = ref([])
const databasesByHost = ref({})
const statusLastRefreshed = ref(null)
const refreshingStatus = ref(false)
const expandedHosts = ref([])

onMounted(() => { refreshScreen() })

async function refreshScreen() {
  statusLastRefreshed.value = new Date()
  refreshingStatus.value = true
  try {
    const result = await databaseStatsService.getDatabaseServers()
    databaseServers.value = result || []
    
    // Load databases for each host
    for (const server of databaseServers.value) {
      const dbResult = await databaseStatsService.getDatabases(server.type, server.host)
      databasesByHost.value[server.host] = dbResult || []
    }
  } catch (err) { console.error('Failed to load database stats', err) }
  finally { refreshingStatus.value = false }
}

function toggleExpand(host) {
  const idx = expandedHosts.value.indexOf(host)
  if (idx === -1) {
    expandedHosts.value.push(host)
  } else {
    expandedHosts.value.splice(idx, 1)
  }
}
</script>

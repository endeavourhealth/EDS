<template>
  <div class="module">
    <div class="module-heading">
      Queue Server Administration
      <span style="font-size: 0.875rem; color: var(--text-muted-color); margin-left: 0.5rem;">
        {{ getTimeAgo(statusLastRefreshed) }}
      </span>
      <span v-if="quickestNode && quickestNode.address" style="font-size: 0.875rem; margin-left: 1rem;">
        <i class="fa-solid fa-circle" :style="{ color: quickestNode.ping < 100 ? 'var(--green-600)' : quickestNode.ping < 500 ? 'var(--yellow-600)' : 'var(--red-600)', fontSize: '0.5rem' }"></i>
        RabbitMQ: {{ quickestNode.address }} ({{ quickestNode.ping }}ms)
      </span>
      <span v-else-if="rabbitConnectionError" style="font-size: 0.875rem; margin-left: 1rem; color: var(--red-600);">
        <i class="fa-solid fa-circle-xmark"></i>
        RabbitMQ: Connection failed
      </span>
      <div class="pull-right">
        <p-button
          label="Refresh All"
          icon="fa-solid fa-refresh"
          severity="success"
          size="small"
          :loading="actionInProgress"
          @click="refreshAll"
        />
      </div>
    </div>

    <div v-if="actionMessage" class="alert" :class="actionError ? 'alert-danger' : 'alert-info'" role="alert">
      <i :class="actionError ? 'fa-solid fa-circle-xmark' : 'fa-solid fa-circle-info'"></i>
      {{ actionMessage }}
    </div>

    <p-tabView>
      <!-- Tab 1: Server Config -->
      <p-tabPanel header="Server Config" :disabled="false">
        <div class="module" style="margin-bottom: 1.5rem;">
          <div class="module-heading">
            Queue Reader Servers ({{ servers.length }})
            <p-button
              label="Add Server"
              icon="fa-solid fa-plus"
              severity="success"
              size="small"
              @click="openAddServerDialog"
            />
          </div>
          <div v-if="serversLoading" class="text-center" style="padding: 2rem;">
            <p-progressspinner style="width: 40px; height: 40px" strokeWidth="8" animationDuration=".5em" />
            <p style="color: var(--text-muted-color); margin-top: 0.5rem;">Loading servers...</p>
          </div>
          <div v-else-if="!servers.length" class="text-center text-muted" style="padding: 2rem;">
            No queue reader servers found in configuration.
          </div>
          <table v-else class="table table-striped table-condensed" style="width: 100%;">
            <thead>
              <tr>
                <th style="width: 80px;">Server #</th>
                <th style="width: 150px;">Hostname</th>
                <th style="width: 130px;">IP Address</th>
                <th style="width: 110px;">EC2 State</th>
                <th style="width: 120px;">Physical Memory</th>
                <th style="width: 120px;">Heap Usage</th>
                <th style="width: 80px;">Queues</th>
                <th style="width: 300px;" class="text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in servers" :key="row.serverNumber"
                  :class="{ 'selection': selectedServer?.serverNumber === row.serverNumber }"
                  @click="selectedServer = row" style="cursor:pointer;">
                <td><strong>QR{{ row.serverNumber }}</strong></td>
                <td>{{ row.hostName }}</td>
                <td>{{ row.ipAddress }}</td>
                <td>
                  <p-tag
                    :value="row.ec2State || 'Unknown'"
                    :severity="getEc2StateSeverity(row.ec2State)"
                    :rounded="true"
                  />
                </td>
                <td>{{ serverMemoryMap[row.hostName]?.physical || '-' }}</td>
                <td v-if="serverMemoryMap[row.hostName]">
                  {{ serverMemoryMap[row.hostName]?.heapUsed }} / {{ serverMemoryMap[row.hostName]?.heapMax }}
                </td>
                <td v-else class="text-muted">-</td>
                <td>
                  <p-tag
                    v-if="row.queues"
                    :value="row.queues.split(' ').length + ' queue(s)'"
                    severity="info"
                    :rounded="true"
                  />
                  <span v-else class="text-muted">None</span>
                </td>
                <td class="text-right" @click.stop>
                  <div class="d-flex gap-2" style="justify-content: flex-end; flex-wrap: wrap;">
                    <p-button
                      v-if="row.queues"
                      icon="fa-solid fa-stop"
                      severity="warning"
                      size="small"
                      text
                      @click="stopAllQueuesForServer(row)"
                      :disabled="actionInProgress"
                      title="Stop all queues"
                    />
                    <p-button
                      v-if="row.queues"
                      icon="fa-solid fa-rotate"
                      severity="warning"
                      size="small"
                      text
                      @click="restartAllQueuesForServer(row)"
                      :disabled="actionInProgress"
                      title="Restart all queues"
                    />
                    <p-button
                      v-if="row.ec2State === 'stopped'"
                      icon="fa-solid fa-play"
                      severity="success"
                      size="small"
                      text
                      @click="ec2StartServer(row)"
                      :disabled="actionInProgress"
                      title="EC2 Start"
                    />
                    <p-button
                      v-if="row.ec2State === 'running'"
                      icon="fa-solid fa-power-off"
                      severity="danger"
                      size="small"
                      text
                      @click="ec2StopServer(row)"
                      :disabled="actionInProgress"
                      title="EC2 Stop"
                    />
                    <p-button
                      icon="fa-solid fa-upload"
                      severity="info"
                      size="small"
                      text
                      @click="triggerJarUpload(row)"
                      :disabled="actionInProgress"
                      title="Upload JAR"
                    />
                    <p-button
                      icon="fa-solid fa-edit"
                      severity="secondary"
                      size="small"
                      text
                      @click="openEditServerDialog(row)"
                      title="Edit server"
                    />
                    <p-button
                      icon="fa-solid fa-trash"
                      severity="danger"
                      size="small"
                      text
                      @click="confirmDeleteServer(row)"
                      title="Delete server"
                    />
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Instance Management -->
        <div class="module" style="margin-bottom: 1.5rem;">
          <div class="module-heading" style="cursor: pointer;" @click="showInstances = !showInstances">
            <i :class="showInstances ? 'fa-solid fa-chevron-down' : 'fa-solid fa-chevron-right'"></i>
            Queue Reader Instances ({{ instances.length }})
            <p-button
              label="Deploy All"
              icon="fa-solid fa-rocket"
              severity="warning"
              size="small"
              :loading="actionInProgress"
              @click.stop="deployAll"
            />
          </div>
          <div v-if="showInstances">
            <table v-if="instances.length" class="table table-striped table-condensed" style="width: 100%;">
              <thead>
                <tr>
                  <th style="width: 40px;"></th>
                  <th style="width: 200px;">Config ID</th>
                  <th style="width: 200px;">Queue Name</th>
                  <th style="width: 100px;">Type</th>
                  <th style="width: 150px;">Host</th>
                  <th style="width: 150px;">Heap</th>
                  <th style="width: 200px;" class="text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in instances" :key="row.configId">
                  <td>
                    <i :class="getStatusIconClass(row.status)" :title="row.status"></i>
                  </td>
                  <td><strong>{{ row.configId }}</strong></td>
                  <td>{{ row.queueName || '-' }}</td>
                  <td><p-tag :value="row.queueType" severity="info" :rounded="true" /></td>
                  <td>{{ row.hostName }}</td>
                  <td v-if="row.currentHeapDesc && row.maxHeapDesc">
                    {{ row.currentHeapDesc }} / {{ row.maxHeapDesc }}
                  </td>
                  <td v-else class="text-muted">-</td>
                  <td class="text-right" @click.stop>
                    <div class="d-flex gap-2" style="justify-content: flex-end; flex-wrap: wrap;">
                      <p-button
                        v-if="row.status === 'running'"
                        label="Stop"
                        severity="danger"
                        size="small"
                        @click="stopInstance(row)"
                      />
                      <p-button
                        v-else
                        label="Start"
                        severity="success"
                        size="small"
                        @click="restartInstance(row)"
                      />
                      <p-button
                        label="Config"
                        severity="warning"
                        size="small"
                        @click="editConfig(row)"
                      />
                      <p-button
                        label="Deploy"
                        severity="info"
                        size="small"
                        @click="deployToInstance(row)"
                      />
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-else-if="!instancesLoading" class="text-center text-muted" style="padding: 2rem;">
              No instances found. Deploy all or check configuration.
            </div>
            <div v-else class="text-center" style="padding: 2rem;">
              <p-progressspinner style="width: 40px; height: 40px" strokeWidth="8" animationDuration=".5em" />
            </div>
          </div>
        </div>
      </p-tabPanel>

      <!-- Tab 2: Queue Admin -->
      <p-tabPanel header="Queue Admin" :disabled="false">
        <div class="module" style="margin-bottom: 1.5rem;">
          <div class="module-heading" style="cursor: pointer;" @click="showMemoryDefaults = !showMemoryDefaults">
            <i :class="showMemoryDefaults ? 'fa-solid fa-chevron-down' : 'fa-solid fa-chevron-right'"></i>
            Memory Defaults by Queue Type
          </div>
          <div v-if="showMemoryDefaults">
          <table class="table table-striped table-condensed" style="width: 100%;">
            <thead>
              <tr>
                <th style="width: 200px;">Queue Type</th>
                <th style="width: 180px;">Default Max Heap</th>
                <th style="width: 100px;" class="text-right">Action</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, index) in memoryDefaultsList" :key="index">
                <td>
                  <p-tag :value="row.queueType" severity="info" :rounded="true" />
                </td>
                <td>
                  <div style="display: flex; gap: 0.5rem;">
                    <p-inputnumber
                      :model-value="row.maxHeap"
                      @update:model-value="updateMemoryDefault(index, 'maxHeap', $event)"
                      mode="decimal"
                      :min="0"
                      :max="65536"
                      :use-grouping="false"
                      style="width: 120px;"
                    />
                    <p-select
                      :model-value="row.unit"
                      @update:model-value="updateMemoryDefault(index, 'unit', $event)"
                      :options="['MB', 'G']"
                      style="width: 60px;"
                    />
                  </div>
                </td>
                <td class="text-right">
                  <p-button
                    label="Save"
                    severity="primary"
                    size="small"
                    :loading="actionInProgress"
                    @click="saveMemoryDefault(index, row.queueType)"
                  />
                </td>
              </tr>
            </tbody>
          </table>
          </div>
        </div>

        <div class="module" style="margin-bottom: 1.5rem;">
          <div class="module-heading">Create New Queue</div>
          <div style="display: flex; gap: 1rem; flex-wrap: wrap; align-items: flex-end;">
            <div style="flex: 1; min-width: 150px;">
              <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">Queue Type</label>
              <p-dropdown
                v-model="newQueueType"
                :options="queueTypeOptions"
                optionLabel="label"
                optionValue="value"
                placeholder="Select Type"
                fluid
              >
                <template #option=" { option }">
                  <p-tag :value="option.label" :severity="getQueueTypeSeverity(option.value)" :rounded="true" />
                </template>
              </p-dropdown>
            </div>
            <div style="flex: 1; min-width: 150px;">
              <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">Suffix</label>
              <p-inputtext
                v-model="newQueueSuffix"
                placeholder="e.g. A1"
                fluid
              />
            </div>
            <div style="flex: 1; min-width: 150px;">
              <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">Memory (optional)</label>
              <p-inputtext
                v-model="newQueueMemory"
                placeholder="e.g. 512m / 1g"
                fluid
              />
            </div>
            <div style="flex: 2; min-width: 300px;">
              <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">Assign Server</label>
              <p-select
                v-model="newQueueServer"
                :options="serverOptions"
                optionLabel="label"
                optionValue="value"
                placeholder="Select server"
                fluid
              />
            </div>
            <div>
              <p-button
                label="Add Queue"
                icon="fa-solid fa-plus"
                severity="success"
                size="small"
                :loading="actionInProgress"
                @click="addQueue"
              />
            </div>
          </div>
        </div>

        <div class="module" style="margin-bottom: 1.5rem;">
          <div class="module-heading">
            Queue Assignments ({{ queueList.length }} queues)
            <small style="color: var(--text-muted-color);">Changes auto-save when modified</small>
            <div class="pull-right" style="display: flex; gap: 0.5rem; align-items: center;">
              <span v-if="quickestNode && quickestNode.address" style="color: var(--text-muted-color); font-size: 0.875rem;">
                <i class="fa-solid fa-circle" style="color: var(--green-600); font-size: 0.5rem;"></i>
                {{ filteredRabbitQueues.length }} queues from RabbitMQ
              </span>
              <p-button
                icon="fa-solid fa-refresh"
                severity="secondary"
                size="small"
                text
                @click="refreshRabbitQueues"
                :loading="rabbitQueuesLoading"
                title="Refresh queue list"
                :disabled="!quickestNode || !quickestNode.address"
              />
            </div>
          </div>
          <table v-if="queueList.length" class="table table-striped table-condensed" style="width: 100%;">
            <thead>
              <tr>
                <th style="width: 200px;">Queue Name</th>
                <th style="width: 120px;">Type</th>
                <th style="width: 120px;">Memory</th>
                <th>Assigned Servers</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in queueList" :key="row.name">
                <td><strong>{{ row.name }}</strong></td>
                <td><p-tag :value="row.type" :severity="getQueueTypeSeverity(row.type)" :rounded="true" /></td>
                <td>
                  <p-dropdown
                    :model-value="row.memory"
                    @update:model-value="onMemoryChange(row, $event)"
                    :options="memoryOptions"
                    fluid
                    style="width: 100%;"
                  />
                </td>
                <td>
                  <div style="display: flex; flex-wrap: wrap; gap: 0.5rem;">
                    <p-tag
                      v-for="serverNum in row.serverAssignments"
                      :key="serverNum"
                      :value="'QR' + serverNum"
                      severity="success"
                      :rounded="true"
                      :clickable="true"
                      @click="onServerToggle(row.name, serverNum)"
                      style="cursor: pointer;"
                    />
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-else class="text-center text-muted" style="padding: 2rem;">
            No queues configured. Add a server first in the Server Config tab, then create queues.
          </div>
        </div>
      </p-tabPanel>

      <!-- Tab 3: Queue Management -->
      <p-tabPanel header="Queue Management" :disabled="false">
        <div class="module" style="margin-bottom: 1.5rem;">
          <div class="module-heading">
            <i class="fa-solid fa-play" style="color: var(--green-600);"></i>
            Start Queue
            <div class="pull-right" style="display: flex; gap: 0.5rem; align-items: center;">
              <span v-if="quickestNode && quickestNode.address" style="color: var(--text-muted-color); font-size: 0.875rem;">
                <i class="fa-solid fa-circle" style="color: var(--green-600); font-size: 0.5rem;"></i>
                {{ quickestNode.address }}
                <span v-if="rabbitQueuesLastRefreshed"> · {{ getTimeAgo(rabbitQueuesLastRefreshed) }}</span>
              </span>
              <p-button
                icon="fa-solid fa-refresh"
                severity="secondary"
                size="small"
                text
                @click="refreshRabbitQueues"
                :loading="rabbitQueuesLoading"
                title="Refresh queue list"
                :disabled="!quickestNode || !quickestNode.address"
              />
            </div>
          </div>
          <div style="display: flex; gap: 1rem; flex-wrap: wrap; align-items: flex-end;">
            <div style="flex: 1; min-width: 200px;">
              <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">Queue Name</label>
              <p-dropdown
                v-model="startQueueName"
                :options="queueNameOptions"
                optionLabel="label"
                optionValue="value"
                placeholder="Select queue"
                fluid
              />
            </div>
            <div style="flex: 2; min-width: 300px;">
              <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">Start On Servers (Multi-Select)</label>
              <p-select
                v-model="startServers"
                :options="serverOptionsForSelect"
                optionLabel="label"
                optionValue="value"
                multiple
                placeholder="Select one or more servers"
                fluid
              />
            </div>
            <div style="flex: 1; min-width: 150px;">
              <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">Custom Memory (optional)</label>
              <p-inputtext
                v-model="startQueueMemory"
                placeholder="e.g. 1g"
                fluid
              />
            </div>
            <div>
              <p-button
                label="Start"
                icon="fa-solid fa-play"
                severity="success"
                size="small"
                :loading="actionInProgress"
                :disabled="!startQueueName || !startServers?.length"
                @click="startQueueMultiple"
              />
            </div>
          </div>
        </div>

        <div class="module" style="margin-bottom: 1.5rem;">
          <div class="module-heading">
            <i class="fa-solid fa-stop" style="color: var(--red-600);"></i>
            Stop Queue
            <div class="pull-right" style="display: flex; gap: 0.5rem; align-items: center;">
              <span v-if="quickestNode && quickestNode.address" style="color: var(--text-muted-color); font-size: 0.875rem;">
                <i class="fa-solid fa-circle" style="color: var(--green-600); font-size: 0.5rem;"></i>
                {{ quickestNode.address }}
              </span>
              <p-button
                icon="fa-solid fa-refresh"
                severity="secondary"
                size="small"
                text
                @click="refreshRabbitQueues"
                :loading="rabbitQueuesLoading"
                title="Refresh queue list"
                :disabled="!quickestNode || !quickestNode.address"
              />
            </div>
          </div>
          <div style="display: flex; gap: 1rem; flex-wrap: wrap; align-items: flex-end;">
            <div style="flex: 1; min-width: 200px;">
              <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">Queue Name</label>
              <p-dropdown
                v-model="stopQueueName"
                :options="queueNameOptions"
                optionLabel="label"
                optionValue="value"
                placeholder="Select queue"
                fluid
              />
            </div>
            <div style="flex: 1; min-width: 200px;">
              <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">Server</label>
              <p-dropdown
                v-model="stopServer"
                :options="serverOptions"
                optionLabel="label"
                optionValue="value"
                placeholder="Select server"
                fluid
              />
            </div>
            <div>
              <p-button
                label="Stop"
                icon="fa-solid fa-stop"
                severity="danger"
                size="small"
                :loading="actionInProgress"
                :disabled="!stopQueueName || !stopServer"
                @click="stopQueue"
              />
            </div>
          </div>
        </div>

        <div class="module" style="margin-bottom: 1.5rem;">
          <div class="module-heading">
            <i class="fa-solid fa-rotate" style="color: var(--yellow-600);"></i>
            Restart Queue
            <div class="pull-right" style="display: flex; gap: 0.5rem; align-items: center;">
              <span v-if="quickestNode && quickestNode.address" style="color: var(--text-muted-color); font-size: 0.875rem;">
                <i class="fa-solid fa-circle" style="color: var(--green-600); font-size: 0.5rem;"></i>
                {{ quickestNode.address }}
              </span>
              <p-button
                icon="fa-solid fa-refresh"
                severity="secondary"
                size="small"
                text
                @click="refreshRabbitQueues"
                :loading="rabbitQueuesLoading"
                title="Refresh queue list"
                :disabled="!quickestNode || !quickestNode.address"
              />
            </div>
          </div>
          <div style="display: flex; gap: 1rem; flex-wrap: wrap; align-items: flex-end;">
            <div style="flex: 1; min-width: 200px;">
              <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">Queue Name</label>
              <p-dropdown
                v-model="restartQueueName"
                :options="queueNameOptions"
                optionLabel="label"
                optionValue="value"
                placeholder="Select queue"
                fluid
              />
            </div>
            <div style="flex: 1; min-width: 200px;">
              <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">Server</label>
              <p-dropdown
                v-model="restartServer"
                :options="serverOptions"
                optionLabel="label"
                optionValue="value"
                placeholder="Select server"
                fluid
              />
            </div>
            <div>
              <p-button
                label="Restart"
                icon="fa-solid fa-rotate"
                severity="warning"
                size="small"
                :loading="actionInProgress"
                :disabled="!restartQueueName || !restartServer"
                @click="restartQueue"
              />
            </div>
          </div>
        </div>

        <div class="module" style="margin-bottom: 1.5rem;">
          <div class="module-heading">
            <i class="fa-solid fa-stop" style="color: var(--red-600);"></i>
            Stop All Queues on Server
          </div>
          <div style="display: flex; gap: 1rem; flex-wrap: wrap; align-items: flex-end;">
            <div style="flex: 1; min-width: 200px;">
              <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">Server</label>
              <p-dropdown
                v-model="stopAllServer"
                :options="serverOptions"
                optionLabel="label"
                optionValue="value"
                placeholder="Select server"
                fluid
              />
            </div>
            <div>
              <p-button
                label="Stop All Queues"
                icon="fa-solid fa-stop"
                severity="danger"
                size="small"
                :loading="actionInProgress"
                :disabled="!stopAllServer"
                @click="stopAllQueuesForServer(selectedServer || stopAllServer)"
              />
            </div>
          </div>
        </div>

        <div class="module" style="margin-bottom: 1.5rem;">
          <div class="module-heading">
            <i class="fa-solid fa-rocket" style="color: var(--blue-600);"></i>
            Start All Default Queues
          </div>
          <p style="color: var(--text-muted-color); margin-bottom: 1rem;">
            Starts all queues defined in the default configuration using type-based memory presets (Inbound/Transform = 2g, Protocol/Subscriber = 650m).
          </p>
          <p-button
            label="Start All Default Queues"
            icon="fa-solid fa-rocket"
            severity="info"
            size="small"
            :loading="actionInProgress"
            @click="startAllDefaultQueues"
          />
        </div>

        <div class="module">
          <div class="module-heading">Current Queue Status</div>
          <table v-if="instances.length" class="table table-striped table-condensed" style="width: 100%;">
            <thead>
              <tr>
                <th style="width: 40px;"></th>
                <th>Config ID</th>
                <th>Queue Name</th>
                <th>Type</th>
                <th>Host</th>
                <th>Heap</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in instances" :key="row.configId">
                <td><i :class="getStatusIconClass(row.status)"></i></td>
                <td><strong>{{ row.configId }}</strong></td>
                <td>{{ row.queueName || '-' }}</td>
                <td><p-tag :value="row.queueType" :severity="getQueueTypeSeverity(row.queueType)" :rounded="true" /></td>
                <td>{{ row.hostName }}</td>
                <td v-if="row.currentHeapDesc && row.maxHeapDesc">{{ row.currentHeapDesc }} / {{ row.maxHeapDesc }}</td>
                <td v-else class="text-muted">-</td>
                <td>
                  <p-tag
                    :value="row.status"
                    :severity="row.status === 'running' ? 'success' : row.status === 'stopped' ? 'danger' : 'secondary'"
                    :rounded="true"
                  />
                </td>
              </tr>
            </tbody>
          </table>
          <div v-else class="text-center text-muted" style="padding: 2rem;">
            No instances running.
          </div>
        </div>
      </p-tabPanel>

      <!-- Tab 4: Server Operations -->
      <p-tabPanel header="Server Operations" :disabled="false">
        <div class="module" style="margin-bottom: 1.5rem;">
          <div class="module-heading">
            <i class="fa-solid fa-server" style="color: var(--primary-color);"></i>
            EC2 Instance Status
          </div>
          <table class="table table-striped table-condensed" style="width: 100%;">
            <thead>
              <tr>
                <th style="width: 80px;">Server #</th>
                <th style="width: 150px;">Hostname</th>
                <th style="width: 120px;">EC2 State</th>
                <th style="width: 150px;">Instance ID</th>
                <th style="width: 300px;" class="text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in ec2Status" :key="row.serverNumber">
                <td><strong>QR{{ row.serverNumber }}</strong></td>
                <td>{{ row.serverHostName }}</td>
                <td>
                  <p-tag
                    :value="row.state || 'Unknown'"
                    :severity="getEc2StateSeverity(row.state)"
                    :rounded="true"
                  />
                </td>
                <td><code>{{ row.instanceId || 'N/A' }}</code></td>
                <td class="text-right" @click.stop>
                  <div class="d-flex gap-2" style="justify-content: flex-end; flex-wrap: wrap;">
                    <p-button
                      v-if="row.state === 'stopped'"
                      label="EC2 Start"
                      icon="fa-solid fa-play"
                      severity="success"
                      size="small"
                      @click="ec2StartServerFromOps(row)"
                      :disabled="actionInProgress"
                    />
                    <p-button
                      v-if="row.state === 'running'"
                      label="EC2 Stop"
                      icon="fa-solid fa-power-off"
                      severity="danger"
                      size="small"
                      @click="ec2StopServerFromOps(row)"
                      :disabled="actionInProgress"
                    />
                    <p-button
                      label="Shutdown"
                      icon="fa-solid fa-power-off"
                      severity="danger"
                      size="small"
                      @click="shutdownServer(row.serverNumber)"
                      :disabled="actionInProgress"
                    />
                    <p-button
                      label="Reboot"
                      icon="fa-solid fa-rotate"
                      severity="warning"
                      size="small"
                      @click="rebootServer(row.serverNumber)"
                      :disabled="actionInProgress"
                    />
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="module" style="margin-bottom: 1.5rem;">
          <div class="module-heading">
            <i class="fa-solid fa-upload" style="color: var(--blue-600);"></i>
            JAR Upload
          </div>
          <div style="display: flex; gap: 1rem; flex-wrap: wrap; align-items: flex-end;">
            <div style="flex: 1; min-width: 200px;">
              <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">Target Server</label>
              <p-dropdown
                v-model="jarUploadServer"
                :options="servers"
                optionLabel="hostName"
                optionValue="serverNumber"
                placeholder="Select server"
                fluid
              />
            </div>
            <div style="flex: 2; min-width: 300px;">
              <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">JAR File</label>
              <input type="file" ref="jarFileInput" accept=".jar" style="width: 100%; color: var(--text-color);" />
            </div>
            <div>
              <p-button
                label="Upload"
                icon="fa-solid fa-upload"
                severity="info"
                size="small"
                :loading="actionInProgress"
                :disabled="!jarUploadServer"
                @click="uploadJarFile"
              />
            </div>
          </div>
        </div>

        <div class="module">
          <div class="module-heading">
            <i class="fa-solid fa-database" style="color: var(--purple-600);"></i>
            JAR Versions
          </div>
          <table v-if="jarVersions?.length" class="table table-striped table-condensed" style="width: 100%;">
            <thead>
              <tr>
                <th style="width: 200px;">Server</th>
                <th>JAR Information</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in jarVersions" :key="row.serverHostName">
                <td><strong>{{ row.serverHostName }}</strong></td>
                <td><pre style="margin: 0; white-space: pre-wrap; font-family: monospace; font-size: 0.875rem; color: var(--text-color);">{{ row.jarInfo }}</pre></td>
              </tr>
            </tbody>
          </table>
          <div v-else-if="!jarVersionsLoading" class="text-center text-muted" style="padding: 2rem;">
            No JAR version information available.
          </div>
          <div v-else class="text-center" style="padding: 2rem;">
            <p-progressspinner style="width: 40px; height: 40px" strokeWidth="8" animationDuration=".5em" />
          </div>
        </div>
      </p-tabPanel>
    </p-tabView>

    <!-- Dialogs -->
    <p-dialog
      v-model:visible="showServerEditor"
      :header="isEditingServer ? 'Edit Server' : 'Add Server'"
      :style="{ width: '550px' }"
      :modal="true"
      :closable="true"
      @hide="showServerEditor = false"
    >
      <div style="display: flex; flex-direction: column; gap: 1rem;">
        <div>
          <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">Server Number</label>
          <p-inputtext
            v-model="serverForm.serverNumber"
            placeholder="e.g. 1"
            :disabled="isEditingServer"
            fluid
          />
        </div>
        <div>
          <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">IP Address</label>
          <p-inputtext
            v-model="serverForm.ipAddress"
            placeholder="e.g. 10.0.0.1"
            fluid
          />
        </div>
        <div>
          <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">AWS Instance ID</label>
          <p-inputtext
            v-model="serverForm.awsInstanceId"
            placeholder="e.g. i-1234567890abcdef0"
            fluid
          />
        </div>
        <div>
          <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">Queues Assigned</label>
          <div style="display: flex; gap: 0.5rem; margin-bottom: 0.5rem; align-items: center;">
            <span style="color: var(--text-muted-color); font-size: 0.875rem;">{{ rabbitQueueNames.length }} queues from RabbitMQ</span>
            <p-button
              icon="fa-solid fa-refresh"
              severity="secondary"
              size="small"
              text
              @click="refreshRabbitQueues"
              :loading="rabbitQueuesLoading"
              title="Refresh queue list from RabbitMQ"
            />
          </div>
          <div style="max-height: 200px; overflow-y: auto; border: 1px solid var(--surface-border-color); border-radius: var(--border-radius-md); padding: 0.5rem;">
            <p-select
              v-model="serverForm.selectedQueues"
              :options="allQueuesList"
              multiple
              @update:model-value="serverForm.selectedQueues = $event"
              placeholder="Select queues"
              fluid
            />
          </div>
        </div>
      </div>
      <template #footer>
        <p-button label="Cancel" @click="showServerEditor = false" severity="secondary" :rounded="true" />
        <p-button
          :label="isEditingServer ? 'Update' : 'Add'"
          severity="primary"
          :loading="actionInProgress"
          @click="saveServer"
          :rounded="true"
        />
      </template>
    </p-dialog>

    <p-dialog
      v-model:visible="showConfigEditor"
      header="Edit Memory Configuration"
      :style="{ width: '500px' }"
      :modal="true"
      :closable="true"
      @hide="showConfigEditor = false"
    >
      <div style="display: flex; flex-direction: column; gap: 1rem;">
        <div>
          <label style="display: block; margin-bottom: 0.375rem; color: var(--text-muted-color); font-size: 0.875rem;">Config ID</label>
          <p-inputtext :value="currentConfig?.configId" disabled fluid />
        </div>
        <div>
          <label style="display: block; margin-bottom: 0.375rem; color: var(--text-muted-color); font-size: 0.875rem;">Queue Type</label>
          <p-tag :value="currentConfig?.queueType" severity="info" :rounded="true" />
        </div>
        <div>
          <p-select
            v-model="memoryConfigUseDefaults"
            :options="memoryDefaultsOptions"
            @update:model-value="memoryConfigUseDefaults = $event"
            optionLabel="label"
            optionValue="value"
            class="binary-select"
          />
          <label style="margin-left: 0.5rem; color: var(--text-color);">Use queue type defaults</label>
        </div>
        <div v-if="!memoryConfigUseDefaults">
          <label style="display: block; margin-bottom: 0.375rem; color: var(--text-color); font-size: 0.875rem; font-weight: 500;">Max Heap</label>
          <div style="display: flex; gap: 0.5rem;">
            <p-inputnumber
              v-model="memoryConfigMaxHeap"
              :disabled="memoryConfigUseDefaults"
              mode="decimal"
              :min="0"
              fluid
              style="width: 120px;"
            />
            <p-select
              v-model="memoryConfigUnit"
              :disabled="memoryConfigUseDefaults"
              :options="['MB', 'G']"
              style="width: 60px;"
            />
          </div>
        </div>
      </div>
      <template #footer>
        <p-button label="Cancel" @click="showConfigEditor = false" severity="secondary" :rounded="true" />
        <p-button
          label="Save"
          severity="primary"
          :loading="actionInProgress"
          @click="saveMemoryConfig"
          :rounded="true"
        />
      </template>
    </p-dialog>

    <p-dialog
      v-model:visible="showDeployDialog"
      header="Deploy JAR"
      :style="{ width: '600px' }"
      :modal="true"
      :closable="true"
      @hide="showDeployDialog = false"
    >
      <div>
        <div style="margin-bottom: 1rem;">
          <p-select
            v-model="deploySelectAll"
            :options="deploySelectOptions"
            @update:model-value="deploySelectAll = $event"
            optionLabel="label"
            optionValue="value"
            class="binary-select"
          />
          <label style="margin-left: 0.5rem; color: var(--text-color);">Select All</label>
        </div>
        <div style="max-height: 300px; overflow-y: auto;">
          <p-select
            v-model="deploySelectedInstances"
            :options="deployInstanceOptions"
            multiple
            @update:model-value="deploySelectedInstances = $event"
            placeholder="Select instances to deploy"
            fluid
          />
        </div>
      </div>
      <template #footer>
        <p-button label="Cancel" @click="showDeployDialog = false" severity="secondary" :rounded="true" />
        <p-button
          label="Deploy"
          severity="info"
          :loading="actionInProgress"
          @click="deploySelected"
          :rounded="true"
        />
      </template>
    </p-dialog>

    <ServerConfirmDialog
      v-model:visible="showDeleteConfirm"
      title="Delete Server"
      :message="`Are you sure you want to delete QR${deleteConfirmServer?.serverNumber}? This will remove the server from management.`"
      confirm-label="Delete"
      confirm-severity="danger"
      @confirmed="executeDeleteServer"
      @closed="showDeleteConfirm = false"
    />

    <p-toast />
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import * as apiService from '@/services/api'
import * as rabbitService from '@/services/rabbit.service.js'
import ServerConfirmDialog from '@/components/ServerConfirmDialog.vue'
import { useToast } from 'primevue/usetoast'

const toast = useToast()

// Reactive state
const instances = ref([])
const memoryDefaultsList = ref([])
const servers = ref([])
const ec2Status = ref([])
const jarVersions = ref([])
const selectedServer = ref(null)
const queueList = ref([])
const serverMemoryMap = ref({})
const instancesLoading = ref(false)
const serversLoading = ref(false)
const jarVersionsLoading = ref(false)
const statusLastRefreshed = ref(null)

// RabbitMQ state
const rabbitNodes = ref([])
const quickestNode = ref(null)
const rabbitQueues = ref([])
const rabbitQueuesLoading = ref(false)
const rabbitQueuesLastRefreshed = ref(null)
const rabbitConnectionError = ref(null)

// Form state
const newQueueType = ref('inbound')
const newQueueSuffix = ref('')
const newQueueMemory = ref('')
const newQueueServer = ref(null)

// Queue Management state
const startQueueName = ref('')
const startServers = ref([])
const startQueueMemory = ref('')

// Computed
const filteredRabbitQueues = computed(() => {
  return rabbitQueues.value.filter(q => {
    if (q.name.startsWith('amq.')) return false
    if (q.name === 'management') return false
    if (q.name.includes('dlq')) return false
    if (q.name.includes('.dead.')) return false
    return true
  })
})

const rabbitQueueNames = computed(() => {
  return [...new Set(filteredRabbitQueues.value.map(q => q.name))].sort()
})

const allQueuesList = ref([])

const serverOptionsForSelect = computed(() => {
  return servers.value.map(s => ({ label: 'QR' + s.serverNumber + ' (' + s.hostName + ')', value: s.serverNumber }))
})

const memoryDefaultsMap = computed(() => {
  const map = {}
  for (const row of memoryDefaultsList.value) {
    map[row.queueType] = row
  }
  return map
})

function getDefaultMemoryForType(queueType) {
  const defaults = memoryDefaultsMap.value[queueType]
  if (!defaults || !defaults.maxHeap) return '2g'
  const unit = (defaults.unit || 'MB').toUpperCase()
  const suffix = unit === 'G' ? 'g' : 'mb'
  return defaults.maxHeap + suffix
}

const stopQueueName = ref('')
const stopServer = ref('')
const restartQueueName = ref('')
const restartServer = ref('')
const stopAllServer = ref('')

// Action state
const actionInProgress = ref(false)
const actionMessage = ref('')
const actionError = ref(false)

// Dialog state
const showInstances = ref(false)
const showMemoryDefaults = ref(false)
const showServerEditor = ref(false)
const showConfigEditor = ref(false)
const showDeployDialog = ref(false)
const showDeleteConfirm = ref(false)
const isEditingServer = ref(false)
const currentConfig = ref(null)
const currentServer = ref(null)
const deleteConfirmServer = ref(null)
const jarUploadServer = ref(null)
const jarFileInput = ref(null)

// Memory config dialog
const memoryConfigUseDefaults = ref(true)
const memoryConfigMaxHeap = ref(null)
const memoryConfigUnit = ref('MB')

// Deploy dialog
const deployInstancesLocal = ref([])
const deploySelectAll = ref(false)
const deploySelectedInstances = ref([])
const deploySelectOptions = ref([
  { label: 'Unchecked', value: false },
  { label: 'Checked', value: true }
])
const deployInstanceOptions = ref([])

// Server form
const serverForm = ref({
  serverNumber: '',
  ipAddress: '',
  awsInstanceId: '',
  selectedQueues: []
})

// Options
const queueTypeOptions = ref([
  { label: 'Inbound', value: 'inbound' },
  { label: 'Protocol', value: 'protocol' },
  { label: 'Transform', value: 'transform' },
  { label: 'Subscriber', value: 'subscriber' }
])

const memoryOptions = ref(['256m', '512m', '650m', '1g', '2g', '4g'])
const memoryDefaultsOptions = ref([
  { label: 'Unchecked (Custom)', value: false },
  { label: 'Checked (Defaults)', value: true }
])

const serverOptions = ref([])

const queueNameOptions = ref([])

// --- Data Loading ---
async function refreshAll() {
  await Promise.all([
    refreshInstances(),
    refreshMemoryDefaults(),
    refreshServers(),
    refreshEc2Status(),
    refreshJarVersions(),
    refreshRabbitQueues()
  ])
}

async function refreshRabbitNodes() {
  try {
    rabbitNodes.value = await rabbitService.getRabbitNodes()
    if (rabbitNodes.value && rabbitNodes.value.length > 0) {
      await getRabbitNodePings()
    }
  } catch (err) {
    console.error('Failed to load RabbitMQ nodes', err)
    rabbitConnectionError.value = 'Failed to connect to RabbitMQ'
  }
}

async function getRabbitNodePings() {
  if (!rabbitNodes.value || !rabbitNodes.value.length) return
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
}

async function refreshRabbitQueues() {
  if (!quickestNode.value || !quickestNode.value.address) return
  rabbitQueuesLoading.value = true
  rabbitConnectionError.value = null
  try {
    rabbitQueues.value = await rabbitService.getRabbitQueuesFiltered(quickestNode.value.address)
    rabbitQueuesLastRefreshed.value = new Date()
  } catch (err) {
    console.error('Failed to load RabbitMQ queues', err)
    rabbitConnectionError.value = 'Failed to load queues from RabbitMQ'
    rabbitQueues.value = []
    toast.add({ severity: 'warn', summary: 'RabbitMQ Warning', detail: 'Could not connect to RabbitMQ. Using cached queue list.', life: 5000 })
  } finally {
    rabbitQueuesLoading.value = false
  }
}

async function refreshServers() {
  serversLoading.value = true
  try {
    servers.value = await apiService.getServers()
    console.log('Servers loaded:', servers.value, 'length:', servers.value.length)
    if (servers.value.length) selectedServer.value = servers.value[0]
    updateServerOptions()
    buildQueueList()
    getAllQueuesFromInstances()
  } catch (err) {
    console.error('Failed to load servers', err)
  } finally {
    serversLoading.value = false
  }
}

async function refreshInstances() {
  instancesLoading.value = true
  statusLastRefreshed.value = new Date()
  try {
    instances.value = await apiService.getInstances()
    serverMemoryMap.value = {}
    for (const inst of instances.value) {
      if (inst.hostName && inst.physicalMemoryDesc) {
        if (!serverMemoryMap.value[inst.hostName]) {
          serverMemoryMap.value[inst.hostName] = {
            physical: inst.physicalMemoryDesc,
            heapUsed: inst.currentHeapDesc || '-',
            heapMax: inst.maxHeapDesc || '-'
          }
        }
      }
    }
    if (instances.value.length && servers.value.length) {
      const queueMapByServer = {}
      for (const inst of instances.value) {
        if (inst.queueName && inst.hostName) {
          if (!queueMapByServer[inst.hostName]) {
            queueMapByServer[inst.hostName] = new Set()
          }
          queueMapByServer[inst.hostName].add(inst.queueName)
        }
      }
      for (const server of servers.value) {
        const queuesForServer = queueMapByServer[server.hostName]
        if (queuesForServer && queuesForServer.size > 0) {
          const queueArr = [...queuesForServer].sort()
          if (!server.queues || server.queues.trim() === '') {
            server.queues = queueArr.join(' ')
          } else {
            const existingQueues = new Set(server.queues.split(/\s+/).filter(q => q))
            for (const q of queuesForServer) existingQueues.add(q)
            server.queues = [...existingQueues].sort().join(' ')
          }
        }
      }
    }
    getAllQueuesFromInstances()
    buildQueueList()
  } catch (err) {
    console.error('Failed to load instances', err)
  } finally {
    instancesLoading.value = false
  }
}

async function refreshMemoryDefaults() {
  try {
    memoryDefaultsList.value = await apiService.getMemoryDefaults()
  } catch (err) {
    console.error('Failed to load memory defaults', err)
  }
}

async function refreshEc2Status() {
  try {
    ec2Status.value = await apiService.getEc2Status()
    updateEc2States()
  } catch (err) {
    console.error('Failed to load EC2 status', err)
  }
}

async function refreshJarVersions() {
  jarVersionsLoading.value = true
  try {
    jarVersions.value = await apiService.getJarVersions()
  } catch (err) {
    console.error('Failed to load JAR versions', err)
  } finally {
    jarVersionsLoading.value = false
  }
}

// --- Helpers ---
function updateServerOptions() {
  serverOptions.value = servers.value.map(s => ({
    label: `QR${s.serverNumber} (${s.hostName})`,
    value: s.serverNumber
  }))
}

function updateQueueNameOptions() {
  const queueNames = [...rabbitQueueNames.value].sort()
  queueNameOptions.value = queueNames.map(q => ({ label: q, value: q }))
  if (queueNames.length && !startQueueName.value) {
    startQueueName.value = queueNames[0]
  }
}

function getAllQueuesFromInstances() {
  allQueuesList.value = [...rabbitQueueNames.value].sort()
}

function buildQueueList() {
  if (!servers.value.length) { queueList.value = []; return }
  const queueMap = {}
  for (const server of servers.value) {
    if (!server.queues) continue
    for (const qName of server.queues.trim().split(/\s+/)) {
      if (!qName) continue
      if (!queueMap[qName]) {
        let type = 'inbound'
        if (qName.includes('Protocol')) type = 'protocol'
        else if (qName.includes('Transform')) type = 'transform'
        else if (qName.includes('Subscriber')) type = 'subscriber'
        const defaultMemory = getDefaultMemoryForType(type)
        queueMap[qName] = { name: qName, type, memory: defaultMemory, serverAssignments: [] }
      }
      if (!queueMap[qName].serverAssignments.includes(server.serverNumber)) {
        queueMap[qName].serverAssignments.push(server.serverNumber)
      }
    }
  }
  for (const qName of rabbitQueueNames.value) {
    if (!queueMap[qName]) {
      let type = 'inbound'
      if (qName.includes('Protocol')) type = 'protocol'
      else if (qName.includes('Transform')) type = 'transform'
      else if (qName.includes('Subscriber')) type = 'subscriber'
      const defaultMemory = getDefaultMemoryForType(type)
      queueMap[qName] = { name: qName, type, memory: defaultMemory, serverAssignments: [] }
    }
  }
  queueList.value = Object.values(queueMap).sort((a, b) => a.name.localeCompare(b.name))
}

function updateEc2States() {
  for (const server of servers.value) {
    const ec2 = ec2Status.value.find(e => {
      const ec2ServerNum = e.serverNumber?.toString() || ''
      const serverNum = server.serverNumber?.toString() || ''
      return ec2ServerNum === serverNum
    })
    if (ec2) {
      server.ec2State = ec2.state
      server.instanceId = ec2.instanceId || ''
    }
  }
}

function getEc2StateSeverity(state) {
  const l = state?.toLowerCase()
  if (l === 'running') return 'success'
  if (l === 'stopped') return 'danger'
  return 'secondary'
}

function getQueueTypeSeverity(type) {
  const l = type?.toLowerCase()
  if (l === 'inbound') return 'success'
  if (l === 'protocol') return 'info'
  if (l === 'transform') return 'warning'
  if (l === 'subscriber') return 'secondary'
  return 'secondary'
}

function getStatusIconClass(status) {
  const l = status?.toLowerCase()
  if (l === 'running') return 'fa-solid fa-circle'
  if (l === 'stopped') return 'fa-solid fa-circle'
  return 'fa-solid fa-circle'
}

function getTimeAgo(date) {
  if (!date) return ''
  const diff = Date.now() - new Date(date).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return '(just now)'
  if (mins < 60) return `(${mins}m ago)`
  const hrs = Math.floor(mins / 60)
  return `(${hrs}h ${mins % 60}m ago)`
}

function onQueueCheckboxChange(queue, checked) {
  const idx = serverForm.value.selectedQueues.indexOf(queue)
  if (checked && idx === -1) {
    serverForm.value.selectedQueues.push(queue)
  } else if (!checked && idx !== -1) {
    serverForm.value.selectedQueues.splice(idx, 1)
  }
}

// --- Queue Admin Actions ---
function updateMemoryDefault(index, field, value) {
  if (memoryDefaultsList.value[index]) {
    memoryDefaultsList.value[index][field] = value
  }
}

async function saveMemoryDefault(index, queueType) {
  const defaults = memoryDefaultsList.value[index]
  if (!defaults) return

  const maxHeap = parseInt(defaults.maxHeap)
  const unit = defaults.unit || 'MB'
  if (isNaN(maxHeap)) {
    toast.add({ severity: 'warn', summary: 'Warning', detail: 'Please enter a valid number', life: 3000 })
    return
  }

  actionInProgress.value = true
  try {
    await apiService.saveMemoryDefaults(queueType, maxHeap, unit)
    toast.add({ severity: 'success', summary: 'Success', detail: 'Memory defaults saved', life: 3000 })
    await refreshMemoryDefaults()
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to save memory defaults', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

async function addQueue() {
  if (!newQueueSuffix.value?.trim()) {
    toast.add({ severity: 'warn', summary: 'Warning', detail: 'Please enter a queue suffix', life: 3000 })
    return
  }
  if (!newQueueServer.value) {
    toast.add({ severity: 'warn', summary: 'Warning', detail: 'Please select a server', life: 3000 })
    return
  }

  const queueType = newQueueType.value || 'inbound'
  const fullName = queueType.charAt(0).toUpperCase() + queueType.slice(1) + newQueueSuffix.value.trim()
  const memoryValue = newQueueMemory.value || getDefaultMemoryForType(queueType.toLowerCase())
  actionInProgress.value = true

  try {
    await apiService.addQueue({
      name: fullName,
      type: queueType.toLowerCase(),
      memory: memoryValue,
      servers: [newQueueServer.value]
    })
    toast.add({ severity: 'success', summary: 'Success', detail: `Queue "${fullName}" added`, life: 3000 })
    newQueueSuffix.value = ''
    newQueueMemory.value = ''
    newQueueServer.value = null
    await refreshAll()
  } catch (err) {
    console.error('Failed to add queue:', err)
    const errorMsg = err.response?.data?.error || err.message || 'Failed to add queue'
    toast.add({ severity: 'error', summary: 'Error', detail: errorMsg, life: 5000 })
  } finally {
    actionInProgress.value = false
  }
}

async function onServerToggle(queueName, serverNumber) {
  const queue = queueList.value.find(q => q.name === queueName)
  if (!queue) return
  const idx = queue.serverAssignments.indexOf(serverNumber)
  if (idx === -1) queue.serverAssignments.push(serverNumber)
  else queue.serverAssignments.splice(idx, 1)

  try {
    await apiService.saveQueueAssignments(queue.name, queue.type, queue.memory, [...queue.serverAssignments])
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to save assignment', life: 3000 })
  }
}

async function onMemoryChange(queue, memory) {
  queue.memory = memory
  try {
    await apiService.saveQueueAssignments(queue.name, queue.type, queue.memory, [...queue.serverAssignments])
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to save assignment', life: 3000 })
  }
}

// --- Queue Management Actions ---
async function startQueueMultiple() {
  if (!startQueueName.value || !startServers.value?.length) {
    toast.add({ severity: 'warn', summary: 'Warning', detail: 'Please select a queue and at least one server', life: 3000 })
    return
  }

  actionInProgress.value = true
  actionMessage.value = `Starting ${startQueueName.value} on ${startServers.value.length} server(s)...`

  try {
    const result = await apiService.startQueuesOnMultipleServers(
      startQueueName.value,
      startQueueMemory.value || null,
      [...startServers.value]
    )
    actionMessage.value = result.message || 'Queues started successfully'
    toast.add({ severity: 'success', summary: 'Success', detail: result.message, life: 5000 })
    setTimeout(refreshInstances, 3000)
  } catch (err) {
    actionError.value = true
    actionMessage.value = 'Failed: ' + (err.response?.data?.error || err.message)
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to start queues', life: 5000 })
  } finally {
    actionInProgress.value = false
  }
}

async function stopQueue() {
  if (!stopQueueName.value || !stopServer.value) return

  actionInProgress.value = true
  actionMessage.value = `Stopping ${stopQueueName.value} on QR${stopServer.value}...`

  try {
    await apiService.stopQueue(stopServer.value, stopQueueName.value)
    actionMessage.value = 'Queue stopped'
    toast.add({ severity: 'success', summary: 'Success', detail: 'Stop command sent', life: 3000 })
    setTimeout(refreshInstances, 3000)
  } catch (err) {
    actionError.value = true
    actionMessage.value = 'Failed: ' + (err.response?.data?.error || err.message)
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to stop queue', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

async function restartQueue() {
  if (!restartQueueName.value || !restartServer.value) return

  actionInProgress.value = true
  actionMessage.value = `Restarting ${restartQueueName.value} on QR${restartServer.value}...`

  try {
    await apiService.restartQueue(restartServer.value, restartQueueName.value)
    actionMessage.value = 'Queue restart command sent'
    toast.add({ severity: 'success', summary: 'Success', detail: 'Restart command sent', life: 3000 })
    setTimeout(refreshInstances, 3000)
  } catch (err) {
    actionError.value = true
    actionMessage.value = 'Failed: ' + (err.response?.data?.error || err.message)
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to restart queue', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

async function startAllDefaultQueues() {
  if (!confirm('Start all default queues on all servers?\n\nThis will start queues based on the default configuration with type-based memory presets.')) return

  actionInProgress.value = true
  actionMessage.value = 'Starting all default queues...'

  try {
    for (const server of servers.value) {
      await apiService.restartAllQueues(server.serverNumber)
    }
    actionMessage.value = 'All default queues started'
    toast.add({ severity: 'success', summary: 'Success', detail: 'All default queues started', life: 5000 })
    setTimeout(refreshInstances, 5000)
  } catch (err) {
    actionError.value = true
    actionMessage.value = 'Failed: ' + (err.response?.data?.error || err.message)
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to start default queues', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

// --- Server Actions ---
async function stopAllQueuesForServer(server) {
  const serverNum = server?.serverNumber || selectedServer.value?.serverNumber
  if (!serverNum) return
  if (!confirm(`Stop ALL queues on QR${serverNum}?`)) return

  actionInProgress.value = true
  actionMessage.value = `Stopping all queues on QR${serverNum}...`

  try {
    await apiService.stopAllQueues(serverNum)
    actionMessage.value = 'All queues stopped'
    toast.add({ severity: 'success', summary: 'Success', detail: 'All queues stopped', life: 3000 })
    setTimeout(refreshInstances, 3000)
  } catch (err) {
    actionError.value = true
    actionMessage.value = 'Failed: ' + (err.response?.data?.error || err.message)
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to stop all queues', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

async function restartAllQueuesForServer(server) {
  const serverNum = server?.serverNumber || selectedServer.value?.serverNumber
  if (!serverNum) return
  if (!confirm(`Restart ALL queues on QR${serverNum}?`)) return

  actionInProgress.value = true
  actionMessage.value = `Restarting all queues on QR${serverNum}...`

  try {
    await apiService.restartAllQueues(serverNum)
    actionMessage.value = 'All queues restarted'
    toast.add({ severity: 'success', summary: 'Success', detail: 'All queues restarted', life: 3000 })
    setTimeout(refreshInstances, 3000)
  } catch (err) {
    actionError.value = true
    actionMessage.value = 'Failed: ' + (err.response?.data?.error || err.message)
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to restart all queues', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

async function ec2StartServer(server) {
  const serverNum = server?.serverNumber || selectedServer.value?.serverNumber
  if (!serverNum) return
  if (!confirm(`EC2 START QR${serverNum}?\n\nThis will start the AWS EC2 instance.`)) return

  actionInProgress.value = true
  actionMessage.value = `Sending EC2 start command for QR${serverNum}...`

  try {
    await apiService.ec2Start(serverNum)
    actionMessage.value = 'EC2 start command sent'
    toast.add({ severity: 'success', summary: 'Success', detail: 'EC2 start command sent', life: 3000 })
    setTimeout(refreshEc2Status, 5000)
  } catch (err) {
    actionError.value = true
    actionMessage.value = 'Failed: ' + (err.response?.data?.error || err.message)
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to EC2 start', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

async function ec2StopServer(server) {
  const serverNum = server?.serverNumber || selectedServer.value?.serverNumber
  if (!serverNum) return
  if (!confirm(`EC2 STOP QR${serverNum}?\n\nThis will STOP the AWS EC2 instance (power off).`)) return
  if (!confirm(`FINAL CONFIRMATION: EC2 will be STOPPED for QR${serverNum}`)) return

  actionInProgress.value = true
  actionMessage.value = `Sending EC2 stop command for QR${serverNum}...`

  try {
    await apiService.ec2Stop(serverNum)
    actionMessage.value = 'EC2 stop command sent'
    toast.add({ severity: 'warn', summary: 'Warning', detail: 'EC2 stop command sent', life: 5000 })
    setTimeout(refreshEc2Status, 5000)
  } catch (err) {
    actionError.value = true
    actionMessage.value = 'Failed: ' + (err.response?.data?.error || err.message)
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to EC2 stop', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

async function ec2StartServerFromOps(ec2Row) {
  if (!confirm(`EC2 START QR${ec2Row.serverNumber}?`)) return
  actionInProgress.value = true
  try {
    await apiService.ec2Start(ec2Row.serverNumber)
    toast.add({ severity: 'success', summary: 'Success', detail: 'EC2 start command sent', life: 3000 })
    setTimeout(refreshEc2Status, 5000)
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to EC2 start', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

async function ec2StopServerFromOps(ec2Row) {
  if (!confirm(`EC2 STOP QR${ec2Row.serverNumber}?\n\nThis will STOP the AWS EC2 instance.`)) return
  if (!confirm(`FINAL CONFIRMATION: EC2 will be STOPPED for QR${ec2Row.serverNumber}`)) return
  actionInProgress.value = true
  try {
    await apiService.ec2Stop(ec2Row.serverNumber)
    toast.add({ severity: 'warn', summary: 'Warning', detail: 'EC2 stop command sent', life: 5000 })
    setTimeout(refreshEc2Status, 5000)
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to EC2 stop', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

async function shutdownServer(serverNumber) {
  if (!confirm(`Shutdown QR${serverNumber}?\n\nThe server will power off in 1 minute.`)) return
  if (!confirm(`FINAL CONFIRMATION: QR${serverNumber} will be SHUTDOWN.`)) return
  actionInProgress.value = true
  try {
    await apiService.shutdownServer(serverNumber)
    toast.add({ severity: 'warn', summary: 'Warning', detail: 'Shutdown command sent', life: 5000 })
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to shutdown server', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

async function rebootServer(serverNumber) {
  if (!confirm(`Reboot QR${serverNumber}?`)) return
  actionInProgress.value = true
  try {
    await apiService.rebootServer(serverNumber)
    toast.add({ severity: 'warn', summary: 'Warning', detail: 'Reboot command sent', life: 3000 })
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to reboot server', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

// --- Instance Actions ---
async function stopInstance(instance) {
  if (!confirm(`Stop queue reader "${instance.configId}"?`)) return
  actionInProgress.value = true
  try {
    await apiService.stopInstance([instance.configId])
    toast.add({ severity: 'success', summary: 'Success', detail: 'Stop signal sent', life: 3000 })
    setTimeout(refreshInstances, 3000)
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to stop instance', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

async function restartInstance(instance) {
  if (!confirm(`Restart queue reader "${instance.configId}"?`)) return
  actionInProgress.value = true
  try {
    await apiService.restartInstance([instance.configId])
    toast.add({ severity: 'success', summary: 'Success', detail: 'Restart signal sent', life: 3000 })
    setTimeout(refreshInstances, 3000)
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to restart instance', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

function editConfig(instance) {
  currentConfig.value = instance
  memoryConfigUseDefaults.value = true
  memoryConfigMaxHeap.value = instance?.memoryDefaults?.maxHeap || null
  memoryConfigUnit.value = instance?.memoryDefaults?.unit || 'MB'
  showConfigEditor.value = true
}

async function saveMemoryConfig() {
  if (!currentConfig.value) return
  actionInProgress.value = true

  try {
    if (memoryConfigUseDefaults.value) {
      await apiService.deleteMemoryConfig(currentConfig.value.configId)
      toast.add({ severity: 'success', summary: 'Success', detail: 'Using defaults', life: 3000 })
    } else {
      const maxHeap = parseInt(memoryConfigMaxHeap.value)
      const unit = memoryConfigUnit.value
      if (isNaN(maxHeap)) {
        toast.add({ severity: 'warn', summary: 'Warning', detail: 'Please enter a valid number', life: 3000 })
        actionInProgress.value = false
        return
      }
      await apiService.saveMemoryConfig({
        configId: currentConfig.value.configId,
        maxHeap,
        unit
      })
      toast.add({ severity: 'success', summary: 'Success', detail: 'Memory config saved', life: 3000 })
    }
    showConfigEditor.value = false
    setTimeout(refreshInstances, 2000)
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to save memory config', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

function deployToInstance(instance) {
  deployInstancesLocal.value = [{ ...instance, selected: true }]
  deployInstanceOptions.value = instances.value.map(i => ({
    label: `${i.configId} - ${i.queueName} (${i.hostName})`,
    value: i.configId
  }))
  deploySelectedInstances.value = [instance.configId]
  deploySelectAll.value = true
  showDeployDialog.value = true
}

async function deploySelected() {
  if (!deploySelectedInstances.value?.length) {
    toast.add({ severity: 'warn', summary: 'Warning', detail: 'Please select at least one instance', life: 3000 })
    return
  }
  actionInProgress.value = true
  try {
    await apiService.queueReaderCommand([...deploySelectedInstances.value])
    toast.add({ severity: 'success', summary: 'Success', detail: 'Deploy command sent', life: 3000 })
    showDeployDialog.value = false
    setTimeout(refreshInstances, 5000)
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to deploy', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

function deployAll() {
  if (!instances.value.length) return
  if (!confirm('Deploy JAR to all queue reader instances?')) return
  deployInstancesLocal.value = instances.value.map(i => ({ ...i, selected: true }))
  deployInstanceOptions.value = instances.value.map(i => ({
    label: `${i.configId} - ${i.queueName} (${i.hostName})`,
    value: i.configId
  }))
  deploySelectedInstances.value = instances.value.map(i => i.configId)
  deploySelectAll.value = true
  showDeployDialog.value = true
}

// --- Server Editor ---
function openAddServerDialog() {
  isEditingServer.value = false
  serverForm.value = { serverNumber: '', ipAddress: '', awsInstanceId: '', selectedQueues: [] }
  showServerEditor.value = true
}

function openEditServerDialog(server) {
  isEditingServer.value = true
  currentServer.value = server
  serverForm.value = {
    serverNumber: server.serverNumber,
    ipAddress: server.ipAddress,
    awsInstanceId: server.awsInstanceId || '',
    selectedQueues: server.queues ? server.queues.trim().split(/\s+/).filter(q => q) : []
  }
  showServerEditor.value = true
}

async function saveServer() {
  if (!serverForm.value.serverNumber || !serverForm.value.ipAddress) {
    toast.add({ severity: 'warn', summary: 'Warning', detail: 'Please enter server number and IP address', life: 3000 })
    return
  }

  actionInProgress.value = true
  const serverData = {
    serverNumber: serverForm.value.serverNumber,
    ipAddress: serverForm.value.ipAddress,
    awsInstanceId: serverForm.value.awsInstanceId,
    queues: serverForm.value.selectedQueues.join(' ')
  }

  try {
    if (isEditingServer.value) {
      await apiService.editServer(serverForm.value.serverNumber, serverData)
      toast.add({ severity: 'success', summary: 'Success', detail: 'Server updated', life: 3000 })
    } else {
      await apiService.addServer(serverData)
      toast.add({ severity: 'success', summary: 'Success', detail: 'Server added', life: 3000 })
    }
    showServerEditor.value = false
    setTimeout(refreshServers, 1000)
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to save server', life: 3000 })
  } finally {
    actionInProgress.value = false
  }
}

function confirmDeleteServer(server) {
  deleteConfirmServer.value = server
  showDeleteConfirm.value = true
}

async function executeDeleteServer(callback) {
  try {
    await apiService.deleteServer(deleteConfirmServer.value.serverNumber)
    toast.add({ severity: 'success', summary: 'Success', detail: 'Server deleted', life: 3000 })
    if (selectedServer.value?.serverNumber === deleteConfirmServer.value.serverNumber) {
      selectedServer.value = null
    }
    buildQueueList()
    setTimeout(refreshServers, 1000)
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to delete server', life: 3000 })
  }
  callback?.()
}

// --- JAR Upload ---
function triggerJarUpload(server) {
  jarUploadServer.value = server.serverNumber
  jarFileInput.value?.click()
}

async function uploadJarFile() {
  const serverNum = jarUploadServer.value
  if (!serverNum) return

  const inputEl = jarFileInput.value
  if (!inputEl || !inputEl.files || !inputEl.files[0]) return

  const file = inputEl.files[0]
  if (!file.name.toLowerCase().endsWith('.jar')) {
    toast.add({ severity: 'warn', summary: 'Warning', detail: 'Please select a .jar file', life: 3000 })
    inputEl.value = ''
    return
  }

  actionInProgress.value = true
  try {
    const server = servers.value.find(s => s.serverNumber === serverNum)
    const reader = new FileReader()
    reader.onload = async (e) => {
      try {
        await apiService.uploadJar(serverNum, server?.hostName || '', file.name, e.target.result)
        toast.add({ severity: 'success', summary: 'Success', detail: 'JAR uploaded successfully', life: 3000 })
      } catch (err) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to upload JAR', life: 3000 })
      } finally {
        actionInProgress.value = false
        inputEl.value = ''
        jarUploadServer.value = null
      }
    }
    reader.readAsDataURL(file)
  } catch (err) {
    actionInProgress.value = false
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to upload JAR', life: 3000 })
  }
}

watch(rabbitQueues, () => {
  updateQueueNameOptions()
  getAllQueuesFromInstances()
  buildQueueList()
})

watch(quickestNode, async () => {
  if (quickestNode.value && quickestNode.value.address) {
    await refreshRabbitQueues()
    updateQueueNameOptions()
    getAllQueuesFromInstances()
    buildQueueList()
  }
})

watch(instances, () => {
  getAllQueuesFromInstances()
  buildQueueList()
}, { deep: true })

onMounted(async () => {
  await refreshRabbitNodes()
  await refreshAll()
  if (quickestNode.value && quickestNode.value.address) {
    await refreshRabbitQueues()
    updateQueueNameOptions()
    getAllQueuesFromInstances()
    buildQueueList()
  }
})
</script>

<style scoped>
.d-flex {
  display: flex;
}

.gap-2 {
  gap: 0.5rem;
}

.text-right {
  text-align: right;
}

.text-muted {
  color: var(--text-muted-color) !important;
}

.pull-right {
  float: right;
}

pre {
  background-color: var(--surface-50);
  padding: 0.75rem;
  border-radius: var(--border-radius-md);
  font-size: 0.8rem;
  overflow-x: auto;
}

.binary-select {
  width: 140px;
}
</style>

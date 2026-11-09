<template>
  <div class="app-container">
    <!-- Loading state while Keycloak initializes -->
    <div v-if="loading" class="loading-container">
      <ProgressSpinner style="width: 50px; height: 50px" strokeWidth="8" fill="var(--surface-100)" animationDuration=".5em" />
      <p class="mt-3" style="color: var(--text-muted-color)">Loading...</p>
    </div>

    <!-- Error state if Keycloak fails -->
    <div v-if="error" class="error-container">
      <i class="fa-solid fa-triangle-exclamation" style="font-size: 3rem; color: var(--red-500)"></i>
      <h4 style="color: var(--text-color)">Authentication Error</h4>
      <p style="color: var(--text-color-secondary)">{{ error }}</p>
      <Button @click="retryLogin" label="Retry Login" style="margin-top: 1rem" />
    </div>

    <!-- Main app content (only shown when authenticated) -->
    <template v-if="!loading && !error">
      <!-- Sidebar -->
      <div id="sidebar" :style="{ width: sidebarWidth + 'px' }"
           @mouseover="openMenu" @mouseleave="closeMenu">
        <div class="header">
          <div class="brand">DDS UI</div>
          <div class="title-row">
            <Button class="pin-button" icon="fa-solid fa-thumbtack" text @click="pinned = !pinned" style="width: 32px; height: 32px; margin: 10px 0 0 16px;" />
          </div>
        </div>
        <div class="menu-content">
          <div v-for="(item, index) in menuItems" :key="index" class="menu-item" @click="navigateTo(item.route)" :class="{ 'menu-item-active': $route.path === item.route }">
            <i :class="item.icon"></i>
            <span>{{ item.caption }}</span>
          </div>
        </div>
      </div>

      <!-- Top Bar -->
      <div class="top-bar" :style="{ marginLeft: contentOffset + 'px' }">
        <div class="top-bar-title">DDS Administration</div>
        <div class="top-bar-right">
          <Button @click="logout" label="Logout" severity="danger" text rounded style="font-size: 0.875rem" v-if="$keycloak?.authenticated" />
        </div>
      </div>

      <!-- Main Content -->
      <div class="main-content" :style="{ marginLeft: contentOffset + 'px' }">
        <router-view />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import ProgressSpinner from 'primevue/progressspinner'
import menuConfig from './config/menu'
import KeycloakService from '@/services/keycloak'

const router = useRouter()
const loading = ref(!window.__keycloakReady)
const error = ref('')
const pinned = ref(true)
const sidebarWidth = ref(260)
const menuItems = ref([])

const MIN_WIDTH = 60
const MAX_WIDTH = 260
const windowWidth = ref(window.innerWidth)

const contentOffset = computed(() => {
  if (windowWidth.value <= 768) return 0
  return pinned.value ? 270 : 70
})

let resizeHandler: (() => void) | null = null

function getIsMobile() {
  return window.innerWidth <= 768
}

function updateSidebar() {
  if (getIsMobile()) {
    sidebarWidth.value = 0
  } else {
    sidebarWidth.value = pinned.value ? MAX_WIDTH : MIN_WIDTH
  }
}

function openMenu() {
  if (!getIsMobile()) {
    sidebarWidth.value = MAX_WIDTH
  }
}

function closeMenu() {
  if (!pinned.value) {
    sidebarWidth.value = getIsMobile() ? 0 : MIN_WIDTH
  }
}

function getContentOffset() {
  if (getIsMobile()) return 0
  return pinned.value ? 270 : 70
}

function navigateTo(route: string) {
  router.push(route)
}

function logout() {
  KeycloakService.LogOut()
}

function retryLogin() {
  error.value = ''
  window.location.reload()
}

onMounted(() => {
  menuItems.value = menuConfig.filter(item => true)
  updateSidebar()
  
  resizeHandler = () => {
    windowWidth.value = window.innerWidth
    updateSidebar()
  }
  window.addEventListener('resize', resizeHandler)
  
  watch(pinned, () => {
    updateSidebar()
  })
})

onUnmounted(() => {
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
})
</script>

<style>
/* App container */
.app-container {
  font-family: "Inter var", Lato, Helvetica, Arial, sans-serif;
  color: #04466B;
  min-height: 100vh;
}

/* Loading and error states */
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  text-align: center;
}

.error-container h4 {
  margin-top: 1rem;
  color: var(--text-color);
}

.error-container p {
  color: var(--text-muted-color);
}

/* Sidebar */
#sidebar {
  position: fixed;
  top: 0;
  left: 0;
  height: 103vh;
  overflow: hidden;
  background-color: #5a7999;
  overflow-y: auto;
  white-space: nowrap;
  transition: all .15s ease;
  color: white;
  z-index: 1000;
  margin: 0;
}

.header {
  background-color: #04466B;
  padding: 0;
  height: 10vh;
  color: white;
  display: flex;
  flex-direction: column;
}

.brand {
  font-size: 1.25rem;
  font-weight: 700;
  color: white;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.title-row {
  display: flex;
  align-items: center;
}

.menu-content {
  background-color: #5a7999;
}

.menu-item {
  list-style-type: none;
  display: flex;
  align-items: center;
  padding: 1.166vh 0 1.166vh 0;
  margin: 0;
  cursor: pointer;
  transition: background-color 0.15s ease;
  overflow: hidden;
}

.menu-item:hover {
  background-color: #4a6989;
}

.menu-item-active {
  background-color: var(--highlight-background);
  color: var(--highlight-text-color);
  font-weight: 600;
}

.menu-item i {
  margin: 0 20px 0 20px;
  padding: 0;
  font-size: 1.2rem;
  min-width: 20px;
  text-align: center;
  flex-shrink: 0;
}

.menu-item span {
  padding: 0;
  margin: 3px 0 0 0;
  white-space: nowrap;
  text-align: left;
  font-size: 0.95rem;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Top bar */
.top-bar {
  background-color: var(--surface-0);
  padding: 0.75rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: margin-left 0.15s ease;
}

.top-bar-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-color);
  flex: 1;
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* Main content */
.main-content {
  padding: 1.5rem;
  transition: margin-left 0.15s ease;
}

/* Module styling */
.module {
  background-color: var(--surface-0);
  border-radius: var(--border-radius-md);
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.module-heading {
  margin-bottom: 1.25rem;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid var(--primary-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.module-heading h3,
.module-heading h4 {
  margin: 0;
  color: var(--text-color);
  font-weight: 600;
}

/* Scroll boxes */
.scroll-box-50 { height: 50px; overflow-y: auto; overflow-x: hidden; }
.scroll-box-100 { height: 100px; overflow-y: auto; overflow-x: hidden; }
.scroll-box-150 { height: 150px; overflow-y: auto; overflow-x: hidden; }
.scroll-box-300 { height: 300px; overflow-y: auto; overflow-x: hidden; }
.scroll-box-350 { height: 350px; overflow-y: auto; overflow-x: hidden; }
.scroll-box-400 { height: 400px; overflow-y: auto; overflow-x: hidden; }
.scroll-box-500 { height: 500px; overflow-y: auto; overflow-x: hidden; }

/* Selection and hover states */
.selection {
  background: var(--highlight-background) !important;
}

.hover-box:hover {
  background: var(--content-hover-background) !important;
  cursor: pointer;
}

.show-child-on-hover .child-to-show {
  display: none;
}

.show-child-on-hover:hover .child-to-show {
  display: inline;
}

.show-child-on-hover .child-to-hide {
  display: inline;
}

.show-child-on-hover:hover .child-to-hide {
  display: none;
}

/* Utility classes */
.pull-right {
  float: right;
}

.text-center {
  text-align: center;
}

.text-danger {
  color: var(--red-500) !important;
}

.text-muted {
  color: var(--text-muted-color) !important;
}

.mt-3 {
  margin-top: 1rem !important;
}

.mb-3 {
  margin-bottom: 1rem !important;
}

.mr-2 {
  margin-right: 0.5rem !important;
}

/* Table styling */
.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  padding: 0.5rem 0.75rem;
  text-align: left;
  border-bottom: 1px solid var(--surface-border-color);
}

.table thead th {
  background-color: var(--surface-50);
  color: var(--text-color);
  font-weight: 600;
  font-size: 0.875rem;
}

.table tbody tr:hover {
  background-color: var(--content-hover-background);
}

.table-striped tbody tr:nth-child(even) {
  background-color: var(--surface-50);
}

/* Form styling */
.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.375rem;
  color: var(--text-color);
  font-size: 0.875rem;
  font-weight: 500;
}

.form-control {
  width: 100%;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--surface-border-color);
  border-radius: var(--border-radius-md);
  background-color: var(--surface-0);
  color: var(--text-color);
  font-size: 0.95rem;
  transition: border-color 0.2s ease;
}

.form-control:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--highlight-background);
}

.form-control:disabled {
  background-color: var(--surface-100);
  color: var(--text-muted-color);
}

.form-inline {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.form-inline label {
  margin-bottom: 0;
  white-space: nowrap;
}

/* Label badges (for log levels, etc.) */
.label {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  border-radius: var(--border-radius-md);
  font-size: 0.75rem;
  font-weight: 600;
}

.label-danger {
  background-color: var(--red-100);
  color: var(--red-700);
}

.label-warning {
  background-color: var(--yellow-100);
  color: var(--yellow-700);
}

.label-info {
  background-color: var(--blue-100);
  color: var(--blue-700);
}

.label-success {
  background-color: var(--green-100);
  color: var(--green-700);
}

/* Alert boxes */
.alert {
  padding: 0.75rem 1rem;
  border-radius: var(--border-radius-md);
  margin-bottom: 1rem;
}

.alert-info {
  background-color: var(--blue-50);
  color: var(--blue-700);
  border: 1px solid var(--blue-200);
}

.alert-success {
  background-color: var(--green-50);
  color: var(--green-700);
  border: 1px solid var(--green-200);
}

.alert-warning {
  background-color: var(--yellow-50);
  color: var(--yellow-700);
  border: 1px solid var(--yellow-200);
}

.alert-danger {
  background-color: var(--red-50);
  color: var(--red-700);
  border: 1px solid var(--red-200);
}

/* Modal styling (for views that still use custom modals) */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 2000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal {
  background-color: var(--surface-0);
  border-radius: var(--border-radius-md);
  box-shadow: 0 3px 9px rgba(0, 0, 0, 0.3);
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--surface-border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header .close {
  cursor: pointer;
  font-size: 1.5rem;
  background: none;
  border: none;
  padding: 0;
  color: var(--text-muted-color);
}

.modal-title {
  margin: 0;
  color: var(--text-color);
  font-weight: 600;
}

.modal-body {
  padding: 1.25rem;
}

.modal-footer {
  padding: 0.75rem 1.25rem;
  border-top: 1px solid var(--surface-border-color);
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

/* Scrollbar styling */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f5f5f5;
}

::-webkit-scrollbar-thumb {
  background: #c0c0c0;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a0a0a0;
}

/* Override PrimeVue panel header border for module-heading look */
:deep(.p-panel-header) {
  border-bottom: 2px solid var(--primary-color) !important;
  padding-bottom: 0.75rem !important;
  margin-bottom: 1.25rem !important;
}

/* Responsive */
@media (max-width: 768px) {
  #sidebar {
    width: 0 !important;
  }
  
  .main-content,
  .top-bar {
    margin-left: 0 !important;
  }
  
  .form-inline {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .top-bar {
    padding: 0.5rem 1rem;
  }
  
  .main-content {
    padding: 1rem;
  }
}
</style>

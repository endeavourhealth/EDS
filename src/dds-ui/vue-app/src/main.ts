import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import PrimeVue from 'primevue/config'
import Aura from '@primeuix/themes/aura'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Select from 'primevue/select'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dropdown from 'primevue/dropdown'
import MultiSelect from 'primevue/multiselect'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import Tag from 'primevue/tag'
import Toast from 'primevue/toast'
import ToastService from 'primevue/toastservice'
import ConfirmDialog from 'primevue/confirmdialog'
import ConfirmationService from 'primevue/confirmationservice'
import RadioButton from 'primevue/radiobutton'
import Checkbox from 'primevue/checkbox'
import Button from 'primevue/button'
import ProgressSpinner from 'primevue/progressspinner'
import KeycloakService from '@/services/keycloak'
import { api } from '@/services/api'
import './style.css'
import './fontawesome.css'

const app = createApp(App)

app.component('p-tabview', TabView)
app.component('p-tabviewpanel', TabPanel)
app.component('p-tabView', TabView)
app.component('p-tabPanel', TabPanel)
app.component('p-dialog', Dialog)
app.component('p-table', DataTable)
app.component('p-column', Column)
app.component('p-dropdown', Dropdown)
app.component('p-multi-select', MultiSelect)
app.component('p-inputnumber', InputNumber)
app.component('p-inputtext', InputText)
app.component('p-tag', Tag)
app.component('p-toast', Toast)
app.component('p-confirmdialog', ConfirmDialog)
app.component('p-radiobutton', RadioButton)
app.component('p-checkbox', Checkbox)
app.component('p-select', Select)
app.component('p-button', Button)
app.component('p-progressspinner', ProgressSpinner)

app.use(router)
app.use(PrimeVue, {
  theme: {
    preset: Aura,
    options: {
      darkModeSelector: '.p-dark',
    }
  }
})
app.use(ToastService)
app.use(ConfirmationService)

// Initialize Keycloak and mount when authenticated
KeycloakService.CallLogin((authenticated) => {
  const keycloakInstance = KeycloakService.getInstance()
  
  // Set up request interceptor to attach token
  api.interceptors.request.use(config => {
    if (keycloakInstance?.token) config.headers.Authorization = `Bearer ${keycloakInstance.token}`
    return config
  })
  
  // Signal that Keycloak is ready
  window.__keycloakReady = true
  
  // Mount the app only after authentication
  if (!document.getElementById('app')?._vnode) {
    app.mount('#app')
  }
})

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import KeycloakService from '@/services/keycloak'
import api from '@/services/api'

const app = createApp(App)

// Initialize Keycloak authentication (wrapped to avoid top-level await)
async function initApp() {
  const keycloak = new KeycloakService()
  await keycloak.init()
  window.$keycloak = keycloak // Make available globally for components
  app.config.globalProperties.$keycloak = keycloak

  // Setup global properties and plugins
  app.use(createPinia())
  app.use(router)

  // Attach Keycloak token to Axios
  api.init(keycloak)

  app.mount('#app')
}

initApp().catch(err => {
  console.error('Failed to initialize application:', err)
})

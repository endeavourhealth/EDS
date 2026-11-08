import Keycloak from 'keycloak-js'

const baseUrl = './'
let keycloakInstance: any = null

// Keycloak readiness flag
window.__keycloakReady = false

interface CallbackOneParam<T1 = void, T2 = void> {
    (param1: T1): T2;
}

const Login = (onAuthenticatedCallback: CallbackOneParam) => {
    keycloakInstance = new Keycloak(`${baseUrl}keycloak.json`)
    
    keycloakInstance.init({ onLoad: 'login-required', checkLoginIframe: false })
        .then((authenticated: boolean) => {
            if (authenticated) {
                console.log('Keycloak authenticated successfully')
                
                // Set global keycloak instance for components to access
                window.$keycloak = {
                    authenticated: true,
                    tokenParsed: keycloakInstance.tokenParsed,
                    LogOut: KeycloakService.LogOut,
                    GetAccessToken: KeycloakService.GetAccessToken,
                    getInstance: () => keycloakInstance,
                }
                
                // Start token refresh cycle
                beginRefreshCycle()
                
                // Mount the Vue app
                onAuthenticatedCallback()
            } else {
                console.warn('User not authenticated - redirecting to login')
            }
        })
        .catch((e: any) => {
            console.error('Keycloak init failed:', e)
            console.log(`keycloak init exception: ${JSON.stringify(e)}`)
            
            // Set error state on window for App.vue to display
            if (window.$keycloak) {
                window.$keycloak.authenticated = false
                window.$keycloak.error = 'Authentication failed. Please try again.'
            }
            
            // Still call callback so app can render error state
            onAuthenticatedCallback()
        })
}

function beginRefreshCycle() {
    console.log('Token refresh cycle started')
    setTimeout(refreshToken, 45000)
}

async function refreshToken() {
    if (!keycloakInstance) return
    
    try {
        const refreshed = await keycloakInstance.updateToken(30)
        if (refreshed) {
            console.log('Token refreshed successfully')
        } else {
            console.log('Token still valid')
        }
        
        // Update global instance with new token
        window.$keycloak.tokenParsed = keycloakInstance.tokenParsed
        
        beginRefreshCycle()
    } catch (e) {
        console.error('Token refresh failed - logging out')
        sessionStorage.clear()
        keycloakInstance.logout()
    }
}

const KeycloakService = {
    CallLogin: Login,
    GetAccessToken: () => keycloakInstance?.token,
    LogOut: () => {
        sessionStorage.clear()
        if (keycloakInstance) {
            keycloakInstance.logout()
        }
    },
    GetUserID: () => keycloakInstance?.tokenParsed?.sub,
    getInstance: () => keycloakInstance,
}

export default KeycloakService

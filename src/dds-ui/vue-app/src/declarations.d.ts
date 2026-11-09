interface KeycloakWindow {
  authenticated: boolean;
  tokenParsed?: any;
  LogOut: () => void;
  GetAccessToken: () => string | undefined;
  getInstance: () => any;
  error?: string;
}

declare global {
  interface Window {
    $keycloak: KeycloakWindow;
  }
}

export {}

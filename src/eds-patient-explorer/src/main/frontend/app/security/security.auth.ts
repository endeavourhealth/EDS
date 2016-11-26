import Keycloak = require('keycloak');

import {AuthConfig} from "./models/AuthConfig";
import {WellKnownConfig} from "./models/WellKnownConfig";


export class Auth {

    private static instance:Auth;
    public static factory():Auth {
        if(Auth.instance == null) {
            Auth.instance = new Auth();
        }
        return Auth.instance;
    }

    private authz:any;
    private accountUrl:string;
    private logoutUrl:string;
    private redirectUrl:string;

    private onAuthSuccess:any;
    private onAuthError:any;

    constructor() {
    }

    init() {

        var authConfig:AuthConfig = WellKnownConfig.factory().getAuthConfig();

        this.authz = new Keycloak({
            url: authConfig.authServerUrl,
            realm: authConfig.realm,
            clientId: authConfig.authClientId
        });

        this.accountUrl = authConfig.authServerUrl + '/realms/' + authConfig.realm + '/account';
        this.redirectUrl = window.location.protocol + '//' + window.location.hostname + ':' + window.location.port + '/';
        this.logoutUrl = authConfig.authServerUrl + '/realms/' + authConfig.realm + '/protocol/openid-connect/logout?redirect_uri=' + this.redirectUrl;

        var vm = this;
        this.authz.init({onLoad: 'login-required'})
            .success(function () {
                if(vm.onAuthSuccess != null) {
                    vm.onAuthSuccess();
                }
            }).error(function () {
                console.log('Auth failed to initialise...');
                if(vm.onAuthError != null) {
                    vm.onAuthError();
                }
        });
    }

    getOnAuthSuccess():any {
        return this.onAuthSuccess;
    }

    setOnAuthSuccess(onAuthSuccess:any) {
        this.onAuthSuccess = onAuthSuccess;
    }

    getOnAuthError():any {
        return this.onAuthError;
    }

    setOnAuthError(onAuthError:any) {
        this.onAuthError = onAuthError;
    }

    getAuthz() : any {
        return this.authz;
    }

    getLogoutUrl() : string {
        return this.logoutUrl;
    }

    getAccountUrl() : string {
        return this.accountUrl;
    }

    getRedirectUrl() : string {
        return this.redirectUrl;
    }
}

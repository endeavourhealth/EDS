/// <reference path="../../typings/index.d.ts" />

module app.core {
    'use strict';

	import IPromise = angular.IPromise;
	import LoginResponse = app.models.LoginResponse;
    import User = app.models.User;
    import AuthConfig = app.models.wellknown.AuthConfig;

	export interface ISecurityService {
		getCurrentUser() : app.models.User;
		switchUserInRole(userInRoleUuid:string) : IPromise<app.models.UserInRole>;
		isAuthenticated() : boolean;
        login() : void;
		logout() : void;
        openUserAccountTab() : void;
        onAuthSuccess(callback:any) : void;
        onAuthError(callback:any) : void;
        onAuthLogout(callback:any) : void;
        onAuthReady(callback:any) : void;
	}

	export class SecurityService extends BaseHttpService implements ISecurityService {
		currentUser:app.models.User;

		static $inject = ['$http', '$q', '$rootScope'];

		constructor(protected http:ng.IHttpService, protected promise:ng.IQService, protected $rootScope:IRootScopeService) {
            super(http, promise);
        }

        getAuthz() : any {
            return Auth.factory().getAuthz();
        }

		getCurrentUser() : User {
		    if(!this.currentUser) {
		        this.currentUser = this.parseUser();
            }
			return this.currentUser;
		}

		isAuthenticated():boolean {
			return this.getCurrentUser() != null;
		}

		switchUserInRole(userInRoleUuid:string) : IPromise<app.models.UserInRole> {
			var request = '"' + userInRoleUuid + '"';
			return this.httpPost('/api/security/switchUserInRole', request);
		}

		login() {
		    this.getAuthz().login({ redirectUri : Auth.factory().getRedirectUrl() });
        }

		logout() {
			this.currentUser = null;
            window.location.href = Auth.factory().getLogoutUrl();
		}

        openUserAccountTab() {
            window.open('/api/user/account', '_blank');
        }

        onAuthReady(callback:any) {

            this.getAuthz().onReady = callback;
        }

        onAuthSuccess(callback:any) {
            this.getAuthz().onAuthSuccess = callback;
        }

        onAuthError(callback:any) {
            this.getAuthz().onAuthError = callback;
        }

        onAuthLogout(callback:any) {
            this.getAuthz().onAuthLogout = callback;
        }

        private parseUser() : User {
            if(this.getAuthz().idTokenParsed && this.getAuthz().realmAccess) {
                var user = new User;
                user.forename = this.getAuthz().idTokenParsed.given_name;
                user.surname = this.getAuthz().idTokenParsed.family_name;
                //user.title = this.getAuthz().idTokenParsed.title;              // TODO: custom attribute??
                user.uuid = this.getAuthz().idTokenParsed.sub;
                user.permissions = this.getAuthz().realmAccess.roles;

                user.isSuperUser = false;                                   // TODO: design session needed on RBAC roles / ABAC attributes!
                for(var permission in user.permissions) {
                    if(permission == 'eds_superuser') {
                        user.isSuperUser = true;
                        break;
                    }
                }

                return user;
            }
            return null;
        }
	}

	angular
		.module('app.core')
		.service('SecurityService', SecurityService);
}
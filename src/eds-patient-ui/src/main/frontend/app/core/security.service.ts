/// <reference path="../../typings/index.d.ts" />
/// <reference path="../models/MenuOption.ts" />
/// <reference path="../models/Role.ts" />
/// <reference path="../models/User.ts" />

module app.core {
	import IPromise = angular.IPromise;
	import LoginResponse = app.models.LoginResponse;
	'use strict';

	export interface ISecurityService {
		getCurrentUser() : app.models.User;
		isAuthenticated() : boolean;
		login(username:string, password:string) : IPromise<app.models.User>;
		logout() : void;
	}

	export class SecurityService extends BaseHttpService implements ISecurityService {
		currentUser:app.models.User;

		getCurrentUser() : app.models.User {
			return this.currentUser;
		}

		isAuthenticated():boolean {
			return this.currentUser != null;
		}

		login(username:string, password:string) : IPromise<app.models.User> {
			var vm = this;
			vm.currentUser = null;
			var defer = vm.promise.defer();
			var request = {
				'username': username,
				'password': password
			};
			vm.http.post('/patient/api/security/login', request)
				.then(function (response) {
					vm.currentUser = <app.models.User>response.data;
						/*var loginResponse = <app.models.LoginResponse>response.data;
                        vm.currentUser = loginResponse.user;*/
					defer.resolve(vm.currentUser);
				})
				.catch(function (exception) {
					defer.reject(exception);
				});

			return defer.promise;
		}

		logout() {
			this.currentUser = null;
			var vm = this;
	//		vm.http.post('/patient/api/security/logout');
		}
	}

	angular
		.module('app.core')
		.service('SecurityService', SecurityService);
}
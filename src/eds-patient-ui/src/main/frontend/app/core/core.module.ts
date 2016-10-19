import 'ng-idle';

import {AdminService} from "./admin.service";
import {ModuleStateService} from "./moduleState.service";
import {SecurityService} from "./security.service";
import {AuthInterceptor} from "./security.auth";
import {Config} from "./core.config";

angular.module('app.core', ['ngIdle'])
	.service('AdminService', AdminService)
	.service('ModuleStateService', ModuleStateService)
	.service('AuthInterceptor', AuthInterceptor)
	.service('SecurityService', SecurityService)
	.config(['$httpProvider', function ($httpProvider: ng.IHttpProvider) {
		$httpProvider.interceptors.push('AuthInterceptor');
	}])
	.config(Config);
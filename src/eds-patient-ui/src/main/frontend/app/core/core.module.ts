import 'ng-idle';

import {AdminService} from "./admin.service";
import {AuditService} from "./audit.service";
import {ConsentService} from "./consent.service";
import {MedicalRecordService} from "./medicalRecord.service";
import {ModuleStateService} from "./moduleState.service";
import {SecurityService} from "./security.service";
import {AuthInterceptor} from "./security.auth";
import {Config} from "./core.config";

angular.module('app.core', ['ngIdle'])
	.service('AdminService', AdminService)
	.service('AuditService', AuditService)
	.service('ConsentService', ConsentService)
	.service('MedicalRecordService', MedicalRecordService)
	.service('ModuleStateService', ModuleStateService)
	.service('AuthInterceptor', AuthInterceptor)
	.service('SecurityService', SecurityService)
	.config(['$httpProvider', function ($httpProvider: ng.IHttpProvider) {
		$httpProvider.interceptors.push('AuthInterceptor');
	}])
	.config(Config);
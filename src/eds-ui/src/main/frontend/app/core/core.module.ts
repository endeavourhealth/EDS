import 'angular-toastr';

import {Config} from './core.config';
import {AdminService} from "./admin.service";
import {AuditService} from "./audit.service";
import {FolderService} from "./folder.service";
import {LibraryService} from "./library.service";
import {AuthInterceptor} from "./security.auth";
import {DashboardService} from "./dashboard.service";
import {LoggingService} from "./logging.service";
import {ModuleStateService} from "./moduleState.service";
import {OrganisationSetService} from "./organisationSet.service";
import {PatientIdentityService} from "./patientIdentity.service";
import {RabbitService} from "./rabbit.service";
import {ResourcesService} from "./resources.service";
import {SecurityService} from "./security.service";
import {StatsService} from "./stats.service";
// import {EkbCodingService} from "./ekbCoding.service";
import {TermlexCodingService} from "./termlexCoding.service";
import {TransformErrorsService} from "./transformErrors.service";

angular.module('app.core', ['ngIdle', 'toastr'])
	.service('AdminService', AdminService)
	.service('AuditService', AuditService)
	.service('DashboardService', DashboardService)
	.service('FolderService', FolderService)
	.service('LibraryService', LibraryService)
	.service('LoggingService', LoggingService)
	.service('ModuleStateService', ModuleStateService)
	.service('OrgsanisationSetService', OrganisationSetService)
	.service('PatientIdentityService', PatientIdentityService)
	.service('RabbitService', RabbitService)
	.service('ResourcesService', ResourcesService)
	.service('SecurityService', SecurityService)
	.service('StatsService', StatsService)
	.service('AuthInterceptor', AuthInterceptor)
	// .service('CodingService', EkbCodingService)
	.service('CodingService', TermlexCodingService)
	.service('TransformErrorsService', TransformErrorsService)
	.config(['$httpProvider', function ($httpProvider: ng.IHttpProvider) {
		$httpProvider.interceptors.push('AuthInterceptor');
	}])
	.config(Config);
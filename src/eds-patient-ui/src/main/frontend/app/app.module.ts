/// <reference path="../typings/index.d.ts"/>

//******** ANGULAR 2 START --------

require('zone.js');
import 'reflect-metadata';
import 'angular';

import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import {HttpModule, RequestOptions, XHRBackend, Http} from '@angular/http';
import { Logger } from "angular2-logger/core";
import {ToastModule} from "ng2-toastr";

import {AuthHttp} from "./core/httpInterceptor";

import {AuditComponent} from "./audit/audit.component";
import {ConsentComponent} from "./consent/consent.component";
import {MedicalRecordComponent} from "./medicalRecord/medicalRecord.component";

import {MedicalRecordService} from "./medicalRecord/medicalRecord.service";
import {ConfigService} from "./config/config.service";
import {ConsentService} from "./consent/consent.service";
import {AuditService} from "./audit/audit.service";
import {AdminService} from "./core/admin.service";
import {SecurityService} from "./core/security.service";
import {EdsLoggerService} from "./blocks/logger.service";


@NgModule({
	imports: [
		BrowserModule,
		FormsModule,
		HttpModule,
		ToastModule,
	],
	declarations: [
		AuditComponent,
		ConsentComponent,
		MedicalRecordComponent
	],
	providers: [
		Logger,

		EdsLoggerService,
		MedicalRecordService,
		AuditService,
		ConsentService,
		ConfigService,
		AdminService,
		SecurityService,
		{
			provide: Http,
			useFactory: (backend: XHRBackend, defaultOptions: RequestOptions) => new AuthHttp(backend, defaultOptions),
			deps: [XHRBackend, RequestOptions]
		}
	]
})

export class AppModule {}
//-------- END ANGULAR 2 ********

// Node dependencies
import 'angular-ui-bootstrap';
import 'angular-ui-router';
import 'angular-toastr';
import 'bootstrap';

// Internal dependencies
import '../content/css/index.css';

// Internal module dependencies
import "./appstartup/appstartup.module";
import "./audit/audit.module";
import "./blocks/blocks.module";
import "./config/config.module";
import "./consent/consent.module";
import "./core/core.module";
import "./dialogs/dialogs.module";
import "./layout/layout.module";
import "./medicalRecord/medicalRecord.module";
import "./models/models.module";

// Node module types
import {StateService} from "angular-ui-router";
import IModalService = angular.ui.bootstrap.IModalService;
import IRootScopeService = angular.IRootScopeService;

// Internal module types
import {AppRoute} from "./app.route";

export let app = angular.module('app', [
		'toastr',
		'app.appstartup',
		'app.models',
		'app.core',
		'app.config',
		'app.blocks',
		'app.layout',
		'app.dialogs',

		'app.medicalRecord',
		'app.consent',
		'app.audit'
	])
	.run(['$state', '$rootScope', 'AdminService', 'SecurityService', /*'EdsLoggerService',*/ '$uibModal',
		function ($state:StateService,
							$rootScope:IRootScopeService,
							adminService:AdminService,
							securityService:SecurityService,
							// logger:EdsLoggerService,
							$modal:IModalService) {


			$rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
				if (toState.unsecured !== true && !securityService.isAuthenticated()) {
					var data = {
						isAuth : securityService.isAuthenticated(),
						toState : toState
					};
					// logger.log('You are not logged in', data);
					event.preventDefault();
					//$state.transitionTo('app.register');		// TODO: create registration controller
				}
				if (adminService.getPendingChanges()) {
					event.preventDefault();
					var options = {
						templateUrl:'app/dialogs/messageBox/messageBox.html',
						controller:'MessageBoxController',
						controllerAs:'ctrl',
						backdrop:'static',
						resolve: {
							title : () => 'Unsaved changes',
							message : () => 'There are unsaved changes, do you wish to continue',
							okText : () => 'Yes',
							cancelText : () => 'No'
						}
					};

					$modal.open(options)
						.result
						.then(function() {
							adminService.clearPendingChanges();
							$state.transitionTo(toState);
						});
				}
			});

			// logger.log('Starting app...', securityService.getCurrentUser());
			$state.go('app.medicalRecord', {}, {});
		}]
	)
	.config(AppRoute);

/// <reference path="../typings/index.d.ts"/>

import '../content/css/index.css';
import 'angular';
import 'angular-ui-bootstrap';
import 'bootstrap-css';

import {module} from "angular";
import {IRootScopeService} from 'angular';
import IModalService = angular.ui.bootstrap.IModalService;

import {ILoggerService} from "./blocks/logger.service";
import {IStateService} from "angular-ui-router";
import {IAdminService} from "./core/admin.service";
import {ISecurityService} from "./core/security.service";
import {AppRoute} from "./app.route";

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

export let app = module('app', [
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
	.run(['$state', '$rootScope', 'AdminService', 'SecurityService', 'LoggerService', '$uibModal',
		function ($state:IStateService,
							$rootScope:IRootScopeService,
							adminService:IAdminService,
							securityService:ISecurityService,
							logger:ILoggerService,
							$modal:IModalService) {


			$rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
				if (toState.unsecured !== true && !securityService.isAuthenticated()) {
					var data = {
						isAuth : securityService.isAuthenticated(),
						toState : toState
					};
					logger.log('You are not logged in', data);
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

			logger.log('Starting app...', securityService.getCurrentUser());
			$state.go('app.medicalRecord', {}, {});
		}]
	)
	.config(AppRoute);


/// <reference path="../typings/index.d.ts" />

import ILoggerService = app.blocks.ILoggerService;
import IStateService = angular.ui.IStateService;
import IRootScopeService = angular.IRootScopeService;
import IAdminService = app.core.IAdminService;
import IModalService = angular.ui.bootstrap.IModalService;
import ISecurityService = app.core.ISecurityService;

angular.module('app', [
		'ui.bootstrap',
		'ngIdle',
		'ui.tree',
		'ngDragDrop',
		'angular-uuid-generator',

		'app.core',
		'app.config',
		'app.blocks',
		'app.models',
		'app.layout',
		'app.login',

		'app.dialogs',
		'app.dashboard',
		'app.library',
		'app.protocol',
		'app.system',
		'app.dataSet',
		'app.codeSet',
		'app.organisation',
		'app.service',
		'app.organisationSet',
		'app.admin',
		'app.query',
		'app.routeGroup',
		'flowChart',
		'dragging',
		'mouseCapture'

	])
	.run(['$state', '$rootScope', 'AdminService', 'SecurityService', 'LoggerService', '$uibModal',
		function ($state:IStateService,
							$rootScope:IRootScopeService,
							adminService:IAdminService,
							securityService:ISecurityService,
							logger:ILoggerService,
							$modal : IModalService) {
			$rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
				if (toState.unsecured !== true && !securityService.isAuthenticated()) {
					logger.error('You are not logged in');
					event.preventDefault();
					$state.transitionTo('login');
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
			$state.go('login', {}, {reload: true});
		}]
	);



/// <reference path="../typings/index.d.ts"/>

// Node dependencies
import 'angular';
import 'angular-ui-bootstrap';
import 'angular-ui-router';
import 'bootstrap';
import 'angular-ui-tree';
import 'angular-dragdrop';
import 'angular-toastr';
import 'angular-moment';
import 'moment';

// Internal dependencies
import '../content/css/index.css';
import '../content/less/index.less';

// Internal module dependencies
import "./appstartup/appstartup.module";
import "./blocks/blocks.module";
import "./config/config.module";
import "./core/core.module";
import "./dialogs/dialogs.module";
import "./filters/filters.module";
import "./layout/layout.module";
import "./models/models.module";

import "./mousecapture/mousecapture.module";
import "./dragging/dragging.module";
import "./flowchart/flowchart.module";
import "./dashboard/dashboard.module";
import "./logging/logging.module";
import "./stats/stats.module";
import "./library/library.module";
import "./protocol/protocol.module";
import "./system/system.module";
import "./dataSet/dataSet.module";
import "./codeSet/codeSet.module";
import "./organisations/organisation.module";
import "./services/service.module";
import "./organisationSet/organisationSet.module";
import "./administration/admin.module";
import "./query/query.module";
import "./routeGroup/routeGroup.module";
import "./patientIdentity/patientIdentity.module";
import "./recordViewer/recordViewer.module";
import "./resources/resources.module";
import "./audit/audit.module";

// Node module types
import IModalService = angular.ui.bootstrap.IModalService;
import IStateService = angular.ui.IStateService;
import IRootScopeService = angular.IRootScopeService;

// Internal module types
import {ISecurityService} from "./core/security.service";
import {ILoggerService} from "./blocks/logger.service";
import {IAdminService} from "./core/admin.service";
import {AppRoute} from "./app.route";

angular.module('app', [
		'toastr',
		'angularMoment',
		'ui.bootstrap',
		'ngIdle',
		'ui.tree',
		'ngDragDrop',
		'app.appstartup',
		'app.models',
		'app.core',
		'app.config',
		'app.blocks',
		'app.layout',
		'app.filters',
		'app.dialogs',

		'app.dashboard',
		'app.logging',
		'app.stats',
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
		'app.patientIdentity',
		'app.recordViewer',
		'app.resources',
		'app.audit',
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
						template:require('./dialogs/messageBox/messageBox.html'),
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
            $state.go('app.dashboard', {}, {});
		}]
	)
	.config(AppRoute);




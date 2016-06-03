/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.routeGroup {
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
	import IModalService = angular.ui.bootstrap.IModalService;
	import BaseDialogController = app.dialogs.BaseDialogController;

	'use strict';

	export class RouteGroupEditorController extends BaseDialogController {
		public static open($modal : IModalService, routeGroup : RouteGroup) : IModalServiceInstance {
			var options : IModalSettings = {
				templateUrl:'app/dialogs/routeGroup/editor/routeGroupEditor.html',
				controller:'RouteGroupEditorController',
				controllerAs:'ctrl',
				backdrop: 'static',
				resolve:{
					routeGroup : () => routeGroup
				}
			};

			var dialog = $modal.open(options);
			return dialog;
		}

		static $inject = ['$uibModalInstance', 'LoggerService', 'AdminService', 'routeGroup'];

		constructor(protected $uibModalInstance : IModalServiceInstance,
								private logger:app.blocks.ILoggerService,
								private adminService : IAdminService,
								private routeGroup : RouteGroup) {
			super($uibModalInstance);
			this.resultData = jQuery.extend(true, {}, routeGroup);
		}
	}

	angular
		.module('app.routeGroup')
		.controller('RouteGroupEditorController', RouteGroupEditorController);
}

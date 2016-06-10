/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.service {
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
	import IModalService = angular.ui.bootstrap.IModalService;
	import BaseDialogController = app.dialogs.BaseDialogController;
	import Service = app.models.Service;

	'use strict';

	export class ServiceEditorController extends BaseDialogController {
		public static open($modal : IModalService, service : Service) : IModalServiceInstance {
			var options : IModalSettings = {
				templateUrl:'app/services/editor/serviceEditor.html',
				controller:'ServiceEditorController',
				controllerAs:'ctrl',
				backdrop: 'static',
				resolve:{
					service : () => service
				}
			};

			var dialog = $modal.open(options);
			return dialog;
		}

		static $inject = ['$uibModalInstance', 'LoggerService', 'AdminService', 'service'];

		constructor(protected $uibModalInstance : IModalServiceInstance,
								private logger:app.blocks.ILoggerService,
								private adminService : IAdminService,
								private service : Service) {
			super($uibModalInstance);
			this.resultData = jQuery.extend(true, {}, service);
		}
	}

	angular
		.module('app.service')
		.controller('ServiceEditorController', ServiceEditorController);
}

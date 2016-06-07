/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.organisation {
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
	import IModalService = angular.ui.bootstrap.IModalService;
	import BaseDialogController = app.dialogs.BaseDialogController;

	'use strict';

	export class OrganisationEditorController extends BaseDialogController {
		public static open($modal : IModalService, organisation : Organisation) : IModalServiceInstance {
			var options : IModalSettings = {
				templateUrl:'app/organisation/editor/organisationEditor.html',
				controller:'OrganisationEditorController',
				controllerAs:'ctrl',
				backdrop: 'static',
				resolve:{
					organisation : () => organisation
				}
			};

			var dialog = $modal.open(options);
			return dialog;
		}

		static $inject = ['$uibModalInstance', 'LoggerService', 'AdminService', 'organisation'];

		constructor(protected $uibModalInstance : IModalServiceInstance,
								private logger:app.blocks.ILoggerService,
								private adminService : IAdminService,
								private organisation : Organisation) {
			super($uibModalInstance);
			this.resultData = jQuery.extend(true, {}, organisation);
		}

		addFilter(filter : string) {
			this.resultData.regex += filter;
		}
	}

	angular
		.module('app.organisation')
		.controller('OrganisationEditorController', OrganisationEditorController);
}

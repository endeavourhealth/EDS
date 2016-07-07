/// <reference path="../../../typings/index.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.service {
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
	import IModalService = angular.ui.bootstrap.IModalService;
	import BaseDialogController = app.dialogs.BaseDialogController;
	import Service = app.models.Service;

	'use strict';

	export class ServicePickerController extends BaseDialogController {
		public static open($modal : IModalService, services : Service[]) : IModalServiceInstance {
			var options : IModalSettings = {
				templateUrl:'app/services/picker/servicePicker.html',
				controller:'ServicePickerController',
				controllerAs:'ctrl',
				backdrop: 'static',
				resolve:{
					services : () => services
				}
			};

			var dialog = $modal.open(options);
			return dialog;
		}

		static $inject = ['$uibModalInstance', 'LoggerService', 'AdminService', 'ServiceService', 'services'];
		searchData : string;
		searchResults : Service[];

		constructor(protected $uibModalInstance : IModalServiceInstance,
								private logger:app.blocks.ILoggerService,
								private adminService : IAdminService,
								private serviceService : IServiceService,
								private services : Service[]) {
			super($uibModalInstance);
			this.resultData = jQuery.extend(true, [], services);
		}

		private search() {
			var vm = this;
			vm.serviceService.search(vm.searchData)
				.then(function (result : Service[]) {
					vm.searchResults = result;
				})
				.catch(function (error : any) {

				});
		}

		private addToSelection(match : Service) {
			if ($.grep(this.resultData, function(s:Service) { return s.uuid === match.uuid; }).length === 0)
				this.resultData.push(match);
		}

		private removeFromSelection(match : Service) {
			var index = this.resultData.indexOf(match, 0);
			if (index > -1)
				this.resultData.splice(index, 1);
		}

	}

	angular
		.module('app.service')
		.controller('ServicePickerController', ServicePickerController);
}

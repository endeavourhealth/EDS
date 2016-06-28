/// <reference path="../../../typings/index.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.service {
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
	import IModalService = angular.ui.bootstrap.IModalService;
	import BaseDialogController = app.dialogs.BaseDialogController;
	import Service = app.models.Service;
	import Organisation = app.models.Organisation;
	import OrganisationPickerController = app.organisation.OrganisationPickerController;

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

		static $inject = ['$uibModalInstance', '$uibModal', 'LoggerService', 'AdminService', 'ServiceService', 'service'];

		organisations : Organisation[];

		constructor(protected $uibModalInstance : IModalServiceInstance,
								private $modal : IModalService,
								private log:app.blocks.ILoggerService,
								private adminService : IAdminService,
								private serviceService : IServiceService,
								service : Service) {
			super($uibModalInstance);
			this.resultData = jQuery.extend(true, {}, service);

			if (service.uuid)
				this.getServiceOrganisations(service.uuid);
		}

		private getServiceOrganisations(uuid : string) {
			var vm = this;
			vm.serviceService.getServiceOrganisations(uuid)
				.then(function(result : Organisation[]) {
					vm.organisations = result;
				})
				.catch(function (error : any) {
					vm.log.error('Failed to load organisation services', error, 'Load organisation services');
				});
		}

		private editOrganisations() {
			var vm = this;
			OrganisationPickerController.open(vm.$modal, vm.organisations)
				.result.then(function (result : Organisation[]) {
				vm.organisations = result;
			});
		}

		public ok() {
			// build new list of service orgs
			this.resultData.organisations = {};

			for (var idx in this.organisations) {
				var organisation : Organisation = this.organisations[idx];
				this.resultData.organisations[organisation.uuid] = organisation.name;
			}

			super.ok();
		}
	}

	angular
		.module('app.service')
		.controller('ServiceEditorController', ServiceEditorController);
}

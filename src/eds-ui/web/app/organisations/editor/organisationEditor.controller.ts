/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.organisation {
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
	import IModalService = angular.ui.bootstrap.IModalService;
	import BaseDialogController = app.dialogs.BaseDialogController;
	import Organisation = app.models.Organisation;
	import Service = app.models.Service;
	import ServicePickerController = app.service.ServicePickerController;

	'use strict';

	export class OrganisationEditorController extends BaseDialogController {
		public static open($modal : IModalService, organisation : Organisation) : IModalServiceInstance {
			var options : IModalSettings = {
				templateUrl:'app/organisations/editor/organisationEditor.html',
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

		static $inject = ['$uibModalInstance', '$uibModal', 'LoggerService', 'AdminService', 'OrganisationService', 'organisation'];

		services : Service[];

		constructor(protected $uibModalInstance : IModalServiceInstance,
								private $modal : IModalService,
								private log:app.blocks.ILoggerService,
								private adminService : IAdminService,
								private organisationService : IOrganisationService,
								private organisation : Organisation) {
			super($uibModalInstance);
			this.resultData = jQuery.extend(true, {}, organisation);

			if (organisation.uuid)
				this.getOrganisationServices(organisation.uuid);
		}

		private getOrganisationServices(uuid : string) {
			var vm = this;
			vm.organisationService.getOrganisationServices(uuid)
				.then(function(result : Service[]) {
					vm.services = result;
				})
				.catch(function (error : any) {
					vm.log.error('Failed to load organisation services', error, 'Load organisation services');
				});
		}

		private editServices() {
			var vm = this;
			ServicePickerController.open(vm.$modal, vm.services)
				.result.then(function (result : Service[]) {
					vm.services = result;
				// TODO : SAVE LINKS TO DB
			});
		}


		public ok() {
			// build new list of service orgs
			this.resultData.services = {};

			for (var idx in this.services) {
				var service : Service = this.services[idx];
				this.resultData.services[service.uuid] = service.name;
			}

			super.ok();
		}
	}

	angular
		.module('app.organisation')
		.controller('OrganisationEditorController', OrganisationEditorController);
}

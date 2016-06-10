/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../core/library.service.ts" />

module app.organisation {
	import IOrganisationService = app.organisation.IOrganisationService;
	import MessageBoxController = app.dialogs.MessageBoxController;
	import Organisation = app.models.Organisation;
	'use strict';

	export class OrganisationListController {
		organisations : Organisation[];

		static $inject = ['$uibModal', 'OrganisationService', 'LoggerService'];

		constructor(private $modal : IModalService,
								private organisationService : IOrganisationService,
								private log : ILoggerService) {
			this.getOrganisations();
		}

		getOrganisations() {
			var vm = this;
			vm.organisationService.getOrganisations()
				.then(function(result) {
					vm.organisations = result;
				})
				.catch(function (error) {
					vm.log.error('Failed to load organisations', error, 'Load organisations');
				});
		}

		edit(item : Organisation) {
			var vm = this;
			OrganisationEditorController.open(vm.$modal, item)
				.result.then(function(result : Organisation) {
				jQuery.extend(true, item, result);
				vm.organisationService.saveOrganisation(item)
					.then(function() {
						vm.log.success('Organisation saved', item, 'Save organisation');
					})
					.catch(function (error : any) {
						vm.log.error('Failed to save organisation', error, 'Save organisation');
					});
			});
		}

		delete(item : Organisation) {
			var vm = this;
			MessageBoxController.open(vm.$modal,
																'Delete Organisation', 'Are you sure you want to delete the Organisation?', 'Yes', 'No')
				.result.then(function() {
					// remove item from list
					vm.organisationService.deleteOrganisation(item.uuid)
						.then(function() {
							vm.log.success('Organisation deleted', item, 'Delete Organisation');
						})
						.catch(function(error : any) {
							vm.log.error('Failed to delete Organisation', error, 'Delete Organisation');
						});
			});
		}
	}

	angular
		.module('app.organisation')
		.controller('OrganisationListController', OrganisationListController);
}

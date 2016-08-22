/// <reference path="../../../typings/index.d.ts" />
/// <reference path="../../core/library.service.ts" />

module app.organisation {
	import IOrganisationService = app.organisation.IOrganisationService;
	import MessageBoxController = app.dialogs.MessageBoxController;
	import Organisation = app.models.Organisation;
	'use strict';

	export class OrganisationListController {
		organisations : Organisation[];

		static $inject = ['$uibModal', 'OrganisationService', 'LoggerService','$state'];

		constructor(private $modal : IModalService,
								private organisationService : IOrganisationService,
								private log : ILoggerService,
								protected $state : IStateService) {
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

		add() {
			this.$state.go('app.organisationAction', {itemUuid: null, itemAction: 'add'});
		}

		edit(item : Organisation) {
			this.$state.go('app.organisationAction', {itemUuid: item.uuid, itemAction: 'edit'});
		}

		save(original : Organisation, edited : Organisation) {
			var vm = this;
			vm.organisationService.saveOrganisation(edited)
				.then(function(saved : Organisation) {
					if (original.uuid)
						jQuery.extend(true, original, saved);
					else
						vm.organisations.push(saved);

					vm.log.success('Organisation saved', original, 'Save organisation');
				})
				.catch(function (error : any) {
					vm.log.error('Failed to save organisation', error, 'Save organisation');
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
							var index = vm.organisations.indexOf(item);
							vm.organisations.splice(index, 1);
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

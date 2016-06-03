/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />

module app.organisationSet {
	import OrganisationPickerController = app.dialogs.OrganisationPickerController;
	import IOrganisationService = app.core.IOrganisationService;
	import OrganisationSet = app.models.OrganisationSet;
	import OrganisationSetMember = app.models.OrganisationSetMember;
	import MessageBoxController = app.dialogs.MessageBoxController;
	'use strict';

	export class OrganisationSetController {
		organisationSets : OrganisationSet[];
		selectedOrganisationSet : OrganisationSet;

		static $inject = ['$uibModal', 'OrganisationService', 'LoggerService'];

		constructor(private $modal : IModalService,
								private organisationService : IOrganisationService,
								private log : ILoggerService) {
			this.getRootFolders();
		}

		getRootFolders() {
			var vm = this;
			vm.organisationService.getOrganisationSets()
				.then(function(result) {
					vm.organisationSets = result;
				})
				.catch(function (error) {
					vm.log.error('Failed to load sets', error, 'Load sets');
				});
		}

		selectOrganisationSet(item : OrganisationSet) {
			var vm = this;

			vm.selectedOrganisationSet = item;

			// Load members if necessary
			if (!item.organisations || item.organisations.length === 0) {
				vm.organisationService.getOrganisationSetMembers(item.uuid)
					.then(function (result:OrganisationSetMember[]) {
						vm.selectedOrganisationSet.organisations = result;
					});
			}
		}

		showOrganisationPicker() {
			var vm = this;
			OrganisationPickerController.open(vm.$modal, null, vm.selectedOrganisationSet)
				.result.then(function(organisationSet : OrganisationSet) {
				vm.organisationService.saveOrganisationSet(organisationSet)
					.then(function(result : OrganisationSet) {
						vm.log.success('Organisation set saved', organisationSet, 'Save set');
						vm.selectedOrganisationSet.organisations = organisationSet.organisations;
					})
					.catch(function (error) {
						vm.log.error('Failed to save set', error, 'Save set');
					});
			});
		}

		deleteSet(item : OrganisationSet) {
			var vm = this;
			MessageBoxController.open(vm.$modal,
																'Delete Organisation Set', 'Are you sure you want to delete the set?', 'Yes', 'No')
				.result.then(function() {
					vm.organisationService.deleteOrganisationSet(item)
						.then(function() {
							var i = vm.organisationSets.indexOf(item);
							vm.organisationSets.splice(i, 1);
							vm.log.success('Organisation set deleted', item, 'Delete set');
						})
						.catch(function(error) {
							vm.log.error('Failed to delete set', error, 'Delete set');
						});
			});
		}
	}

	angular
		.module('app.organisationSet')
		.controller('OrganisationSetController', OrganisationSetController);
}

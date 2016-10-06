import IModalService = angular.ui.bootstrap.IModalService;
import IStateService = angular.ui.IStateService;

import {Service} from "../../models/Service";
import {IServiceService} from "../service/service.service";
import {ILoggerService} from "../../blocks/logger.service";
import {MessageBoxController} from "../../dialogs/messageBox/messageBox.controller";

export class ServiceListController {
	services : Service[];

	static $inject = ['$uibModal', 'ServiceService', 'LoggerService','$state'];

	constructor(private $modal : IModalService,
							private serviceService : IServiceService,
							private log : ILoggerService,
							protected $state : IStateService) {
		this.getAll();
	}

	getAll() {
		var vm = this;
		vm.serviceService.getAll()
			.then(function(result) {
				vm.services = result;
			})
			.catch(function (error) {
				vm.log.error('Failed to load services', error, 'Load services');
			});
	}

	add() {
		this.$state.go('app.serviceAction', {itemUuid: null, itemAction: 'add'});
	}

	edit(item : Service) {
		this.$state.go('app.serviceAction', {itemUuid: item.uuid, itemAction: 'edit'});
	}

	save(original : Service, edited : Service) {
		var vm = this;
		vm.serviceService.save(edited)
			.then(function(saved : Service) {
				if (original.uuid)
					jQuery.extend(true, original, saved);
				else
					vm.services.push(saved);

				vm.log.success('Service saved', original, 'Save service');
			})
			.catch(function (error : any) {
				vm.log.error('Failed to save service', error, 'Save service');
			});
	}

	delete(item : Service) {
		var vm = this;
		MessageBoxController.open(vm.$modal,
															'Delete Service', 'Are you sure you want to delete the Service?', 'Yes', 'No')
			.result.then(function() {
				// remove item from list
				vm.serviceService.delete(item.uuid)
					.then(function() {
						var index = vm.services.indexOf(item);
						vm.services.splice(index, 1);
						vm.log.success('Service deleted', item, 'Delete Service');
					})
					.catch(function(error : any) {
						vm.log.error('Failed to delete Service', error, 'Delete Service');
					});
		});
	}
}

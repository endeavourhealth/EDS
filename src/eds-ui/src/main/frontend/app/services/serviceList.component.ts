import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService} from "ui-router-ng2";

import {Service} from "../models/Service";
import {ServiceService} from "./service.service";
import {LoggerService} from "../common/logger.service";
import {MessageBoxDialog} from "../dialogs/messageBox/messageBox.dialog";

@Component({
	template: require('./serviceList.html')
})
export class ServiceListComponent {
	services : Service[];

	static $inject = ['$uibModal', 'ServiceService', 'LoggerService','$state'];

	constructor(private $modal : NgbModal,
							private serviceService : ServiceService,
							private log : LoggerService,
							protected $state : StateService) {
		this.getAll();
	}

	getAll() {
		var vm = this;
		vm.serviceService.getAll()
			.subscribe(
				(result) => vm.services = result,
				(error) => vm.log.error('Failed to load services', error, 'Load services')
			)
	}


	add() {
		this.$state.go('app.serviceEdit', {itemUuid: null, itemAction: 'add'});
	}

	edit(item : Service) {
		this.$state.go('app.serviceEdit', {itemUuid: item.uuid, itemAction: 'edit'});
	}

	save(original : Service, edited : Service) {
		var vm = this;
		vm.serviceService.save(edited)
			.subscribe(
				(saved) => {
					if (original.uuid)
						jQuery.extend(true, original, saved);
					else
						vm.services.push(saved);

					vm.log.success('Service saved', original, 'Save service');
				},
				(error) => vm.log.error('Failed to save service', error, 'Save service')
			);
	}

	delete(item : Service) {
		var vm = this;
		MessageBoxDialog.open(vm.$modal, 'Delete Service', 'Are you sure you want to delete the Service?', 'Yes', 'No')
			.result.then(
			() => vm.doDelete(item),
			() => vm.log.info('Delete cancelled')
		);
	}

	doDelete(item : Service) {
		var vm = this;
		vm.serviceService.delete(item.uuid)
			.subscribe(
				() => {
					var index = vm.services.indexOf(item);
					vm.services.splice(index, 1);
					vm.log.success('Service deleted', item, 'Delete Service');
				},
				(error) => vm.log.error('Failed to delete Service', error, 'Delete Service')
			);
	}
}

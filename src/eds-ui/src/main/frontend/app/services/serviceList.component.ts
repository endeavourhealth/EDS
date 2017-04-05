import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService} from "ui-router-ng2";
import {Service} from "./models/Service";
import {ServiceService} from "./service.service";
import {LoggerService, MessageBoxDialog} from "eds-common-js";
import {Observable} from "rxjs";

@Component({
	template: require('./serviceList.html')
})
export class ServiceListComponent {
	services : Service[];
	timerSubscription : any;

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
				(result) => {
					vm.services = result;
					vm.startRefreshTimersIfNecessary();
				},
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

	deleteData(item : Service) {
		var vm = this;
		MessageBoxDialog.open(vm.$modal, 'Delete Data', 'Are you sure you want to delete all data for this Service?', 'Yes', 'No')
			.result.then(
			() => vm.doDeleteData(item),
			() => vm.log.info('Delete data cancelled')
		);
	}

	doDeleteData(item : Service) {
		var vm = this;
		vm.serviceService.deleteData(item.uuid)
			.subscribe(
				() => {
					vm.log.success('Data deletion started', item, 'Delete Data');
					vm.refreshService(item);
				},
				(error) => vm.log.error('Failed to delete data', error, 'Delete Data')
			);
	}

	private refreshService(oldService : Service) {
		var vm = this;
		vm.serviceService.get(oldService.uuid)
			.subscribe(
				(result) => {
					var index = vm.services.indexOf(oldService);
					if (index > -1) {
						vm.services[index] = result;
					}
				},
				(error) => vm.log.error('Failed to refresh service', error, 'Refresh Service')
			)
	}

	private startRefreshTimersIfNecessary() {
		var vm = this;

		//if we already have a timer, unsubscribe from it
		vm.stopTimer();

		//check to see if any service has additional info. If any does, start the timer
		if (vm.anyServiceWithAdditionalInfo()) {
			vm.timerSubscription = Observable.interval(2000).subscribe(x => {
				vm.refreshServiceAdditionalInfo();
			});
		}
	}

	private refreshServiceAdditionalInfo() {
		var vm = this;
		var arrayLength = vm.services.length;
		for (var i = 0; i < arrayLength; i++) {
			var service = vm.services[i];
			if (service.additionalInfo) {
				vm.refreshService(service);
			}
		}

		//if no services have additional info we may as well stop the timer
		if (!vm.anyServiceWithAdditionalInfo()) {
			vm.stopTimer();
		}
	}

	private stopTimer() {
		var vm = this;
		if (vm.timerSubscription) {
			vm.timerSubscription.unsubscribe();
			vm.timerSubscription = null;
		}
	}

	private anyServiceWithAdditionalInfo() : boolean {
		var vm = this;
		var arrayLength = vm.services.length;
		for (var i = 0; i < arrayLength; i++) {
			var service = vm.services[i];
			if (service.additionalInfo) {
				return true;
			}
		}
		return false;
	}

	viewExchanges(selectedService: Service) {
		this.$state.go('app.exchangeAudit', {serviceUuid: selectedService.uuid});
	}
}

import {Component, OnDestroy, OnInit} from '@angular/core';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StateService} from "ui-router-ng2";
import {Service} from "./models/Service";
import {ServiceService} from "./service.service";
import {linq, LoggerService, MessageBoxDialog} from "eds-common-js";
import {Observable} from "rxjs";
import {Subscription} from 'rxjs/Subscription';
import {SystemService} from "../system/system.service";
import {SystemPickerDialog} from "../system/systemPicker.dialog";

@Component({
	template: require('./serviceList.html')
})
export class ServiceListComponent implements OnInit, OnDestroy{

	services : Service[];
	timer: Subscription = null;

	//filtering
	filteredServices: Service[];
	allPublisherConfigNames: string[];

	static $inject = ['$uibModal', 'ServiceService', 'LoggerService','$state'];

	constructor(private $modal : NgbModal,
							private serviceService : ServiceService,
							private log : LoggerService,
							protected $state : StateService) {


	}

	ngOnInit() {
		this.refreshAllServices();
	}

	ngOnDestroy() {
		if (this.timer) {
			this.timer.unsubscribe();
			this.timer = null;
		}
	}

	refreshAllServices() {
		var vm = this;
		vm.serviceService.getAll()
			.subscribe(
				(result) => {
					vm.services = linq(result).OrderBy(s => s.name).ToArray();
					vm.startRefreshTimer();
					vm.applyFiltering();
					vm.findAllPublisherConfigNames();
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

	deleteData(service: Service) {
		var vm = this;
		vm.selectSystemId(service, function(service: Service, systemId: string) {

			MessageBoxDialog.open(vm.$modal, 'Delete Data', 'Are you sure you want to delete all data for this Service?', 'Yes', 'No')
				.result.then(
				() => vm.doDeleteData(service, systemId),
				() => vm.log.info('Delete data cancelled')
			);
		});
	}

	private selectSystemId(service: Service, callback) {
		var vm = this;

		var endpoints = service.endpoints;
		if (endpoints.length == 0) {
			vm.log.error('No systems in this serviec');

		} else if (endpoints.length == 1) {
			console.log('servvice = ' + service.name + ' only one endpoint');
			var endpoint = endpoints[0];
			var systemId = endpoint.systemUuid;
			callback(service, systemId);

		} else {
			SystemPickerDialog.open(vm.$modal, service, callback);
		}
	}


	private doDeleteData(service: Service, systemId: string) {
		var vm = this;
		vm.serviceService.deleteData(service.uuid, systemId)
			.subscribe(
				() => {
					vm.log.success('Data deletion started', service, 'Delete Data');
					vm.refreshService(service);
				},
				(error) => vm.log.error('Failed to delete data', error, 'Delete Data')
			);
	}

	private refreshService(oldService : Service) {
		var vm = this;
		vm.serviceService.get(oldService.uuid)
			.subscribe(
				(result) => {
					//sub into the services list
					var index = vm.services.indexOf(oldService);
					if (index > -1) {
						vm.services[index] = result;
					}

					//sub into the filtered services list too
					index = vm.filteredServices.indexOf(oldService);
					if (index > -1) {
						vm.filteredServices[index] = result;
					}
				},
				(error) => vm.log.error('Failed to refresh service', error, 'Refresh Service')
			)
	}

	private startRefreshTimer() {
		var vm = this;
		this.timer = Observable.interval(2000).subscribe(() => vm.refreshServicesWithAdditionalInfo());
	}

	private refreshServicesWithAdditionalInfo() {
		var vm = this;
		var arrayLength = vm.services.length;
		for (var i = 0; i < arrayLength; i++) {
			var service = vm.services[i];
			if (service.additionalInfo) {
				vm.refreshService(service);
			}
		}
	}

	viewExchanges(service: Service) {

		/*var vm = this;
		vm.selectSystemId(selectedService, vm.viewExchangesForServiceAndSystem);*/

		var vm = this;
		vm.selectSystemId(service, function(service: Service, systemId: string) {
			vm.$state.go('app.exchangeAudit', {serviceId: service.uuid, systemId: systemId});
		});
	}

	applyFiltering() {
		var vm = this;
		vm.filteredServices = vm.serviceService.applyFiltering(vm.services);
	}

	toggleFilters() {
		var vm = this;
		vm.serviceService.toggleFiltering();

		//call the filtered changed method to remove the applied filtering
		vm.applyFiltering();
	}

	private findAllPublisherConfigNames() {
		var vm = this;
		vm.allPublisherConfigNames = [];

		var arrayLength = vm.services.length;
		for (var i = 0; i < arrayLength; i++) {
			var service = vm.services[i];
			var publisherConfigName = service.publisherConfigName;
			if (publisherConfigName) {
				var index = vm.allPublisherConfigNames.indexOf(publisherConfigName);
				if (index == -1) {
					vm.allPublisherConfigNames.push(publisherConfigName);
				}
			}
		}

		vm.allPublisherConfigNames.sort();
	}

	getNotesPrefix(service: Service) : string {

		if (service.notes
			&& service.notes.length > 10) {
			return service.notes.substr(0, 10) + '...';

		} else {
			return service.notes;
		}
	}
}

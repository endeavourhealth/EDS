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
import {SystemStatus} from "./models/SystemStatus";
import {OdsSearchDialog} from "./odsSearch.dialog";

@Component({
	template: require('./serviceList.html')
})
export class ServiceListComponent implements OnInit, OnDestroy{

	services : Service[];
	timer: Subscription = null;

	//filtering
	filteredServices: Service[];
	allPublisherConfigNames: string[];
	allCcgCodes: string[];

	static $inject = ['$uibModal', 'ServiceService', 'LoggerService','$state'];

	constructor(private $modal : NgbModal,
							private serviceService : ServiceService,
							private log : LoggerService,
							protected $state : StateService) {

		var vm = this;
		vm.loadOrganisationTypes(); //not strictly necessary for this component, but will be needed if editing anything
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

	/**
	 * ensures the list of org types is loaded and cached
	 */
	private loadOrganisationTypes() {
		var vm = this;

		//if already done, return
		if (vm.serviceService.organisationTypes) {
			return;
		}

		var vm = this;
		vm.serviceService.getOrganisationTypeList()
			.subscribe(
				(result) => {
					vm.serviceService.organisationTypes = result;
				},
				(error) => {
					vm.log.error('Failed to retrieve organisation type list');
				}
			);
	}


	refreshAllServices() {
		var vm = this;
		vm.serviceService.getAll()
			.subscribe(
				(result) => {
					/*vm.services = linq(result).OrderBy(s => s.name).ToArray();*/
					vm.services = linq(result).OrderBy(s => s.name.toLowerCase()).ToArray();
					//vm.startRefreshTimer();
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


	/*delete(item : Service) {
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
	 }*/


	/*deleteData(service: Service) {
		var vm = this;
		vm.selectSystemId(service, function(service: Service, systemId: string) {

			MessageBoxDialog.open(vm.$modal, 'Delete Data', 'Are you sure you want to delete all data for this Service?', 'Yes', 'No')
				.result.then(
				() => vm.doDeleteData(service, systemId),
				() => vm.log.info('Delete data cancelled')
			);
		});
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
	}*/

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

	/*private startRefreshTimer() {
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

			//clear down the cached desc on the system statuses
			if (service.systemStatuses) {
				for (var j=9; j<service.systemStatuses.length; j++) {
					var systemStatus = service.systemStatuses[j];
					systemStatus.cachedLastDataDateDesc = null;
				}
			}
		}
	}*/

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
		vm.allCcgCodes = [];

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

			var ccgCode = service.ccgCode;
			if (ccgCode) {
				var index = vm.allCcgCodes.indexOf(ccgCode);
				if (index == -1) {
					vm.allCcgCodes.push(ccgCode);
				}
			}
		}

		vm.allPublisherConfigNames.sort();
		vm.allCcgCodes.sort();
	}

	getNotesPrefix(service: Service) : string {

		if (service.notes
			&& service.notes.length > 25) {
			return service.notes.substr(0, 25) + '...';

		} else {
			return service.notes;
		}
	}

	formatLastDataTooltip(service: Service, status: SystemStatus) : string {

		if (status.lastDataDate) {

			var lastDate = new Date();
			lastDate.setTime(status.lastDataDate);

			var lastDataReceived = new Date();
			lastDataReceived.setTime(status.lastDataReceived);

			return 'Last data from ' + this.formatDate(lastDate) + ', ' + 'received on ' + this.formatDate(lastDataReceived);

		} else {
			return 'No data received';
		}
	}

	formatLastData(service: Service, status: SystemStatus) : string {

		//if we've a cached value, return that
		if (status.cachedLastDataDateDesc) {
			return status.cachedLastDataDateDesc;
		}

		var ret = '';

		//only show system name if more than one status
		if (service.systemStatuses.length > 1) {
			ret += status.systemName;
			ret += ': ';
		}

		if (status.lastDataDate) {
			var lastDate = new Date();
			lastDate.setTime(status.lastDataDate);

			var today = new Date();

			ret += ServiceListComponent.getDateDiffDesc(lastDate, today);

		} else {
			ret += 'n/a';
		}

		//cache it in the status so we don't need to work it out again
		status.cachedLastDataDateDesc = ret;

		return ret;

	}

	static getDateDiffDesc(earlier: Date, later: Date): string {

		var diffMs = later.getTime() - earlier.getTime();

		var durSec = 1000;
		var durMin = durSec * 60;
		var durHour = durMin * 60;
		var durDay = durHour * 25;
		var durWeek = durDay * 7;
		var durYear = durDay * 365.25;

		var toks = [];

		if (toks.length < 2) {
			var years = Math.floor(diffMs / durYear);
			if (years > 0) {
				toks.push('' + years + 'y');
				diffMs -= years * durYear;
			}
		}

		if (toks.length < 2) {
			var weeks = Math.floor(diffMs / durWeek);
			if (weeks > 0) {
				toks.push('' + weeks + 'w');
				diffMs -= weeks * durWeek;
			}
		}

		if (toks.length < 2) {
			var days = Math.floor(diffMs / durDay);
			if (days > 0) {
				toks.push('' + days + 'd');
				diffMs -= days * durDay;
			}
		}

		if (toks.length < 2) {
			var hours = Math.floor(diffMs / durHour);
			if (hours > 0) {
				toks.push('' + hours + 'h');
				diffMs -= hours * durHour;
			}
		}

		if (toks.length < 2) {
			var mins = Math.floor(diffMs / durMin);
			if (mins > 0 ) {
				toks.push('' + mins + 'm');
				diffMs -= mins * durMin;
			}
		}

		if (toks.length < 2) {
			var secs = Math.floor(diffMs / durSec);
			if (secs > 0 ) {
				toks.push('' + secs + 's');
				diffMs -= secs * durSec;
			}
		}

		if (toks.length < 2) {
			if (diffMs > 0) {
				toks.push('' + diffMs + 'ms');
			}
		}

		if (toks.length == 0) {
			toks.push('0s');
		}

		return toks.join(' ');
	}

	formatProcessingStatusTooltip(service: Service, status: SystemStatus) : string {

		var ret = '';

		if (status.publisherMode) {
			if (status.publisherMode == 'Publisher_Draft') {
				ret += '(Draft - new data will be rejected by messaging API) ';

			} else if (status.publisherMode == 'Publisher_Auto_Fail') {
				ret += '(Auto-fail - inbound transform will automatically fail) ';

			} else if (status.publisherMode == 'Publisher_Bulk') {
				ret += '(Bulk - exchangs will be routed to bulk queues) ';

			} else if (status.publisherMode == 'Publisher_Normal') {
				//don't add anything

			} else {
				//in case one was missed somehow
				ret += '<<<' + status.publisherMode + '>>> ';
			}
		}

		if (status.lastDateSuccessfullyProcessed) {

			var d = new Date();
			d.setTime(status.lastDateSuccessfullyProcessed);
			ret += 'Last successfully processed on ' + this.formatDate(d);

		} else {
			ret += 'Not successfully processed any data yet';
		}

		return ret;
	}

	formatProcessingStatus(service: Service, status: SystemStatus) : string {

		var ret = '';

		if (status.publisherMode) {
			if (status.publisherMode == 'Publisher_Draft') {
				ret += '(D) ';

			} else if (status.publisherMode == 'Publisher_Auto_Fail') {
				ret += '(AF) ';

			} else if (status.publisherMode == 'Publisher_Bulk') {
				ret += '(B) ';

			} else if (status.publisherMode == 'Publisher_Normal') {
				//don't add anything

			} else {
				//in case one was missed somehow
				ret += '<<<' + status.publisherMode + '>>> ';
			}
		}

		//only show system name if more than one status
		//don't need to show the system since this is in the previous column
		/*if (service.systemStatuses.length > 1) {
			ret += status.systemName;
			ret += ': '
		}*/

		if (status.processingInError) {
			ret += 'ERROR';

		} else if (status.processingUpToDate) {
			ret += 'OK';

		} else if (status.lastDataReceived) {
			ret += 'Behind';

		} else {
			ret += 'No data';
		}

		return ret;
	}

	formatDate(d: Date) : string {

		var year = '' + d.getFullYear();
		var month = '' + (d.getMonth() + 1);
		var day = '' + d.getDate();

		var hour = '' + d.getHours();
		var minute = '' + d.getMinutes();
		var seconds = '' + d.getSeconds();

		if (month.length < 2) {
			month = '0' + month;
		}
		if (day.length < 2) {
			day = '0' + day;
		}
		if (hour.length < 2) {
			hour = '0' + hour;
		}
		if (minute.length < 2) {
			minute = '0' + minute;
		}
		if (seconds.length < 2) {
			seconds = '0' + minute;
		}

		return day + '/' + month + '/' + year + ' ' + hour + ':' + minute + ':' + seconds;
	}

	odsSearch() {
		var vm = this;
		OdsSearchDialog.open(vm.$modal);
	}
}

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
import {DateTimeFormatter} from "../utility/DateTimeFormatter";

@Component({
	template: require('./serviceList.html')
})
export class ServiceListComponent implements OnInit, OnDestroy{

	services : Service[];
	filteredServices: Service[];
	tagStrDisplayLimit: number;
	cachedTagStrs: {};

	static $inject = ['$uibModal', 'ServiceService', 'LoggerService','$state'];

	constructor(private $modal : NgbModal,
							private serviceService : ServiceService,
							private log : LoggerService,
							protected $state : StateService) {


	}

	ngOnInit() {
		var vm = this;
		vm.tagStrDisplayLimit = 35;
		vm.cachedTagStrs = {}
		vm.refreshAllServices();
		vm.serviceService.getTagNamesFromCache(); //just call this to pre-cache the tag names
	}

	ngOnDestroy() {

	}



	refreshAllServices() {
		var vm = this;
		vm.serviceService.getAll()
			.subscribe(
				(result) => {
					vm.services = result;
					vm.applyFiltering();
				},
				(error) => vm.log.error('Failed to load services', error, 'Load services')
			)
	}


	add() {
		this.$state.go('app.serviceEdit', {itemUuid: null, itemAction: 'add'});
	}

	edit(item : Service) {
		var vm = this;
		ServiceListComponent.editService(item.uuid, vm.$state);
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

	viewExchanges(service: Service) {
		var vm = this;
		ServiceListComponent.viewExchanges(service, vm.$state, vm.$modal);
	}

	static viewExchanges(service: Service, $state: StateService, $modal: NgbModal) {

		ServiceListComponent.selectSystemId(service, $modal, function(service: Service, systemId: string) {
			$state.go('app.exchangeAudit', {serviceId: service.uuid, systemId: systemId});
		});
	}

	static editService(serviceUuid: string, $state: StateService) {
		$state.go('app.serviceEdit', {itemUuid: serviceUuid, itemAction: 'edit'});
	}


	private static selectSystemId(service: Service, $modal: NgbModal, callback) {

		var endpoints = service.endpoints;
		if (endpoints.length == 0) {
			//vm.log.error('No systems in this serviec');

		} else if (endpoints.length == 1) {
			//console.log('servvice = ' + service.name + ' only one endpoint');
			var endpoint = endpoints[0];
			var systemId = endpoint.systemUuid;
			callback(service, systemId);

		} else {
			SystemPickerDialog.open($modal, service, callback);
		}
	}

	/*viewExchanges(service: Service) {

		var vm = this;
		vm.selectSystemId(service, function(service: Service, systemId: string) {
			vm.$state.go('app.exchangeAudit', {serviceId: service.uuid, systemId: systemId});
		});
	}

	private selectSystemId(service: Service, callback) {
		var vm = this;

		var endpoints = service.endpoints;
		if (endpoints.length == 0) {
			//vm.log.error('No systems in this serviec');

		} else if (endpoints.length == 1) {
			//console.log('servvice = ' + service.name + ' only one endpoint');
			var endpoint = endpoints[0];
			var systemId = endpoint.systemUuid;
			callback(service, systemId);

		} else {
			SystemPickerDialog.open(vm.$modal, service, callback);
		}
	}*/


	applyFiltering() {
		var vm = this;
		//console.log('applying filtering from ' + vm.services.length);
		vm.filteredServices = vm.serviceService.applyFiltering(vm.services, false);
		//console.log('filtered down to ' + vm.filteredServices.length);
	}

	clearFilters() {
		var vm = this;
		vm.serviceService.clearFilters();

		//call the filtered changed method to remove the applied filtering
		vm.applyFiltering();
	}

	formatLatestCutoff(service: Service, status: SystemStatus) : string {

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

		if (status.lastReceivedExtractCutoff) {
			var lastDate = new Date();
			lastDate.setTime(status.lastReceivedExtractCutoff);

			var today = new Date();

			ret += DateTimeFormatter.getDateDiffDesc(lastDate, today, 2);

		} else {
			ret += 'n/a';
		}

		//cache it in the status so we don't need to work it out again
		status.cachedLastDataDateDesc = ret;

		return ret;

	}


	formatLatestCutoffTooltip(service: Service, status: SystemStatus) : string {

		if (status.lastReceivedExtractCutoff) {

			var lastDate = new Date();
			lastDate.setTime(status.lastReceivedExtractCutoff);

			var lastDataReceived = new Date();
			lastDataReceived.setTime(status.lastReceivedExtract);

			return 'Last data received ' + ServiceListComponent.formatDate(lastDataReceived) + ' for ' + ServiceListComponent.formatDate(lastDate);

		} else {
			return 'No data received';
		}
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

		if (status.lastProcessedExtract) {

			var d = new Date();
			d.setTime(status.lastProcessedExtract);
			ret += 'Last successfully processed on ' + ServiceListComponent.formatDate(d);

			if (status.lastProcessedExtractCutoff) {
				var d2 = new Date();
				d2.setTime(status.lastProcessedExtractCutoff);
				ret += ' for ' + ServiceListComponent.formatDate(d2);
			}

		} else {
			ret += 'Not successfully processed any data yet';
		}

		return ret;
	}

	formatProcessingStatus(service: Service, status: SystemStatus) : string {

		var ret = '';

		//show the publisher mode if not "normal"
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

		} else if (status.lastReceivedExtract) {
			ret += 'Behind';

		} else {
			ret += 'No data';
		}

		//show how far behind we are if error or behind
		if (status.processingInError
			|| !status.processingUpToDate) {

			//note that we're showing the data date of the last successfully processed data not WHEN it was processed
			if (status.lastProcessedExtractCutoff) {

				//cache on status object so we're not constantly calculating it
				if (!status.cachedLastProcessedDiffDesc) {
					var d = new Date();
					d.setTime(status.lastProcessedExtractCutoff);

					var today = new Date();
					status.cachedLastProcessedDiffDesc = DateTimeFormatter.getDateDiffDesc(d, today, 2);
				}

				ret += ' (' + status.cachedLastProcessedDiffDesc + ')';
			}
		}

		return ret;
	}


	odsSearch() {
		var vm = this;
		OdsSearchDialog.open(vm.$modal);
	}



	getTagStrPrefix(service: Service) : string {
		var vm = this;
		var str = vm.getTagStr(service);
		if (str
			&& str.length > vm.tagStrDisplayLimit) {
			return str.substr(0, vm.tagStrDisplayLimit) + '...';

		} else {
			return str;
		}
	}

	getTagStr(service: Service) : string {
		var vm = this;

		if (!vm.cachedTagStrs[service.uuid]) {
			var str = vm.serviceService.createTagStr(service);
			vm.cachedTagStrs[service.uuid] = str;
		}
		return vm.cachedTagStrs[service.uuid]
	}

	/**
	 * saves current list to CSV
	 */
	saveToCsv() {

		var vm = this;

		//create CSV content in a String
		var lines = [];
		var line;

		//line = 'Name,ID,Parent,Publisher Config,Last Data,Status,Tags';
		line = '\"Name\",\"ID\",\"Parent\",\"Latest Data Cutoff\",\"Status\",\"Tags\"';
		lines.push(line);

		for (var i=0; i<vm.filteredServices.length; i++) {
			var service = vm.filteredServices[i];

			var cols = [];

			cols.push(service.name);
			cols.push(service.localIdentifier);
			cols.push(service.ccgCode);
			//cols.push(service.publisherConfigName);
			cols.push(''); //populated below
			cols.push(''); //populated below
			cols.push(vm.getTagStr(service));

			if (service.systemStatuses) {

				for (var j=0; j<service.systemStatuses.length; j++) {
					var status = service.systemStatuses[j];

					var lastDataStr = vm.formatLatestCutoff(service, status);
					var statusStr = vm.formatProcessingStatus(service, status);

					cols[3] = lastDataStr;
					cols[4] = statusStr;

					line = '\"' + cols.join('\",\"') + '\"';
					lines.push(line);
				}

			} else {

				line = '\"' + cols.join('\",\"') + '\"';
				lines.push(line);
			}
		}

		var csvStr = lines.join('\r\n');

		const filename = 'Services.csv';
		const blob = new Blob([csvStr], { type: 'text/plain' });

		let url = window.URL.createObjectURL(blob);
		let a = document.createElement('a');
		document.body.appendChild(a);
		a.setAttribute('style', 'display: none');
		a.href = url;
		a.download = filename;
		a.click();
		window.URL.revokeObjectURL(url);
		a.remove();
	}


	static formatDate(d: Date): string {
		if (!d) {
			return '<missing date>';
		}

		return d.getFullYear() + '-' + ('0'+(d.getMonth()+1)).slice(-2) + '-' + ('0' + d.getDate()).slice(-2)
			+ ' ' + ('0' + d.getHours()).slice(-2) + ':' + ('0' + d.getMinutes()).slice(-2);
	}


	/*static formatDate(d: Date) : string {

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
			seconds = '0' + seconds;
		}

		return day + '/' + month + '/' + year + ' ' + hour + ':' + minute + ':' + seconds;
	}	*/
}

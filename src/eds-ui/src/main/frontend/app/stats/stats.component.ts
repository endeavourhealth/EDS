import {ServicePickerDialog} from "../services/servicePicker.dialog";
import {Service} from "../models/Service";
import {StorageStatistics} from "./StorageStatistics";
import {StatsService} from "./stats.service";
import {LoggerService} from "../common/logger.service";
import {ServiceService} from "../services/service.service";
import {Component} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
	template : require('./stats.html')
})
export class StatsComponent {
	storageStatistics : StorageStatistics[];
	services : Service[];
	hidePatients : boolean;
	hideEvents : boolean;
	hideLoading : boolean;
	filterDateFrom : Date;
	filterDateTo : Date;

	constructor(private $modal : NgbModal,
				protected statsService:StatsService,
				protected log : LoggerService,
				protected serviceService : ServiceService) {

		this.hidePatients = false;
		this.hideEvents = false;
		this.hideLoading = true;
	}

	refresh() {
		var vm = this;
		vm.hideLoading = false;
		this.getStorageStatistics(vm.services);
	}

	showGraphs() {
		setTimeout(function(){
			($('table.highchart') as any).highchartTable();
		}, 10);
	}

	lookupServiceName(serviceId: string) {
		var vm = this;
		for (var i = 0; i < vm.services.length; ++i) {
			if (vm.services[i].uuid==serviceId) {
				return vm.services[i].name;
			}
		}
	}

	loadServices() {
		var vm = this;
		vm.serviceService.getAll()
			.subscribe(
				(result) => vm.services = result,
				(error) => vm.log.error('Failed to load services', error, 'Load services')
			);
	}

	getStorageStatistics(services : Service[]) {
		var vm = this;
		vm.storageStatistics = null;
		vm.statsService.getStorageStatistics(services)
			.subscribe(
				(data) => {
				vm.storageStatistics = data;
				console.log(vm.storageStatistics);
				vm.hideLoading = true;
			});
	}

	togglePatientStats() {
		var vm = this;
		vm.hidePatients = !vm.hidePatients;
	}

	toggleEventStats() {
		var vm = this;
		vm.hideEvents = !vm.hideEvents;
	}

	actionItem(stat : StorageStatistics, action : string) {
		alert(action+" : "+stat.serviceId);
	}

	zeroFill( number : any, width : any ) {
		width -= number.toString().length;
		if ( width > 0 )
		{
			return new Array( width + (/\./.test( number ) ? 2 : 1) ).join( '0' ) + number;
		}
		return number + ""; // always return a string
	}

	filterDateFromChange(value : any) {
		var vm = this;

		if (!value)
			value="";

		var datestring : string = "";

		if (value!="" && value!=null)
			datestring = value.getFullYear()  + "-" + this.zeroFill((value.getMonth()+1),2) + "-" + this.zeroFill(value.getDate(),2);
	}

	filterDateToChange(value : any) {
		var vm = this;

		if (!value)
			value="";

		var datestring : string = "";

		if (value!="" && value!=null)
			datestring = value.getFullYear()  + "-" + this.zeroFill((value.getMonth()+1),2) + "-" + this.zeroFill(value.getDate(),2);

		vm.refresh();
	}

	getResourceStatistics() {
		if (this.storageStatistics && this.storageStatistics.length > 0)
			return this.storageStatistics[0].resourceStatistics;
		else
			return [];
}

	private editServices() {
		var vm = this;
		ServicePickerDialog.open(vm.$modal, vm.services)
			.result.then(function (result : Service[]) {
			vm.services = result;
			vm.refresh();
		});
	}

}

import IModalService = angular.ui.bootstrap.IModalService;
import IStateService = angular.ui.IStateService;

import {ServicePickerController} from "../services/picker/servicePicker.controller";
import {Service} from "../models/Service";
import {StorageStatistics} from "../models/StorageStatistics";
import {IStatsService} from "../core/stats.service";
import {ILoggerService} from "../blocks/logger.service";
import {IServiceService} from "../services/service/service.service";

export class StatsController {
	storageStatistics : StorageStatistics[];
	services : Service[];
	hidePatients : boolean;
	hideEvents : boolean;
	hideLoading : boolean;
	filterDateFrom : Date;
	filterDateTo : Date;

	static $inject = ['$uibModal','StatsService', 'LoggerService', 'ServiceService', '$state'];

	constructor(private $modal : IModalService,
				protected statsService:IStatsService,
				protected logger:ILoggerService,
				protected serviceService : IServiceService,
				protected $state : IStateService) {

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
			.then(function(result) {
				vm.services = result;
			})
			.catch(function (error) {
				vm.logger.error('Failed to load services', error, 'Load services');
			});
	}

	getStorageStatistics(services : Service[]) {
		var vm = this;
		vm.storageStatistics = null;
		vm.statsService.getStorageStatistics(services)
			.then(function (data:StorageStatistics[]) {
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

	private editServices() {
		var vm = this;
		ServicePickerController.open(vm.$modal, vm.services)
			.result.then(function (result : Service[]) {
			vm.services = result;
			vm.refresh();
		});
	}

}

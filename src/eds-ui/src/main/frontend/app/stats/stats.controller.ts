/// <reference path="../../typings/index.d.ts" />
/// <reference path="../core/stats.service.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.stats {
	import IStatsService = app.core.IStatsService;
	import StatsPatient = app.models.StatsPatient;
	import StatsEvent = app.models.StatsEvent;
	import Service = app.models.Service;
	import IServiceService = app.service.IServiceService;

	'use strict';

	export class StatsController {
		statsPatients:StatsPatient[];
		statsEvents:StatsEvent[];
		services : Service[];
		serviceId : string;
		hidePatients : boolean;
		hideEvents : boolean;
		filterDateFrom : Date;
		filterDateTo : Date;

		static $inject = ['StatsService', 'LoggerService', 'ServiceService', '$state'];

		constructor(protected statsService:IStatsService,
					protected logger:ILoggerService,
					protected serviceService : IServiceService,
					protected $state : IStateService) {
			this.loadServices();
			this.refresh();
			this.hidePatients = false;
		}

		refresh() {
			var vm = this;
			var serviceName = $("#service>option:selected").html()
			this.getStatsPatients(vm.serviceId);
			this.getStatsEvents(vm.serviceId);
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

		getStatsPatients(serviceId : string) {
			var vm = this;
			vm.statsPatients = null;
			vm.statsService.getStatsPatients(serviceId)
				.then(function (data:StatsPatient[]) {
					vm.statsPatients = data;
				});
		}

		getStatsEvents(serviceId : string) {
			var vm = this;
			vm.statsEvents = null;
			vm.statsService.getStatsEvents(serviceId)
				.then(function (data:StatsEvent[]) {
					vm.statsEvents = data;
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

		actionItem(event : StatsPatient, action : string) {
			alert(action+" : "+event.organisation);
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

	}

	angular
		.module('app.stats')
		.controller('StatsController', StatsController);
}

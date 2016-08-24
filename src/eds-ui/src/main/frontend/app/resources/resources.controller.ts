/// <reference path="../../typings/index.d.ts" />
/// <reference path="../core/resources.service.ts" />
/// <reference path="../blocks/logger.service.ts" />

module app.resources {
	import IResourcesService = app.core.IResourcesService;
	//import ResourcesEvent = app.models.ResourcesEvent;
	//import Service = app.models.Service;
	//import IServiceService = app.service.IServiceService;

	'use strict';

	export class ResourcesController {
		//resources:[];
		//services : Service[];
		//serviceId : string;

		static $inject = ['ResourcesService', 'LoggerService', '$state'];

		constructor(protected resourcesService:IResourcesService,
					protected logger:ILoggerService,
					protected $state : IStateService) {
			//this.loadServices();
			this.refresh();
		}

		refresh() {
			/*var vm = this;
			var serviceName = $("#service>option:selected").html()
			this.getResourcesEvents(vm.serviceId);*/
		}

		/*loadServices() {
			var vm = this;
			vm.serviceService.getAll()
				.then(function(result) {
					vm.services = result;
				})
				.catch(function (error) {
					vm.logger.error('Failed to load services', error, 'Load services');
				});
		}*/

		/*getResourcesEvents(serviceId : string) {
			var vm = this;
			vm.resourcesEvents = null;
			vm.resourcesService.getResourcesEvents(serviceId)
				.then(function (data:ResourcesEvent[]) {
					vm.resourcesEvents = data;
				});
		}*/

		/*actionItem(event : ResourcesEvent, action : string) {
			alert(action+" : "+event.loggerName);
		}*/
	}

	angular
		.module('app.resources')
		.controller('ResourcesController', ResourcesController);
}

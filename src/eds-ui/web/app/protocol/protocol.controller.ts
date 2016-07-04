/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />

module app.protocol {
	import ILoggerService = app.blocks.ILoggerService;
	import IScope = angular.IScope;
	import ILibraryService = app.core.ILibraryService;
	import IModalService = angular.ui.bootstrap.IModalService;
	import IWindowService = angular.IWindowService;
	import LibraryItem = app.models.LibraryItem;
	import LibraryItemModuleBase = app.library.LibraryItemModuleBase;
	import ServiceContract = app.models.ServiceContract;
	import IServiceService = app.service.IServiceService;
	import Service = app.models.Service;
	import Protocol = app.models.Protocol;
	import System = app.models.System;
	import Cohort = app.models.Cohort;
	import Dataset = app.models.DataSet;
	import TechnicalInterface = app.models.TechnicalInterface;

	'use strict';

	export class ProtocolController extends LibraryItemModuleBase {
		protected protocol : Protocol;
		selectedContract : ServiceContract;
		services : Service[];
		systems : System[];
		cohorts : Cohort[];
		dataSets : Dataset[];
		protocols : LibraryItem[];
		technicalInterfaces : TechnicalInterface[];

		enabled = ["TRUE", "FALSE"];
		consent = ["OPT-IN", "OPT-OUT"];
		type = ["PUBLISHER", "SUBSCRIBER"];

		static $inject = ['LibraryService', 'ServiceService', 'LoggerService',
			'$uibModal', 'AdminService', '$window', '$stateParams'];

		constructor(
			protected libraryService : ILibraryService,
			protected serviceService : IServiceService,
			protected logger : ILoggerService,
			protected $modal : IModalService,
			protected adminService : IAdminService,
			protected $window : IWindowService,
			protected $stateParams : {itemAction : string, itemUuid : string}) {

			super(libraryService, adminService, logger, $window, $stateParams);

			this.loadServices();
			this.loadSystems();
			this.loadCohorts();
			this.loadDatasets();
		}

		create(folderUuid : string) {
			this.protocol = {
				enabled: 'TRUE',
				patientConsent: 'OPT-IN',
				cohort: '0',
				dataSet: '0',
				serviceContract: []
			} as Protocol;

			this.libraryItem = {
				uuid: null,
				name: 'New data protocol',
				description: '',
				folderUuid: folderUuid,
				protocol: this.protocol
			} as LibraryItem;

		}

		addContract() {
			this.selectedContract = {
				type: '',
				service: null,
				system: null,
				technicalInterface: null,
				active: 'TRUE'
			} as ServiceContract;

			this.libraryItem.protocol.serviceContract.push(this.selectedContract);
		}

		removeContract(scope : any) {
			this.libraryItem.protocol.serviceContract.splice(scope.$index, 1);
			if (this.selectedContract === scope.item) {
				this.selectedContract = null;
			}
		}

		setService() {
			var serviceName = $("#service>option:selected").html()
			this.selectedContract.service.name = serviceName;
		}

		setSystem() {
			var systemName = $("#system>option:selected").html()
			this.selectedContract.system.name = systemName;
		}

		setTechnicalInterface() {
			var technicalInterfaceName = $("#technicalInterface>option:selected").html()
			this.selectedContract.technicalInterface.name = technicalInterfaceName;
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

		loadCohorts() {
			var vm = this;
			vm.libraryService.getCohorts()
				.then(function(result) {
					vm.cohorts = result;
				})
				.catch(function (error) {
					vm.logger.error('Failed to load cohorts', error, 'Load cohorts');
				});
		}

		loadDatasets() {
			var vm = this;
			vm.libraryService.getDatasets()
				.then(function(result) {
					vm.dataSets = result;
				})
				.catch(function (error) {
					vm.logger.error('Failed to load dataSets', error, 'Load dataSets');
				});

			/*vm.libraryService.getProtocols("edf5ac83-1491-4631-97ff-5c7a283c73b1")
				.then(function(result) {
					vm.protocols = result;
				})
				.catch(function (error) {
					vm.logger.error('Failed to load protocols', error, 'Load protocols');
				});*/
		}

		loadSystems() {
			var vm = this;
			vm.libraryService.getSystems()
				.then(function(result) {
					vm.systems = result;
					vm.technicalInterfaces = [];
					console.log(vm.systems[0].technicalInterface.length);
					console.log(vm.systems[0].technicalInterface[0].name);

					for (var i = 0; i < vm.systems.length; ++i) {
						for (var j = 0; j < vm.systems[i].technicalInterface.length; ++j) {
							var technicalInterface = {
								uuid: vm.systems[i].technicalInterface[j].uuid,
								name: vm.systems[i].technicalInterface[j].name,
								messageType: vm.systems[i].technicalInterface[j].messageType,
								messageFormat: vm.systems[i].technicalInterface[j].messageFormat,
								messageFormatVersion: vm.systems[i].technicalInterface[j].messageFormatVersion
							} as TechnicalInterface;
							vm.technicalInterfaces.push(technicalInterface);
						}
					}

				})
				.catch(function (error) {
					vm.logger.error('Failed to load systems', error, 'Load systems');
				});
		}

	}

	angular
		.module('app.protocol')
		.controller('ProtocolController', ProtocolController);
}


/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />

module app.system {
	import ILoggerService = app.blocks.ILoggerService;
	import IScope = angular.IScope;
	import ILibraryService = app.core.ILibraryService;
	import IModalService = angular.ui.bootstrap.IModalService;
	import IWindowService = angular.IWindowService;
	import LibraryItem = app.models.LibraryItem;
	import LibraryItemModuleBase = app.library.LibraryItemModuleBase;
	import IServiceService = app.service.IServiceService;
	import Service = app.models.Service;
	import System = app.models.System;
	import TechnicalInterface = app.models.TechnicalInterface;

	'use strict';

	export class SystemController extends LibraryItemModuleBase {
		protected system : System;
		selectedInterface : TechnicalInterface;

		formats = ["EMISOPEN", "OPENHR", "EMISCSV", "TPPCSV", "TPPXML", "FHIRJSON", "FHIRXML", "VITRUCARE", "EDWXML", "TABLEAU"];
		types = ["Patient Record","Demographics","Appointments","Summary","Discharge","Episode"];

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

		}

		create(folderUuid : string) {
			this.system = {
				uuid: null,
				name: 'New system',
				technicalInterface: []
			} as System;

			this.libraryItem = {
				uuid: null,
				name: 'New system',
				description: '',
				folderUuid: folderUuid,
				system: this.system
			} as LibraryItem;

		}

		addInterface() {
			this.selectedInterface = {
				uuid: null,
				name: 'New interface',
				messageType: '',
				messageFormat: '',
				messageFormatVersion: ''
			} as TechnicalInterface;

			this.libraryItem.system.technicalInterface.push(this.selectedInterface);
		}

		removeInterface(scope : any) {
			this.libraryItem.system.technicalInterface.splice(scope.$index, 1);
			if (this.selectedInterface === scope.item) {
				this.selectedInterface = null;
			}
		}

	}

	angular
		.module('app.system')
		.controller('SystemController', SystemController);
}

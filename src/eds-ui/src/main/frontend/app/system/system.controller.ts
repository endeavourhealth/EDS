import IModalService = angular.ui.bootstrap.IModalService;
import IWindowService = angular.IWindowService;

import {LibraryItemModuleBase} from "../common/libraryItemModuleBase";
import {System} from "../models/System";
import {TechnicalInterface} from "../models/TechnicalInterface";
import {ILibraryService} from "../core/library.service";
import {IServiceService} from "../services/service/service.service";
import {ILoggerService} from "../blocks/logger.service";
import {IAdminService} from "../core/admin.service";
import {LibraryItem} from "../models/LibraryItem";

export class SystemController extends LibraryItemModuleBase {
	protected system : System;
	selectedInterface : TechnicalInterface;

	formats = ["EMISOPEN", "OPENHR", "EMISCSV", "TPPCSV", "TPPXML", "FHIRJSON", "FHIRXML", "VITRUCARE", "EDWXML", "TABLEAU"];
	types = ["Patient Record","Demographics","Appointments","Summary","Discharge","Episode"];
	frequencies = ["Transactional","Hourly","Daily"];

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
			frequency: '',
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

import {LibraryItemComponent} from "../edsLibrary/libraryItemComponent";
import {System} from "./models/System";
import {TechnicalInterface} from "./models/TechnicalInterface";
import {ServiceService} from "../services/service.service";
import {AdminService, LibraryService, LoggerService} from "eds-common-js";
import {StateService, Transition} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Component} from "@angular/core";
import {EdsLibraryItem} from "../edsLibrary/models/EdsLibraryItem";

@Component({
	template : require('./system.html')
})
export class SystemEditComponent extends LibraryItemComponent {
	protected system : System;
	selectedInterface : TechnicalInterface;

	//NOTE: these strings correspnd to the values in the MessageFormat class
	formats = ["EMISOPEN", "OPENHR", "EMISCSV", "TPPCSV", "TPPXML", "FHIRJSON", "FHIRXML", "VITRUCARE", "EDWXML", "TABLEAU", "ENTERPRISE_CSV", "HL7V2", "BARTSCSV"];

	types = ["Patient Record","Demographics","Appointments","Summary","Discharge","Episode"];
	frequencies = ["Transactional","Hourly","Daily"];

	constructor(
		protected libraryService : LibraryService,
		protected adminService : AdminService,
		protected log : LoggerService,
		protected transition : Transition,
		protected state : StateService,
		protected serviceService : ServiceService,
		protected $modal : NgbModal) {

		super(libraryService, adminService, log, transition, state);

	}

	create(folderUuid : string) {
		this.system = {
			uuid: null,
			name: '',
			technicalInterface: []
		} as System;

		this.libraryItem = {
			uuid: null,
			name: '',
			description: '',
			folderUuid: folderUuid,
			system: this.system
		} as EdsLibraryItem;

	}

	getTechnicalInterface() {
		if (this.libraryItem && this.libraryItem.system)
			return this.libraryItem.system.technicalInterface;
		else
			return null;
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

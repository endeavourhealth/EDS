import {LibraryItemComponent} from "../library/libraryItemComponent";
import {System} from "./models/System";
import {TechnicalInterface} from "./models/TechnicalInterface";
import {LibraryService} from "../library/library.service";
import {ServiceService} from "../services/service.service";
import {LoggerService} from "../common/logger.service";
import {AdminService} from "../administration/admin.service";
import {LibraryItem} from "../library/models/LibraryItem";
import {StateService, Transition} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Component} from "@angular/core";

@Component({
	template : require('./system.html')
})
export class SystemEditComponent extends LibraryItemComponent {
	protected system : System;
	selectedInterface : TechnicalInterface;

	formats = ["EMISOPEN", "OPENHR", "EMISCSV", "TPPCSV", "TPPXML", "FHIRJSON", "FHIRXML", "VITRUCARE", "EDWXML", "TABLEAU"];
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

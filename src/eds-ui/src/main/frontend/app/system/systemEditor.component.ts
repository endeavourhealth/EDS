import {LibraryItemComponent} from "../edsLibrary/libraryItemComponent";
import {System} from "./models/System";
import {TechnicalInterface} from "./models/TechnicalInterface";
import {ServiceService} from "../services/service.service";
import {AdminService, LibraryService, LoggerService} from "eds-common-js";
import {StateService, Transition} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Component} from "@angular/core";
import {EdsLibraryItem} from "../edsLibrary/models/EdsLibraryItem";
import {SystemService} from "./system.service";

@Component({
	template : require('./system.html')
})
export class SystemEditComponent extends LibraryItemComponent {

	//selectedInterface : TechnicalInterface;

	//NOTE: these strings correspnd to the values in the MessageFormat class
	formats: string[];
	//formats = ["EMISOPEN", "OPENHR", "EMISCSV", "TPPCSV", "TPPXML", "FHIRJSON", "FHIRXML", "VITRUCARE", "EDWXML", "TABLEAU", "ENTERPRISE_CSV", "HL7V2", "BARTSCSV"];

	types = ["Patient Record","Demographics","Appointments","Summary","Discharge","Episode"];
	frequencies = ["Transactional","Hourly","Daily"];

	constructor(
		protected libraryService : LibraryService,
		protected adminService : AdminService,
		protected log : LoggerService,
		protected transition : Transition,
		protected state : StateService,
		protected serviceService : ServiceService,
		private systemService : SystemService,
		protected $modal : NgbModal) {

		super(libraryService, adminService, log, transition, state);

		//load our list of known system formats from the systems already on the DB
		this.loadMessageFormats();
	}

	private loadMessageFormats() {
		console.log('loading formats');

		this.systemService.getSystems().subscribe(
			(result) => {
				//go through the existing systems to work out the distinct list of know message formats to choose from
				this.formats = [];
				for (var i = 0; i < result.length; ++i) {
					var existingSystem = result[i];

					for (var j=0; j<existingSystem.technicalInterface.length; j++) {
						var existingTechnicalInterface = existingSystem.technicalInterface[j];
						var existingMessageFormat = existingTechnicalInterface.messageFormat;

						if (!this.formatsContains(existingMessageFormat)) {
							this.formats.push(existingMessageFormat);
						}
					}
				}

				this.formats.sort();
			},
			(error) => {
				console.log('failed to get systems');
				console.log(error);

				this.log.error('Failed to retrieve system formats', error);
			}
		);
	}

	private formatsContains(newFormat: string) : boolean {
		for (var i=0; i<this.formats.length; i++) {
			var format = this.formats[i];
			if (format == newFormat) {
				return true;
			}
		}

		return false;
	}

	getInterface(): TechnicalInterface {
		var vm = this;

		if (!vm.libraryItem
			|| !vm.libraryItem.system) {
			//if called before the library item is loaded
			return new TechnicalInterface();
		} else {
			var interfaces = vm.libraryItem.system.technicalInterface;
			if (interfaces && interfaces.length > 0) {
				return interfaces[0];
			} else {
				//if no interface yet, create and add to the system
				var newInterface = {
					uuid: null,
					name: 'New interface',
					frequency: '',
					messageType: '',
					messageFormat: '',
					messageFormatVersion: ''
				} as TechnicalInterface;
				vm.libraryItem.system.technicalInterface.push(newInterface);
				return newInterface;
			}
		}
	}

	create(folderUuid : string) {
		var vm = this;

		var newSystem = {
			uuid: null,
			name: '',
			technicalInterface: []
		} as System;

		vm.libraryItem = {
			uuid: null,
			name: '',
			description: '',
			folderUuid: folderUuid,
			system: newSystem
		} as EdsLibraryItem;
	}

	save(close : boolean) {

		//the technical interface also has a name field, which we just populate with the system name
		var vm = this;
		var name = vm.libraryItem.name;
		vm.getInterface().name = name;

		super.save(close);
	}

	/*getTechnicalInterface() {
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

	removeInterface(index: number, scope : any) {
		this.libraryItem.system.technicalInterface.splice(index, 1);
		if (this.selectedInterface === scope.item) {
			this.selectedInterface = null;
		}
	}*/

	newFormat() {
		var vm = this;

		var newFormat = prompt("Enter the new format name");
		if (newFormat == null) {
			return;
		}
		if (newFormat.length == 0) {
			vm.log.error('Format cannot be empty');
			return;
		}

		if (vm.formatsContains(newFormat)) {
			vm.log.error('Format already exists');
			return;
		}

		vm.formats.push(newFormat);

		//set on the interface
		vm.getInterface().messageFormat = newFormat;
		//this.selectedInterface.messageFormat = newFormat;
	}

}

import {Composition} from "./models/Composition";
import {Section} from "./models/Section";
import {Resource} from "./models/Resource";
import {EntityMap} from "./models/EntityMap";
import {AdminService, LibraryService, LoggerService} from "eds-common-js";
import {Test} from "../tests/models/Test";
import {TestEditDialog} from "../tests/testEditor.dialog";
import {Component, ViewChild} from "@angular/core";
import {Transition, StateService} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DataSetService} from "./dataSet.service";
import {EdsLibraryItem} from "../edsLibrary/models/EdsLibraryItem";
import {DataSet} from "./models/Dataset";
import {Dpa} from "../dpa/models/Dpa";
import {DpaPickerDialog} from "../dpa/dpaPicker.dialog";

@Component({
	template : require('./dataSetEditor.html')
})
export class DataSetEditComponent {
	libraryItem : EdsLibraryItem;
	selectedComposition: Composition;
	selectedSection: Section;
	selectedResource: Resource;
	entityMap: EntityMap;

	dataset : DataSet = <DataSet>{};
	dpas : Dpa[];

	@ViewChild('datasetname') dataSetNameInput;

	sections = ["GP practice",
		"Referral details",
		"Patient demographics",
		"Special requirements",
		"Participation in research",
		"Admission details",
		"Handover details",
		"Outpatient details",
		"Discharge details",
		"Relevant clinical risk factors",
		"Reason for contact",
		"Presenting complaints or issues",
		"History",
		"Medication and medical devices",
		"Allergies and adverse reaction",
		"Safety alerts",
		"Legal information",
		"Social context",
		"Family history",
		"Review of systems",
		"Patient and carer concerns",
		"Examination findings",
		"Assessment scales",
		"Problems and issues",
		"Diagnoses",
		"Procedures",
		"Clinical summary",
		"Investigations and results",
		"Plan and requested actions",
		"Outstanding issues",
		"Information given",
		"Person completing record",
		"Person handing over record",
		"Person receiving handover",
		"Distribution list"];

	constructor(protected log: LoggerService,
				protected libraryService: LibraryService,
				protected dataSetService : DataSetService,
				protected $modal: NgbModal,
				protected adminService: AdminService,
				protected $state: StateService,
				protected transition: Transition) {

		this.performAction(transition.params()['itemAction'], transition.params()['itemUuid']);
		//this.loadEntityMap();
	}

	protected performAction(action: string, itemUuid: string) {
		switch (action) {
			case 'add':
				this.create();
				break;
			case 'edit':
				this.load(itemUuid);
				break;
		}
	}

	ngAfterViewInit() {
			this.dataSetNameInput.nativeElement.focus();
	}

	// loadEntityMap() {
	// 	var vm = this;
	// 	vm.dataSetService.getEntityMap()
	// 		.subscribe(
	// 			(result) => vm.entityMap = result,
	// 			(error) => vm.log.error('Error loading entity map', error, 'Error')
	// 		);
	// }

	setCompositionHeading(heading: any) {
		this.selectedComposition.heading = heading;
	}

	setSectionHeading(heading: any) {
		this.selectedSection.heading = heading;
	}

	selectResource(resource: Resource, index: any) {
		var vm = this;
		var test: Test = null;

		if (resource.heading != "") {
			test = {resource} as Test;
		}

		TestEditDialog.open(this.$modal, test, true)
			.result.then(function (test: Test) {
			vm.selectedResource.heading = test.resource.heading;
			vm.selectedResource = test.resource;
			vm.selectedSection.resource[index] = test.resource;
			vm.adminService.setPendingChanges();
		});
	}

	addComposition() {
		var newComposition = {
			heading: '',
			section: []
		} as Composition;
		this.libraryItem.dataSet.composition.push(newComposition);
		this.selectedComposition = newComposition;
	}

	removeComposition(item: any) {
		var index = this.libraryItem.dataSet.composition.findIndex((e) => { return e === item; });
		this.libraryItem.dataSet.composition.splice(index, 1);
		if (this.selectedComposition === item) {
			this.selectedComposition = null;
		}
	}

	addSection() {
		this.selectedSection = {
			heading: '',
			resource: []
		} as Section;
		this.selectedComposition.section.push(this.selectedSection);
	}

	removeSection(item: any) {
		var index = this.selectedComposition.section.findIndex((e) => { return e === item; });
		this.selectedComposition.section.splice(index, 1);
		if (this.selectedSection === item) {
			this.selectedSection = null;
		}
	}

	addResource() {
		this.selectedResource = {
			heading: '',
			filter: []
		} as Resource;
		this.selectedSection.resource.push(this.selectedResource);
	}

	removeResource(item: any) {
		var index = this.selectedSection.resource.findIndex((e) => { return e === item; });
		this.selectedSection.resource.splice(index, 1);
		if (this.selectedResource === item) {
			this.selectedResource = null;
		}
	}

	getCompositionSections() {
		if (this.selectedComposition)
			return this.selectedComposition.section;
		return null;
	}

	getSectionResources() {
		if (this.selectedSection)
			return this.selectedSection.resource;
		return null;
	}

	create() {
		this.dataset = {
			uuid: null,
			name: '',
			description: '',
			attributes: '',
			queryDefinition: ''
		} as DataSet;
	}

	// create(folderUuid: string) {
	// 	this.libraryItem = {
	// 		uuid: null,
	// 		name: '',
	// 		description: '',
	// 		folderUuid: folderUuid,
	// 		dataSet: {
	// 			composition: []
	// 		}
	// 	} as EdsLibraryItem;
	// }

	// load(uuid : string) {
	// 	var vm = this;
	// 	this.create(null);
	// 	vm.libraryService.getLibraryItem<EdsLibraryItem>(uuid)
	// 		.subscribe(
	// 			(libraryItem) => vm.libraryItem = libraryItem,
	// 			(data) => vm.logger.error('Error loading', data, 'Error')
	// 		);
	// }

	load(uuid : string) {
		var vm = this;
		vm.dataSetService.getDataSet(uuid)
            .subscribe(result =>  {
					vm.dataset = result;
					vm.getLinkedDpas();
				},
				error => vm.log.error('Error loading Data Set', error, 'Error')
			);
	}

	// save(close : boolean) {
	// 	var vm = this;
	// 	vm.libraryService.saveLibraryItem(vm.libraryItem)
	// 		.subscribe(
	// 			(libraryItem) => {
	// 				vm.libraryItem.uuid = libraryItem.uuid;
	// 				vm.adminService.clearPendingChanges();
	// 				vm.log.success('Item saved', vm.libraryItem, 'Saved');
	// 				if (close) {
	// 					vm.state.go(vm.transition.from());
	// 				}
	// 			},
	// 			(error) => vm.log.error('Error saving', error, 'Error')
	// 		);
	// }

	save(close : boolean) {

		if (!this.validateInput())
			return;

		var vm = this;

		// Populate Data Processing Agreements before save
		vm.dataset.dpas= {};
		for (var idx in vm.dpas) {
			var dpa : Dpa = vm.dpas[idx];
			this.dataset.dpas[dpa.uuid] = dpa.name;
		}

		vm.dataSetService.saveDataSet(vm.dataset)
            .subscribe(
				(response) => {
					vm.adminService.clearPendingChanges();
					vm.log.success('Data Set saved', vm.dataset, 'Saved');
					if (close) { vm.close(); }
				},
				(error) => vm.log.error('Error saving Data Set', error, 'Error')
			);
	}

	close() {
		this.adminService.clearPendingChanges();
		window.history.back();
	}

	private getLinkedDpas() {
		var vm = this;
		vm.dataSetService.getLinkedDpas(vm.dataset.uuid)
            .subscribe(
				result => vm.dpas = result,
				error => vm.log.error('Failed to load linked Data Processing Agreement', error, 'Load Linked Data Processing Agreement')
			);
	}

	private editDataProcessingAgreements() {
		var vm = this;
		DpaPickerDialog.open(vm.$modal, vm.dpas)
            .result.then(function
					(result : Dpa[]) { vm.dpas = result;},
					() => vm.log.info('Edit Data Processing Agreements cancelled')
		);
	}

	private editDataProcessingAgreement(item : Dpa) {
		this.$state.go('app.dpaEditor', {itemUuid: item.uuid, itemAction: 'edit'});
	}

	private validateInput(){
		var vm = this;
		var result = true;

		//name is mandatory
		if (vm.dataset.name.trim() == '') {
			vm.log.warning('Data Set must not be blank');
			vm.dataSetNameInput.nativeElement.focus();
			result = false;
		};

		return result;
	}
}
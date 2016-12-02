import {Composition} from "./models/Composition";
import {Section} from "./models/Section";
import {Resource} from "./models/Resource";
import {EntityMap} from "./models/EntityMap";
import {LibraryService} from "../library/library.service";
import {LoggerService} from "../common/logger.service";
import {AdminService} from "../administration/admin.service";
import {Test} from "../tests/models/Test";
import {TestEditDialog} from "../tests/testEditor.dialog";
import {Component} from "@angular/core";
import {Transition, StateService} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {LibraryItem} from "../library/models/LibraryItem";
import {DataSetService} from "./dataSet.service";

@Component({
	template : require('./dataSetEditor.html')
})
export class DataSetEditComponent {
	libraryItem : LibraryItem;
	selectedComposition: Composition;
	selectedSection: Section;
	selectedResource: Resource;
	entityMap: EntityMap;

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

	constructor(protected libraryService: LibraryService,
							protected dataSetService : DataSetService,
							protected logger: LoggerService,
							protected $modal: NgbModal,
							protected adminService: AdminService,
							protected state: StateService,
							protected transition: Transition) {

		this.performAction(transition.params()['itemAction'], transition.params()['itemUuid']);
		this.loadEntityMap();
	}

	protected performAction(action: string, itemUuid: string) {
		switch (action) {
			case 'add':
				this.create(itemUuid);
				break;
			case 'edit':
				this.load(itemUuid);
				break;
		}
	}

	loadEntityMap() {
		var vm = this;
		vm.dataSetService.getEntityMap()
			.subscribe(
				(result) => vm.entityMap = result,
				(error) => vm.logger.error('Error loading entity map', error, 'Error')
			);
	}

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

	create(folderUuid: string) {
		this.libraryItem = {
			uuid: null,
			name: 'New item',
			description: '',
			folderUuid: folderUuid,
			dataSet: {
				composition: []
			}
		} as LibraryItem;
	}


	load(uuid : string) {
		var vm = this;
		this.create(null);
		vm.libraryService.getLibraryItem(uuid)
			.subscribe(
				(libraryItem) => vm.libraryItem = libraryItem,
				(data) => vm.logger.error('Error loading', data, 'Error')
			);
	}

	save(close : boolean) {
		var vm = this;
		vm.libraryService.saveLibraryItem(vm.libraryItem)
			.subscribe(
				(libraryItem) => {
					vm.libraryItem.uuid = libraryItem.uuid;
					vm.adminService.clearPendingChanges();
					vm.logger.success('Item saved', vm.libraryItem, 'Saved');
					if (close) {
						vm.state.go(vm.transition.from());
					}
				},
				(error) => vm.logger.error('Error saving', error, 'Error')
			);
	}

	close() {
		this.adminService.clearPendingChanges();
		this.state.go(this.transition.from());
	}
}
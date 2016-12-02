import {LibraryItem} from "../library/models/LibraryItem";
import {LibraryService} from "../library/library.service";
import {LoggerService} from "../common/logger.service";
import {AdminService} from "../administration/admin.service";
import {CodePickerDialog} from "../coding/codePicker.dialog";
import {Component} from "@angular/core";
import {Transition, StateService} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CodingService} from "../coding/coding.service";

@Component({
	template : require('./codeSetEditor.html')
})
export class CodeSetEditComponent {
	libraryItem : LibraryItem;
	termCache : any;

	constructor(
		protected libraryService : LibraryService,
		protected logger : LoggerService,
		protected $modal : NgbModal,
		protected adminService : AdminService,
		protected state : StateService,
		protected transition : Transition,
		protected codingService : CodingService) {

		this.termCache = {};
		this.performAction(transition.params()['itemAction'], transition.params()['itemUuid']);
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

	termShorten(term : string) {
		term = term.replace(' (disorder)', '');
		term = term.replace(' (observable entity)', '');
		term = term.replace(' (finding)', '');
		return term;
	}

	getTerm(code : string) : string {
		var vm = this;
		var term = vm.termCache[code];
		if (term) { return term; }
		vm.termCache[code] = 'Loading...';

		vm.codingService.getPreferredTerm(code)
			.subscribe(
				(concept) => vm.termCache[code] = vm.termShorten(concept.preferredTerm)
			);

		return vm.termCache[code];
	}

	showCodePicker() {
		var vm = this;
		CodePickerDialog.open(vm.$modal, vm.libraryItem.codeSet.codeSetValue)
			.result.then(function(result) {
				vm.libraryItem.codeSet.codeSetValue = result;
		});
	}


	create(folderUuid: string) {
		this.libraryItem = {
			uuid: null,
			name: 'New item',
			description: '',
			folderUuid: folderUuid,
			codeSet: {
				codingSystem: 'SNOMED_CT',
				codeSetValue: []
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

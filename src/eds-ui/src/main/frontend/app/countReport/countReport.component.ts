import {Component} from "@angular/core";
import {Transition, StateService} from "ui-router-ng2";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CountReport} from "./models/CountReport";
import {CountReportService} from "./countReport.service";
import {AdminService, LibraryService, LoggerService, MessageBoxDialog} from "eds-common-js";
import {EdsLibraryItem} from "../edsLibrary/models/EdsLibraryItem";

@Component({
	template : require('./countReportEditor.html')
})
export class CountReportEditComponent {
	libraryItem : EdsLibraryItem;
	termCache : any;

	constructor(
		protected libraryService : LibraryService,
		protected countReportService : CountReportService,
		protected logger : LoggerService,
		protected $modal : NgbModal,
		protected adminService : AdminService,
		protected state : StateService,
		protected transition : Transition) {

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

	runQuery() {
		var vm = this;
		let query = vm.libraryItem.countReport.query;
		// vm.countReportService.runReport()
		MessageBoxDialog.open(
			this.$modal,
			'Count Report : ' + this.libraryItem.name,
			'Execution successful - (n) patients counted.  Would you like to export their NHS Numbers?',
			'Yes',
			'No'
		);
	}

	create(folderUuid: string) {
		this.libraryItem = {
			uuid: null,
			name: '',
			description: '',
			folderUuid: folderUuid,
			countReport: {
				fields : '',
				tables : '',
				query : '',
				count : 0,
				status : 'Not Run'
			} as CountReport
		} as EdsLibraryItem;
	}

	load(uuid : string) {
		var vm = this;
		this.create(null);
		vm.libraryService.getLibraryItem<EdsLibraryItem>(uuid)
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

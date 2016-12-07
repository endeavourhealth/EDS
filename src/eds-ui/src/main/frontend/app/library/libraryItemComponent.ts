import {AdminService} from "../administration/admin.service";
import {LoggerService} from "../common/logger.service";
import {LibraryItem} from "./models/LibraryItem";
import {LibraryService} from "./library.service";
import {Transition, StateService} from "ui-router-ng2";

export class LibraryItemComponent {
	protected libraryItem : LibraryItem = <LibraryItem>{};

	constructor(
		protected libraryService : LibraryService,
		protected adminService : AdminService,
		protected log : LoggerService,
		protected transition : Transition,
		protected state : StateService) {

		this.performAction(transition.params()['itemAction'], transition.params()['itemUuid']);
	}

	protected performAction(action:string, itemUuid:string) {
		switch (action) {
			case 'add':
				this.create(itemUuid);
				break;
			case 'edit':
				this.load(itemUuid);
				break;
		}
	}

	create(folderUuid : string) {
		this.libraryItem = {
			uuid : null,
			name : '',
			description : '',
			folderUuid : folderUuid,
		} as LibraryItem;
	}

	load(uuid : string) {
		var vm = this;
		vm.libraryService.getLibraryItem(uuid)
			.subscribe(
				(libraryItem) => vm.libraryItem = libraryItem,
				(error) => vm.log.error('Error loading', error, 'Error')
			);
	}

	save(close : boolean) {
		var vm = this;
		vm.libraryService.saveLibraryItem(vm.libraryItem)
			.subscribe(
				(libraryItem) => {
					vm.libraryItem.uuid = libraryItem.uuid;
					vm.adminService.clearPendingChanges();
					vm.log.success('Item saved', vm.libraryItem, 'Saved');
					if (close) {
						vm.state.go(vm.transition.from());
					}
				},
				(error) => vm.log.error('Error saving', error, 'Error')
			);
	}

	close() {
		this.adminService.clearPendingChanges();
		this.state.go(this.transition.from());
	}

}

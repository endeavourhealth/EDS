module app.library {
	import LibraryItem = app.models.LibraryItem;
	import ILibraryService = app.core.ILibraryService;
	import IWindowService = angular.IWindowService;
	export class LibraryItemModuleBase {
		protected libraryItem : LibraryItem;

		static $inject = ['LibraryService', 'AdminService', 'LoggerService', '$window', '$stateParams'];

		constructor(
			protected libraryService : ILibraryService,
			protected adminService : IAdminService,
			protected logger : ILoggerService,
			protected $window : IWindowService,
			protected $stateParams : {itemAction : string, itemUuid : string}) {

			this.performAction($stateParams.itemAction, $stateParams.itemUuid);
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
				name : 'New item',
				description : '',
				folderUuid : folderUuid,
			} as LibraryItem;
		}

		load(uuid : string) {
			var vm = this;
			vm.libraryService.getLibraryItem(uuid)
				.then(function(libraryItem : LibraryItem) {
					vm.libraryItem = libraryItem;
				})
				.catch(function(data) {
					vm.logger.error('Error loading', data, 'Error');
				});
		}

		save(close : boolean) {
			var vm = this;
			vm.libraryService.saveLibraryItem(vm.libraryItem)
				.then(function(libraryItem : LibraryItem) {
					vm.libraryItem.uuid = libraryItem.uuid;
					vm.adminService.clearPendingChanges();
					vm.logger.success('Item saved', vm.libraryItem, 'Saved');
					if (close) { vm.$window.history.back(); }
				})
				.catch(function(data) {
					vm.logger.error('Error saving', data, 'Error');
				});
		}

		close() {
			this.adminService.clearPendingChanges();
			this.$window.history.back();
		}

	}
}
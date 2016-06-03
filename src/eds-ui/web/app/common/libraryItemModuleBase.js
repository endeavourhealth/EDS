var app;
(function (app) {
    var library;
    (function (library) {
        var LibraryItemModuleBase = (function () {
            function LibraryItemModuleBase(libraryService, adminService, logger, $window, $stateParams) {
                this.libraryService = libraryService;
                this.adminService = adminService;
                this.logger = logger;
                this.$window = $window;
                this.$stateParams = $stateParams;
                this.performAction($stateParams.itemAction, $stateParams.itemUuid);
            }
            LibraryItemModuleBase.prototype.performAction = function (action, itemUuid) {
                switch (action) {
                    case 'add':
                        this.create(itemUuid);
                        break;
                    case 'edit':
                        this.load(itemUuid);
                        break;
                }
            };
            LibraryItemModuleBase.prototype.create = function (folderUuid) {
                this.libraryItem = {
                    uuid: null,
                    name: 'New item',
                    description: '',
                    folderUuid: folderUuid,
                };
            };
            LibraryItemModuleBase.prototype.load = function (uuid) {
                var vm = this;
                vm.libraryService.getLibraryItem(uuid)
                    .then(function (libraryItem) {
                    vm.libraryItem = libraryItem;
                })
                    .catch(function (data) {
                    vm.logger.error('Error loading', data, 'Error');
                });
            };
            LibraryItemModuleBase.prototype.save = function (close) {
                var vm = this;
                vm.libraryService.saveLibraryItem(vm.libraryItem)
                    .then(function (libraryItem) {
                    vm.libraryItem.uuid = libraryItem.uuid;
                    vm.adminService.clearPendingChanges();
                    vm.logger.success('Item saved', vm.libraryItem, 'Saved');
                    if (close) {
                        vm.$window.history.back();
                    }
                })
                    .catch(function (data) {
                    vm.logger.error('Error saving', data, 'Error');
                });
            };
            LibraryItemModuleBase.prototype.close = function () {
                this.adminService.clearPendingChanges();
                this.$window.history.back();
            };
            LibraryItemModuleBase.$inject = ['LibraryService', 'AdminService', 'LoggerService', '$window', '$stateParams'];
            return LibraryItemModuleBase;
        })();
        library.LibraryItemModuleBase = LibraryItemModuleBase;
    })(library = app.library || (app.library = {}));
})(app || (app = {}));
//# sourceMappingURL=libraryItemModuleBase.js.map
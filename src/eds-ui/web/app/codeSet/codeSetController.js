/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var codeSet;
    (function (codeSet) {
        var CodePickerController = app.dialogs.CodePickerController;
        var LibraryItemModuleBase = app.library.LibraryItemModuleBase;
        'use strict';
        var CodeSetController = (function (_super) {
            __extends(CodeSetController, _super);
            function CodeSetController(libraryService, logger, $modal, adminService, $window, $stateParams, codingService) {
                _super.call(this, libraryService, adminService, logger, $window, $stateParams);
                this.libraryService = libraryService;
                this.logger = logger;
                this.$modal = $modal;
                this.adminService = adminService;
                this.$window = $window;
                this.$stateParams = $stateParams;
                this.codingService = codingService;
                this.termCache = {};
            }
            CodeSetController.prototype.create = function (folderUuid) {
                _super.prototype.create.call(this, folderUuid);
                this.libraryItem.codeSet = {
                    codingSystem: 'SNOMED_CT',
                    codeSetValue: []
                };
            };
            CodeSetController.prototype.termShorten = function (term) {
                term = term.replace(' (disorder)', '');
                term = term.replace(' (observable entity)', '');
                term = term.replace(' (finding)', '');
                return term;
            };
            CodeSetController.prototype.getTerm = function (code) {
                var vm = this;
                var term = vm.termCache[code];
                if (term) {
                    return term;
                }
                vm.termCache[code] = 'Loading...';
                vm.codingService.getPreferredTerm(code)
                    .then(function (concept) {
                    vm.termCache[code] = vm.termShorten(concept.preferredTerm);
                });
                return vm.termCache[code];
            };
            CodeSetController.prototype.showCodePicker = function () {
                var vm = this;
                CodePickerController.open(vm.$modal, vm.libraryItem.codeSet.codeSetValue)
                    .result.then(function (result) {
                    vm.libraryItem.codeSet.codeSetValue = result;
                });
            };
            CodeSetController.$inject = ['LibraryService', 'LoggerService',
                '$uibModal', 'AdminService', '$window', '$stateParams', 'CodingService'];
            return CodeSetController;
        })(LibraryItemModuleBase);
        codeSet.CodeSetController = CodeSetController;
        angular
            .module('app.codeSet')
            .controller('CodeSetController', CodeSetController);
    })(codeSet = app.codeSet || (app.codeSet = {}));
})(app || (app = {}));
//# sourceMappingURL=codeSetController.js.map
/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var system;
    (function (system) {
        var LibraryItemModuleBase = app.library.LibraryItemModuleBase;
        'use strict';
        var SystemController = (function (_super) {
            __extends(SystemController, _super);
            function SystemController(libraryService, serviceService, logger, $modal, adminService, $window, $stateParams) {
                _super.call(this, libraryService, adminService, logger, $window, $stateParams);
                this.libraryService = libraryService;
                this.serviceService = serviceService;
                this.logger = logger;
                this.$modal = $modal;
                this.adminService = adminService;
                this.$window = $window;
                this.$stateParams = $stateParams;
                this.formats = ["EMISOPEN", "OPENHR", "EMISCSV", "TPPCSV", "TPPXML", "FHIRJSON", "FHIRXML", "VITRUCARE", "EDWXML", "TABLEAU"];
                this.types = ["Patient Record", "Demographics", "Appointments", "Summary", "Discharge", "Episode"];
            }
            SystemController.prototype.create = function (folderUuid) {
                this.system = {
                    uuid: null,
                    name: 'New system',
                    technicalInterface: []
                };
                this.libraryItem = {
                    uuid: null,
                    name: 'New system',
                    description: '',
                    folderUuid: folderUuid,
                    system: this.system
                };
            };
            SystemController.prototype.addInterface = function () {
                this.selectedInterface = {
                    uuid: null,
                    name: 'New interface',
                    messageType: '',
                    messageFormat: '',
                    messageFormatVersion: ''
                };
                this.libraryItem.system.technicalInterface.push(this.selectedInterface);
            };
            SystemController.prototype.removeInterface = function (scope) {
                this.libraryItem.system.technicalInterface.splice(scope.$index, 1);
                if (this.selectedInterface === scope.item) {
                    this.selectedInterface = null;
                }
            };
            SystemController.$inject = ['LibraryService', 'ServiceService', 'LoggerService',
                '$uibModal', 'AdminService', '$window', '$stateParams'];
            return SystemController;
        })(LibraryItemModuleBase);
        system.SystemController = SystemController;
        angular
            .module('app.system')
            .controller('SystemController', SystemController);
    })(system = app.system || (app.system = {}));
})(app || (app = {}));
//# sourceMappingURL=system.controller.js.map
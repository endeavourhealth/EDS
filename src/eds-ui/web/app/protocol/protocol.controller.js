/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var protocol;
    (function (protocol) {
        var LibraryItemModuleBase = app.library.LibraryItemModuleBase;
        'use strict';
        var ProtocolController = (function (_super) {
            __extends(ProtocolController, _super);
            function ProtocolController(libraryService, logger, $modal, adminService, $window, $stateParams) {
                _super.call(this, libraryService, adminService, logger, $window, $stateParams);
                this.libraryService = libraryService;
                this.logger = logger;
                this.$modal = $modal;
                this.adminService = adminService;
                this.$window = $window;
                this.$stateParams = $stateParams;
                this.enabled = [true, false];
                this.consent = [true, false];
            }
            ProtocolController.prototype.create = function (folderUuid) {
                this.protocol = {
                    enabled: true,
                    patientConsent: true
                };
                this.libraryItem = {
                    uuid: null,
                    name: 'New item',
                    description: '',
                    folderUuid: folderUuid,
                    protocol: this.protocol
                };
            };
            ProtocolController.$inject = ['LibraryService', 'LoggerService',
                '$uibModal', 'AdminService', '$window', '$stateParams'];
            return ProtocolController;
        })(LibraryItemModuleBase);
        protocol.ProtocolController = ProtocolController;
        angular
            .module('app.protocol')
            .controller('ProtocolController', ProtocolController);
    })(protocol = app.protocol || (app.protocol = {}));
})(app || (app = {}));
//# sourceMappingURL=protocol.controller.js.map
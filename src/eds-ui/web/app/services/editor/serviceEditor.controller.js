/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var service;
    (function (service_1) {
        var BaseDialogController = app.dialogs.BaseDialogController;
        var OrganisationPickerController = app.organisation.OrganisationPickerController;
        'use strict';
        var ServiceEditorController = (function (_super) {
            __extends(ServiceEditorController, _super);
            function ServiceEditorController($uibModalInstance, $modal, log, adminService, serviceService, service) {
                _super.call(this, $uibModalInstance);
                this.$uibModalInstance = $uibModalInstance;
                this.$modal = $modal;
                this.log = log;
                this.adminService = adminService;
                this.serviceService = serviceService;
                this.resultData = jQuery.extend(true, {}, service);
                if (service.uuid)
                    this.getServiceOrganisations(service.uuid);
            }
            ServiceEditorController.open = function ($modal, service) {
                var options = {
                    templateUrl: 'app/services/editor/serviceEditor.html',
                    controller: 'ServiceEditorController',
                    controllerAs: 'ctrl',
                    backdrop: 'static',
                    resolve: {
                        service: function () { return service; }
                    }
                };
                var dialog = $modal.open(options);
                return dialog;
            };
            ServiceEditorController.prototype.getServiceOrganisations = function (uuid) {
                var vm = this;
                vm.serviceService.getServiceOrganisations(uuid)
                    .then(function (result) {
                    vm.organisations = result;
                })
                    .catch(function (error) {
                    vm.log.error('Failed to load organisation services', error, 'Load organisation services');
                });
            };
            ServiceEditorController.prototype.editOrganisations = function () {
                var vm = this;
                OrganisationPickerController.open(vm.$modal, vm.organisations)
                    .result.then(function (result) {
                    vm.organisations = result;
                });
            };
            ServiceEditorController.prototype.ok = function () {
                // build new list of service orgs
                this.resultData.organisations = {};
                for (var idx in this.organisations) {
                    var organisation = this.organisations[idx];
                    this.resultData.organisations[organisation.uuid] = organisation.name;
                }
                _super.prototype.ok.call(this);
            };
            ServiceEditorController.$inject = ['$uibModalInstance', '$uibModal', 'LoggerService', 'AdminService', 'ServiceService', 'service'];
            return ServiceEditorController;
        })(BaseDialogController);
        service_1.ServiceEditorController = ServiceEditorController;
        angular
            .module('app.service')
            .controller('ServiceEditorController', ServiceEditorController);
    })(service = app.service || (app.service = {}));
})(app || (app = {}));
//# sourceMappingURL=serviceEditor.controller.js.map
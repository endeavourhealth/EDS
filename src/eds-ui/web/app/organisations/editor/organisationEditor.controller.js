/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var organisation;
    (function (organisation_1) {
        var BaseDialogController = app.dialogs.BaseDialogController;
        var ServicePickerController = app.service.ServicePickerController;
        'use strict';
        var OrganisationEditorController = (function (_super) {
            __extends(OrganisationEditorController, _super);
            function OrganisationEditorController($uibModalInstance, $modal, log, adminService, organisationService, organisation) {
                _super.call(this, $uibModalInstance);
                this.$uibModalInstance = $uibModalInstance;
                this.$modal = $modal;
                this.log = log;
                this.adminService = adminService;
                this.organisationService = organisationService;
                this.organisation = organisation;
                this.resultData = jQuery.extend(true, {}, organisation);
                if (organisation.uuid)
                    this.getOrganisationServices(organisation.uuid);
            }
            OrganisationEditorController.open = function ($modal, organisation) {
                var options = {
                    templateUrl: 'app/organisations/editor/organisationEditor.html',
                    controller: 'OrganisationEditorController',
                    controllerAs: 'ctrl',
                    backdrop: 'static',
                    resolve: {
                        organisation: function () { return organisation; }
                    }
                };
                var dialog = $modal.open(options);
                return dialog;
            };
            OrganisationEditorController.prototype.getOrganisationServices = function (uuid) {
                var vm = this;
                vm.organisationService.getOrganisationServices(uuid)
                    .then(function (result) {
                    vm.services = result;
                })
                    .catch(function (error) {
                    vm.log.error('Failed to load organisation services', error, 'Load organisation services');
                });
            };
            OrganisationEditorController.prototype.editServices = function () {
                var vm = this;
                ServicePickerController.open(vm.$modal, vm.services)
                    .result.then(function (result) {
                    vm.services = result;
                    // TODO : SAVE LINKS TO DB
                });
            };
            OrganisationEditorController.prototype.ok = function () {
                // build new list of service orgs
                this.resultData.services = {};
                for (var idx in this.services) {
                    var service = this.services[idx];
                    this.resultData.services[service.uuid] = service.name;
                }
                _super.prototype.ok.call(this);
            };
            OrganisationEditorController.$inject = ['$uibModalInstance', '$uibModal', 'LoggerService', 'AdminService', 'OrganisationService', 'organisation'];
            return OrganisationEditorController;
        })(BaseDialogController);
        organisation_1.OrganisationEditorController = OrganisationEditorController;
        angular
            .module('app.organisation')
            .controller('OrganisationEditorController', OrganisationEditorController);
    })(organisation = app.organisation || (app.organisation = {}));
})(app || (app = {}));
//# sourceMappingURL=organisationEditor.controller.js.map
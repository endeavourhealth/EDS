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
    (function (organisation) {
        var BaseDialogController = app.dialogs.BaseDialogController;
        'use strict';
        var OrganisationPickerController = (function (_super) {
            __extends(OrganisationPickerController, _super);
            function OrganisationPickerController($uibModalInstance, logger, adminService, organisationService, organisations) {
                _super.call(this, $uibModalInstance);
                this.$uibModalInstance = $uibModalInstance;
                this.logger = logger;
                this.adminService = adminService;
                this.organisationService = organisationService;
                this.organisations = organisations;
                this.resultData = jQuery.extend(true, [], organisations);
            }
            OrganisationPickerController.open = function ($modal, organisations) {
                var options = {
                    templateUrl: 'app/organisations/picker/organisationPicker.html',
                    controller: 'OrganisationPickerController',
                    controllerAs: 'ctrl',
                    backdrop: 'static',
                    resolve: {
                        organisations: function () { return organisations; }
                    }
                };
                var dialog = $modal.open(options);
                return dialog;
            };
            OrganisationPickerController.prototype.search = function () {
                var vm = this;
                vm.organisationService.search(vm.searchData)
                    .then(function (result) {
                    vm.searchResults = result;
                })
                    .catch(function (error) {
                });
            };
            OrganisationPickerController.prototype.addToSelection = function (match) {
                if ($.grep(this.resultData, function (o) { return o.uuid === match.uuid; }).length === 0)
                    this.resultData.push(match);
            };
            OrganisationPickerController.prototype.removeFromSelection = function (match) {
                var index = this.resultData.indexOf(match, 0);
                if (index > -1)
                    this.resultData.splice(index, 1);
            };
            OrganisationPickerController.$inject = ['$uibModalInstance', 'LoggerService', 'AdminService', 'OrganisationService', 'organisations'];
            return OrganisationPickerController;
        })(BaseDialogController);
        organisation.OrganisationPickerController = OrganisationPickerController;
        angular
            .module('app.organisation')
            .controller('OrganisationPickerController', OrganisationPickerController);
    })(organisation = app.organisation || (app.organisation = {}));
})(app || (app = {}));
//# sourceMappingURL=organisationPicker.controller.js.map
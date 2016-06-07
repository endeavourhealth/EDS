/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var dialogs;
    (function (dialogs) {
        'use strict';
        var OrganisationPickerController = (function (_super) {
            __extends(OrganisationPickerController, _super);
            function OrganisationPickerController($uibModalInstance, $modal, log, organisationSetService, organisationList, organisationSet) {
                _super.call(this, $uibModalInstance);
                this.$uibModalInstance = $uibModalInstance;
                this.$modal = $modal;
                this.log = log;
                this.organisationSetService = organisationSetService;
                this.loadOrganisationSets();
                if (organisationSet) {
                    this.editMode = true;
                    this.selectSet(organisationSet);
                }
                else {
                    this.editMode = false;
                    this.resultData = {
                        uuid: null,
                        name: '<New Organisation Set>',
                        organisations: organisationList
                    };
                }
            }
            OrganisationPickerController.open = function ($modal, organisationList, organisationSet) {
                var options = {
                    templateUrl: 'app/dialogs/organisationPicker/organisationPicker.html',
                    controller: 'OrganisationPickerController',
                    controllerAs: 'ctrl',
                    backdrop: 'static',
                    resolve: {
                        organisationList: function () { return organisationList; },
                        organisationSet: function () { return organisationSet; }
                    }
                };
                var dialog = $modal.open(options);
                return dialog;
            };
            OrganisationPickerController.prototype.loadOrganisationSets = function () {
                var vm = this;
                vm.organisationSetService.getOrganisationSets()
                    .then(function (result) {
                    vm.organisationSetList = result;
                });
            };
            OrganisationPickerController.prototype.selectSet = function (organisationSet) {
                var vm = this;
                if (organisationSet === null) {
                    // Clear uuid and name (but not organisation list in case of "Save As")
                    vm.resultData.uuid = null;
                    vm.resultData.name = '<New Organisation Set>';
                }
                else {
                    // Create COPY of selected set (in case of "Save As")
                    vm.resultData = {
                        uuid: organisationSet.uuid,
                        name: organisationSet.name
                    };
                    vm.organisationSetService.getOrganisationSetMembers(organisationSet.uuid)
                        .then(function (result) {
                        vm.resultData.organisations = result;
                    });
                }
            };
            OrganisationPickerController.prototype.search = function () {
                var vm = this;
                vm.organisationSetService.searchOrganisations(vm.searchCriteria)
                    .then(function (result) {
                    vm.searchResults = result;
                });
            };
            OrganisationPickerController.prototype.addOrganisationToSelection = function (organisation) {
                if (this.resultData.organisations.every(function (item) { return item.odsCode !== organisation.odsCode; })) {
                    this.resultData.organisations.push(organisation);
                }
            };
            OrganisationPickerController.prototype.removeOrganisationFromSelection = function (organisation) {
                var index = this.resultData.organisations.indexOf(organisation);
                this.resultData.organisations.splice(index, 1);
            };
            OrganisationPickerController.prototype.removeAll = function () {
                this.resultData.organisations = [];
            };
            OrganisationPickerController.prototype.saveSet = function () {
                var vm = this;
                if (vm.resultData.uuid === null) {
                    dialogs.InputBoxController.open(vm.$modal, 'Save Organisation Set', 'Enter Set Name', 'New Organisation Set')
                        .result.then(function (result) {
                        vm.resultData.name = result;
                        vm.save();
                    });
                }
                else {
                    dialogs.MessageBoxController.open(vm.$modal, 'Save Organisation Set', 'You are about to update an existing set, are you sure you want to continue?', 'Yes', 'No')
                        .result.then(function () {
                        vm.save();
                    });
                }
            };
            OrganisationPickerController.prototype.save = function () {
                var vm = this;
                vm.organisationSetService.saveOrganisationSet(vm.resultData)
                    .then(function (result) {
                    vm.log.success('Organisation Set Saved', result, 'Saved');
                    if (vm.resultData.uuid === null) {
                        vm.resultData.uuid = result.uuid;
                        vm.organisationSetList.push(vm.resultData);
                    }
                });
            };
            OrganisationPickerController.$inject = ['$uibModalInstance', '$uibModal', 'LoggerService', 'OrganisationSetService',
                'organisationList', 'organisationSet'];
            return OrganisationPickerController;
        })(dialogs.BaseDialogController);
        dialogs.OrganisationPickerController = OrganisationPickerController;
        angular
            .module('app.dialogs')
            .controller('OrganisationPickerController', OrganisationPickerController);
    })(dialogs = app.dialogs || (app.dialogs = {}));
})(app || (app = {}));
//# sourceMappingURL=organisationPicker.controller.js.map
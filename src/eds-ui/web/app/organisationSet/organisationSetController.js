/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />
var app;
(function (app) {
    var organisationSet;
    (function (organisationSet_1) {
        var OrganisationPickerController = app.dialogs.OrganisationPickerController;
        var MessageBoxController = app.dialogs.MessageBoxController;
        'use strict';
        var OrganisationSetController = (function () {
            function OrganisationSetController($modal, organisationSetService, log) {
                this.$modal = $modal;
                this.organisationSetService = organisationSetService;
                this.log = log;
                this.getRootFolders();
            }
            OrganisationSetController.prototype.getRootFolders = function () {
                var vm = this;
                vm.organisationSetService.getOrganisationSets()
                    .then(function (result) {
                    vm.organisationSets = result;
                })
                    .catch(function (error) {
                    vm.log.error('Failed to load sets', error, 'Load sets');
                });
            };
            OrganisationSetController.prototype.selectOrganisationSet = function (item) {
                var vm = this;
                vm.selectedOrganisationSet = item;
                // Load members if necessary
                if (!item.organisations || item.organisations.length === 0) {
                    vm.organisationSetService.getOrganisationSetMembers(item.uuid)
                        .then(function (result) {
                        vm.selectedOrganisationSet.organisations = result;
                    });
                }
            };
            OrganisationSetController.prototype.showOrganisationPicker = function () {
                var vm = this;
                OrganisationPickerController.open(vm.$modal, null, vm.selectedOrganisationSet)
                    .result.then(function (organisationSet) {
                    vm.organisationSetService.saveOrganisationSet(organisationSet)
                        .then(function (result) {
                        vm.log.success('Organisation set saved', organisationSet, 'Save set');
                        vm.selectedOrganisationSet.organisations = organisationSet.organisations;
                    })
                        .catch(function (error) {
                        vm.log.error('Failed to save set', error, 'Save set');
                    });
                });
            };
            OrganisationSetController.prototype.deleteSet = function (item) {
                var vm = this;
                MessageBoxController.open(vm.$modal, 'Delete Organisation Set', 'Are you sure you want to delete the set?', 'Yes', 'No')
                    .result.then(function () {
                    vm.organisationSetService.deleteOrganisationSet(item)
                        .then(function () {
                        var i = vm.organisationSets.indexOf(item);
                        vm.organisationSets.splice(i, 1);
                        vm.log.success('Organisation set deleted', item, 'Delete set');
                    })
                        .catch(function (error) {
                        vm.log.error('Failed to delete set', error, 'Delete set');
                    });
                });
            };
            OrganisationSetController.$inject = ['$uibModal', 'OrganisationSetService', 'LoggerService'];
            return OrganisationSetController;
        })();
        organisationSet_1.OrganisationSetController = OrganisationSetController;
        angular
            .module('app.organisationSet')
            .controller('OrganisationSetController', OrganisationSetController);
    })(organisationSet = app.organisationSet || (app.organisationSet = {}));
})(app || (app = {}));
//# sourceMappingURL=organisationSetController.js.map
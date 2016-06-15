/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../core/library.service.ts" />
var app;
(function (app) {
    var organisation;
    (function (organisation) {
        var MessageBoxController = app.dialogs.MessageBoxController;
        'use strict';
        var OrganisationListController = (function () {
            function OrganisationListController($modal, organisationService, log) {
                this.$modal = $modal;
                this.organisationService = organisationService;
                this.log = log;
                this.getOrganisations();
            }
            OrganisationListController.prototype.getOrganisations = function () {
                var vm = this;
                vm.organisationService.getOrganisations()
                    .then(function (result) {
                    vm.organisations = result;
                })
                    .catch(function (error) {
                    vm.log.error('Failed to load organisations', error, 'Load organisations');
                });
            };
            OrganisationListController.prototype.edit = function (item) {
                var vm = this;
                organisation.OrganisationEditorController.open(vm.$modal, item)
                    .result.then(function (result) {
                    jQuery.extend(true, item, result);
                    vm.organisationService.saveOrganisation(item)
                        .then(function () {
                        vm.log.success('Organisation saved', item, 'Save organisation');
                    })
                        .catch(function (error) {
                        vm.log.error('Failed to save organisation', error, 'Save organisation');
                    });
                });
            };
            OrganisationListController.prototype.delete = function (item) {
                var vm = this;
                MessageBoxController.open(vm.$modal, 'Delete Organisation', 'Are you sure you want to delete the Organisation?', 'Yes', 'No')
                    .result.then(function () {
                    // remove item from list
                    vm.organisationService.deleteOrganisation(item.uuid)
                        .then(function () {
                        vm.log.success('Organisation deleted', item, 'Delete Organisation');
                    })
                        .catch(function (error) {
                        vm.log.error('Failed to delete Organisation', error, 'Delete Organisation');
                    });
                });
            };
            OrganisationListController.$inject = ['$uibModal', 'OrganisationService', 'LoggerService'];
            return OrganisationListController;
        })();
        organisation.OrganisationListController = OrganisationListController;
        angular
            .module('app.organisation')
            .controller('OrganisationListController', OrganisationListController);
    })(organisation = app.organisation || (app.organisation = {}));
})(app || (app = {}));
//# sourceMappingURL=organisationListController.js.map
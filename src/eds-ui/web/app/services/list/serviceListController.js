/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../core/library.service.ts" />
var app;
(function (app) {
    var service;
    (function (service) {
        var MessageBoxController = app.dialogs.MessageBoxController;
        var Service = app.models.Service;
        'use strict';
        var ServiceListController = (function () {
            function ServiceListController($modal, serviceService, log) {
                this.$modal = $modal;
                this.serviceService = serviceService;
                this.log = log;
                this.getAll();
            }
            ServiceListController.prototype.getAll = function () {
                var vm = this;
                vm.serviceService.getAll()
                    .then(function (result) {
                    vm.services = result;
                })
                    .catch(function (error) {
                    vm.log.error('Failed to load services', error, 'Load services');
                });
            };
            ServiceListController.prototype.add = function () {
                var newService = new Service();
                this.edit(newService);
            };
            ServiceListController.prototype.edit = function (item) {
                var vm = this;
                service.ServiceEditorController
                    .open(vm.$modal, item)
                    .result
                    .then(function (result) {
                    vm.save(result);
                });
            };
            ServiceListController.prototype.save = function (item) {
                var vm = this;
                vm.serviceService.save(item)
                    .then(function (savedService) {
                    if (item.uuid)
                        jQuery.extend(true, item, savedService);
                    else
                        vm.services.push(savedService);
                    vm.log.success('Service saved', item, 'Save service');
                })
                    .catch(function (error) {
                    vm.log.error('Failed to save service', error, 'Save service');
                });
            };
            ServiceListController.prototype.delete = function (item) {
                var vm = this;
                MessageBoxController.open(vm.$modal, 'Delete Service', 'Are you sure you want to delete the Service?', 'Yes', 'No')
                    .result.then(function () {
                    // remove item from list
                    vm.serviceService.delete(item.uuid)
                        .then(function () {
                        var index = vm.services.indexOf(item);
                        vm.services.splice(index, 1);
                        vm.log.success('Service deleted', item, 'Delete Service');
                    })
                        .catch(function (error) {
                        vm.log.error('Failed to delete Service', error, 'Delete Service');
                    });
                });
            };
            ServiceListController.$inject = ['$uibModal', 'ServiceService', 'LoggerService'];
            return ServiceListController;
        })();
        service.ServiceListController = ServiceListController;
        angular
            .module('app.service')
            .controller('ServiceListController', ServiceListController);
    })(service = app.service || (app.service = {}));
})(app || (app = {}));
//# sourceMappingURL=serviceListController.js.map
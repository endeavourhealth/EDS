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
    (function (service) {
        var BaseDialogController = app.dialogs.BaseDialogController;
        'use strict';
        var ServicePickerController = (function (_super) {
            __extends(ServicePickerController, _super);
            function ServicePickerController($uibModalInstance, logger, adminService, serviceService, services) {
                _super.call(this, $uibModalInstance);
                this.$uibModalInstance = $uibModalInstance;
                this.logger = logger;
                this.adminService = adminService;
                this.serviceService = serviceService;
                this.services = services;
                this.resultData = jQuery.extend(true, [], services);
            }
            ServicePickerController.open = function ($modal, services) {
                var options = {
                    templateUrl: 'app/services/picker/servicePicker.html',
                    controller: 'ServicePickerController',
                    controllerAs: 'ctrl',
                    backdrop: 'static',
                    resolve: {
                        services: function () { return services; }
                    }
                };
                var dialog = $modal.open(options);
                return dialog;
            };
            ServicePickerController.prototype.search = function () {
                var vm = this;
                vm.serviceService.search(vm.searchData)
                    .then(function (result) {
                    vm.searchResults = result;
                })
                    .catch(function (error) {
                });
            };
            ServicePickerController.prototype.addToSelection = function (match) {
                if ($.grep(this.resultData, function (s) { return s.uuid === match.uuid; }).length === 0)
                    this.resultData.push(match);
            };
            ServicePickerController.prototype.removeFromSelection = function (match) {
                var index = this.resultData.indexOf(match, 0);
                if (index > -1)
                    this.resultData.splice(index, 1);
            };
            ServicePickerController.$inject = ['$uibModalInstance', 'LoggerService', 'AdminService', 'ServiceService', 'services'];
            return ServicePickerController;
        })(BaseDialogController);
        service.ServicePickerController = ServicePickerController;
        angular
            .module('app.service')
            .controller('ServicePickerController', ServicePickerController);
    })(service = app.service || (app.service = {}));
})(app || (app = {}));
//# sourceMappingURL=servicePicker.controller.js.map
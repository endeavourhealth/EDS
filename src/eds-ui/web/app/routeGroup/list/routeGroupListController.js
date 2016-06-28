/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../core/library.service.ts" />
var app;
(function (app) {
    var routeGroup;
    (function (routeGroup_1) {
        var MessageBoxController = app.dialogs.MessageBoxController;
        'use strict';
        var RouteGroupListController = (function () {
            function RouteGroupListController($modal, routeGroupService, rabbitService, log) {
                this.$modal = $modal;
                this.routeGroupService = routeGroupService;
                this.rabbitService = rabbitService;
                this.log = log;
                this.getRouteGroups();
                this.getRabbitBindings();
            }
            RouteGroupListController.prototype.getRouteGroups = function () {
                var vm = this;
                vm.routeGroupService.getRouteGroups()
                    .then(function (result) {
                    vm.routeGroups = result;
                })
                    .catch(function (error) {
                    vm.log.error('Failed to load route groups', error, 'Load route groups');
                });
            };
            RouteGroupListController.prototype.getRabbitBindings = function () {
                var vm = this;
                // TODO : Determine fastest node and use for address
                vm.rabbitService.getRabbitBindings('DUMMYADDRESS')
                    .then(function (result) {
                    vm.inboundBindings = $.grep(result, function (e) { return e.source === 'EdsInbound'; });
                    vm.interimBindings = $.grep(result, function (e) { return e.source === 'EdsInterim'; });
                    vm.responseBindings = $.grep(result, function (e) { return e.source === 'EdsResponse'; });
                    vm.subscriberBindings = $.grep(result, function (e) { return e.source === 'EdsSubscriber'; });
                })
                    .catch(function (error) {
                    vm.log.error('Failed to load rabbit bindings', error, 'Load rabbit bindings');
                });
            };
            RouteGroupListController.prototype.getRouteGroupClass = function (routeGroup, bindings) {
                if (!bindings)
                    return 'fa fa-blank text-default';
                if ($.grep(bindings, function (e) { return e.routing_key === routeGroup.routeKey; }).length === 0)
                    return 'fa fa-plus-circle text-danger';
                return 'fa fa-check-circle text-success';
            };
            RouteGroupListController.prototype.bindingExists = function (item) {
                if ($.grep(this.routeGroups, function (e) { return e.routeKey === item.routing_key; }).length === 0)
                    return false;
                return true;
            };
            RouteGroupListController.prototype.edit = function (item) {
                var vm = this;
                routeGroup_1.RouteGroupEditorController.open(vm.$modal, item)
                    .result.then(function (routeGroup) {
                    jQuery.extend(true, item, routeGroup);
                    vm.routeGroupService.saveRouteGroups(vm.routeGroups)
                        .then(function () {
                        vm.log.success('Route group saved', routeGroup, 'Save routeGroup');
                    })
                        .catch(function (error) {
                        vm.log.error('Failed to save route group', error, 'Save route group');
                    });
                });
            };
            RouteGroupListController.prototype.delete = function (item) {
                var vm = this;
                MessageBoxController.open(vm.$modal, 'Delete Route group', 'Are you sure you want to delete the route group?', 'Yes', 'No')
                    .result.then(function () {
                    // remove item from list
                    vm.routeGroupService.saveRouteGroups(vm.routeGroups)
                        .then(function () {
                        vm.log.success('Route group deleted', item, 'Delete route group');
                    })
                        .catch(function (error) {
                        vm.log.error('Failed to delete route group', error, 'Delete route group');
                    });
                });
            };
            RouteGroupListController.prototype.sync = function () {
                var vm = this;
                MessageBoxController.open(vm.$modal, 'Synchronise RabbitMQ', 'Are you sure you want to synchronise RabbitMQ with the defined route groups?', 'Yes', 'No')
                    .result.then(function () {
                    //  TODO : Determine fastest node and use for address
                    vm.rabbitService.synchronize("DUMMYADDRESS")
                        .then(function (result) {
                        vm.log.success('RabbitMQ synchronized');
                        vm.getRabbitBindings();
                    })
                        .catch(function (error) {
                        vm.log.error('Failed to synchronize', error, 'Synchronize RabbitMQ');
                        vm.getRabbitBindings();
                    });
                });
            };
            RouteGroupListController.prototype.splitBindings = function () {
            };
            RouteGroupListController.$inject = ['$uibModal', 'RouteGroupService', 'RabbitService', 'LoggerService'];
            return RouteGroupListController;
        })();
        routeGroup_1.RouteGroupListController = RouteGroupListController;
        angular
            .module('app.routeGroup')
            .controller('RouteGroupListController', RouteGroupListController);
    })(routeGroup = app.routeGroup || (app.routeGroup = {}));
})(app || (app = {}));
//# sourceMappingURL=routeGroupListController.js.map
/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../core/library.service.ts" />
var app;
(function (app) {
    var routeGroup;
    (function (routeGroup_1) {
        var MessageBoxController = app.dialogs.MessageBoxController;
        'use strict';
        var RouteGroupListController = (function () {
            function RouteGroupListController($modal, routeGroupService, log) {
                this.$modal = $modal;
                this.routeGroupService = routeGroupService;
                this.log = log;
                this.getRouteGroups();
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
            RouteGroupListController.$inject = ['$uibModal', 'RouteGroupService', 'LoggerService'];
            return RouteGroupListController;
        })();
        routeGroup_1.RouteGroupListController = RouteGroupListController;
        angular
            .module('app.routeGroup')
            .controller('RouteGroupListController', RouteGroupListController);
    })(routeGroup = app.routeGroup || (app.routeGroup = {}));
})(app || (app = {}));
//# sourceMappingURL=routeGroupListController.js.map
/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/library.service.ts" />
/// <reference path="../blocks/logger.service.ts" />
var app;
(function (app) {
    var dashboard;
    (function (dashboard) {
        var ItemType = app.models.ItemType;
        'use strict';
        var DashboardController = (function () {
            function DashboardController(dashboardService, logger, $state) {
                this.dashboardService = dashboardService;
                this.logger = logger;
                this.$state = $state;
                this.refresh();
            }
            DashboardController.prototype.refresh = function () {
                this.getRecentDocumentsData();
            };
            DashboardController.prototype.getRecentDocumentsData = function () {
                var vm = this;
                vm.recentDocumentsData = null;
                vm.dashboardService.getRecentDocumentsData()
                    .then(function (data) {
                    vm.recentDocumentsData = data;
                });
            };
            DashboardController.prototype.actionItem = function (item, action) {
                switch (item.type) {
                    case ItemType.Query:
                        this.$state.go('app.queryAction', { itemUuid: item.uuid, itemAction: action });
                        break;
                    case ItemType.ListOutput:
                        this.$state.go('app.listOutputAction', { itemUuid: item.uuid, itemAction: action });
                        break;
                    case ItemType.CodeSet:
                        this.$state.go('app.codeSetAction', { itemUuid: item.uuid, itemAction: action });
                        break;
                    case ItemType.Report:
                        this.$state.go('app.reportAction', { itemUuid: item.uuid, itemAction: action });
                        break;
                    case ItemType.Protocol:
                        this.$state.go('app.protocolAction', { itemUuid: item.uuid, itemAction: action });
                        break;
                }
            };
            DashboardController.$inject = ['DashboardService', 'LoggerService', '$state'];
            return DashboardController;
        })();
        angular
            .module('app.dashboard')
            .controller('DashboardController', DashboardController);
    })(dashboard = app.dashboard || (app.dashboard = {}));
})(app || (app = {}));
//# sourceMappingURL=dashboard.controller.js.map
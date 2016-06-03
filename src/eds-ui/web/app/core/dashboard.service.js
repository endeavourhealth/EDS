/// <reference path="../../typings/tsd.d.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var core;
    (function (core) {
        'use strict';
        var DashboardService = (function (_super) {
            __extends(DashboardService, _super);
            function DashboardService() {
                _super.apply(this, arguments);
            }
            DashboardService.prototype.getEngineHistory = function () {
                var request = {
                    params: {
                        'count': 5
                    }
                };
                return this.httpGet('api/dashboard/getEngineHistory', request);
            };
            DashboardService.prototype.getRecentDocumentsData = function () {
                var request = {
                    params: {
                        'count': 5
                    }
                };
                return this.httpGet('api/dashboard/getRecentDocuments', request);
            };
            DashboardService.prototype.getEngineState = function () {
                return this.httpGet('api/dashboard/getProcessorStatus');
            };
            DashboardService.prototype.getReportActivityData = function () {
                var request = {
                    params: {
                        'count': 5
                    }
                };
                return this.httpGet('api/dashboard/getReportActivity', request);
            };
            DashboardService.prototype.startEngine = function () {
                return this.httpPost('api/dashboard/startProcessor');
            };
            DashboardService.prototype.stopEngine = function () {
                return this.httpPost('api/dashboard/stopProcessor');
            };
            return DashboardService;
        })(core.BaseHttpService);
        core.DashboardService = DashboardService;
        angular
            .module('app.core')
            .service('DashboardService', DashboardService);
    })(core = app.core || (app.core = {}));
})(app || (app = {}));
//# sourceMappingURL=dashboard.service.js.map
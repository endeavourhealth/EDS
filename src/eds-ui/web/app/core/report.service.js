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
        var ReportService = (function (_super) {
            __extends(ReportService, _super);
            function ReportService() {
                _super.apply(this, arguments);
            }
            ReportService.prototype.saveReport = function (report) {
                return this.httpPost('api/report/saveReport', report);
            };
            ReportService.prototype.deleteReport = function (uuid) {
                var request = {
                    'uuid': uuid
                };
                return this.httpPost('api/report/deleteReport', request);
            };
            ReportService.prototype.getReport = function (uuid) {
                var request = {
                    params: {
                        'uuid': uuid
                    }
                };
                return this.httpGet('api/report/getReport', request);
            };
            ReportService.prototype.getContentNamesForReportLibraryItem = function (uuid) {
                var request = {
                    params: {
                        'uuid': uuid
                    }
                };
                return this.httpGet('api/library/getContentNamesForReportLibraryItem', request);
            };
            ReportService.prototype.scheduleReport = function (requestParameters) {
                return this.httpPost('api/report/scheduleReport', requestParameters);
            };
            ReportService.prototype.getReportSchedules = function (uuid, count) {
                var request = {
                    params: {
                        'uuid': uuid,
                        'count': count
                    }
                };
                return this.httpGet('api/report/getReportSchedules', request);
            };
            ReportService.prototype.getScheduleResults = function (uuid) {
                var request = {
                    params: {
                        'uuid': uuid
                    }
                };
                return this.httpGet('api/report/getScheduleResults', request);
            };
            return ReportService;
        })(core.BaseHttpService);
        core.ReportService = ReportService;
        angular
            .module('app.core')
            .service('ReportService', ReportService);
    })(core = app.core || (app.core = {}));
})(app || (app = {}));
//# sourceMappingURL=report.service.js.map
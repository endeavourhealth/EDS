/// <reference path="../../../typings/tsd.d.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var service;
    (function (service_1) {
        var BaseHttpService = app.core.BaseHttpService;
        'use strict';
        var ServiceService = (function (_super) {
            __extends(ServiceService, _super);
            function ServiceService() {
                _super.apply(this, arguments);
            }
            ServiceService.prototype.getAll = function () {
                return this.httpGet('api/service');
            };
            ServiceService.prototype.get = function (uuid) {
                var request = {
                    params: {
                        'uuid': uuid
                    }
                };
                return this.httpGet('api/service', request);
            };
            ServiceService.prototype.save = function (service) {
                return this.httpPost('api/service', service);
            };
            ServiceService.prototype.delete = function (uuid) {
                var request = {
                    params: {
                        'uuid': uuid
                    }
                };
                return this.httpDelete('api/service', request);
            };
            ServiceService.prototype.search = function (searchData) {
                var request = {
                    params: {
                        'searchData': searchData
                    }
                };
                return this.httpGet('api/service', request);
            };
            ServiceService.prototype.getServiceOrganisations = function (uuid) {
                var request = {
                    params: {
                        'uuid': uuid
                    }
                };
                return this.httpGet('api/service/organisations', request);
            };
            return ServiceService;
        })(BaseHttpService);
        service_1.ServiceService = ServiceService;
        angular
            .module('app.service')
            .service('ServiceService', ServiceService);
    })(service = app.service || (app.service = {}));
})(app || (app = {}));
//# sourceMappingURL=service.service.js.map
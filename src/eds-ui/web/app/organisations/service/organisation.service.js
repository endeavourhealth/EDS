/// <reference path="../../../typings/tsd.d.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var organisation;
    (function (organisation_1) {
        var BaseHttpService = app.core.BaseHttpService;
        'use strict';
        var OrganisationService = (function (_super) {
            __extends(OrganisationService, _super);
            function OrganisationService() {
                _super.apply(this, arguments);
            }
            OrganisationService.prototype.getOrganisations = function () {
                return this.httpGet('api/organisation');
            };
            OrganisationService.prototype.getOrganisation = function (uuid) {
                var request = {
                    params: {
                        'uuid': uuid
                    }
                };
                return this.httpGet('api/organisation', request);
            };
            OrganisationService.prototype.getOrganisationServices = function (uuid) {
                var request = {
                    params: {
                        'uuid': uuid
                    }
                };
                return this.httpGet('api/organisation/services', request);
            };
            OrganisationService.prototype.saveOrganisation = function (organisation) {
                return this.httpPost('api/organisation', organisation);
            };
            OrganisationService.prototype.deleteOrganisation = function (uuid) {
                var request = {
                    params: {
                        'uuid': uuid
                    }
                };
                return this.httpDelete('api/organisation', request);
            };
            OrganisationService.prototype.search = function (searchData) {
                var request = {
                    params: {
                        'searchData': searchData
                    }
                };
                return this.httpGet('api/organisation', request);
            };
            return OrganisationService;
        })(BaseHttpService);
        organisation_1.OrganisationService = OrganisationService;
        angular
            .module('app.organisation')
            .service('OrganisationService', OrganisationService);
    })(organisation = app.organisation || (app.organisation = {}));
})(app || (app = {}));
//# sourceMappingURL=organisation.service.js.map
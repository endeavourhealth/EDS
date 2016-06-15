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
        var OrganisationSetService = (function (_super) {
            __extends(OrganisationSetService, _super);
            function OrganisationSetService() {
                _super.apply(this, arguments);
            }
            OrganisationSetService.prototype.getOrganisationSets = function () {
                return this.httpGet('api/lookup/getOrganisationSets');
            };
            OrganisationSetService.prototype.getOrganisationSetMembers = function (uuid) {
                var request = {
                    params: {
                        'uuid': uuid
                    }
                };
                return this.httpGet('api/lookup/getOrganisationSetMembers', request);
            };
            OrganisationSetService.prototype.searchOrganisations = function (searchCriteria) {
                var request = {
                    params: {
                        'searchTerm': searchCriteria
                    }
                };
                return this.httpGet('api/lookup/searchOrganisations', request);
            };
            OrganisationSetService.prototype.saveOrganisationSet = function (organisationSet) {
                return this.httpPost('api/lookup/saveOrganisationSet', organisationSet);
            };
            OrganisationSetService.prototype.deleteOrganisationSet = function (organisationSet) {
                return this.httpPost('api/lookup/deleteOrganisationSet', organisationSet);
            };
            return OrganisationSetService;
        })(core.BaseHttpService);
        core.OrganisationSetService = OrganisationSetService;
        angular
            .module('app.core')
            .service('OrganisationSetService', OrganisationSetService);
    })(core = app.core || (app.core = {}));
})(app || (app = {}));
//# sourceMappingURL=organisationSet.service.js.map
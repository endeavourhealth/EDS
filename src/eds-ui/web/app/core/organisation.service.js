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
        var OrganisationService = (function (_super) {
            __extends(OrganisationService, _super);
            function OrganisationService() {
                _super.apply(this, arguments);
            }
            OrganisationService.prototype.getOrganisationSets = function () {
                return this.httpGet('api/lookup/getOrganisationSets');
            };
            OrganisationService.prototype.getOrganisationSetMembers = function (uuid) {
                var request = {
                    params: {
                        'uuid': uuid
                    }
                };
                return this.httpGet('api/lookup/getOrganisationSetMembers', request);
            };
            OrganisationService.prototype.searchOrganisations = function (searchCriteria) {
                var request = {
                    params: {
                        'searchTerm': searchCriteria
                    }
                };
                return this.httpGet('api/lookup/searchOrganisations', request);
            };
            OrganisationService.prototype.saveOrganisationSet = function (organisationSet) {
                return this.httpPost('api/lookup/saveOrganisationSet', organisationSet);
            };
            OrganisationService.prototype.deleteOrganisationSet = function (organisationSet) {
                return this.httpPost('api/lookup/deleteOrganisationSet', organisationSet);
            };
            return OrganisationService;
        })(core.BaseHttpService);
        core.OrganisationService = OrganisationService;
        angular
            .module('app.core')
            .service('OrganisationService', OrganisationService);
    })(core = app.core || (app.core = {}));
})(app || (app = {}));
//# sourceMappingURL=organisation.service.js.map
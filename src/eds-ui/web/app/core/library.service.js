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
        var LibraryService = (function (_super) {
            __extends(LibraryService, _super);
            function LibraryService() {
                _super.apply(this, arguments);
            }
            LibraryService.prototype.getLibraryItem = function (uuid) {
                var request = {
                    params: {
                        'uuid': uuid
                    }
                };
                return this.httpGet('api/library/getLibraryItem', request);
            };
            LibraryService.prototype.saveLibraryItem = function (libraryItem) {
                return this.httpPost('api/library/saveLibraryItem', libraryItem);
            };
            LibraryService.prototype.deleteLibraryItem = function (uuid) {
                var request = {
                    uuid: uuid
                };
                return this.httpPost('api/library/deleteLibraryItem', request);
            };
            LibraryService.prototype.getEntityMap = function () {
                return this.httpGet('api/entity/getEntityMap');
            };
            LibraryService.prototype.getSystems = function () {
                return this.httpGet('api/library/getSystems');
            };
            LibraryService.prototype.getCohorts = function () {
                return this.httpGet('api/library/getQueries');
            };
            LibraryService.prototype.getDatasets = function () {
                return this.httpGet('api/library/getListReports');
            };
            return LibraryService;
        })(core.BaseHttpService);
        core.LibraryService = LibraryService;
        angular
            .module('app.core')
            .service('LibraryService', LibraryService);
    })(core = app.core || (app.core = {}));
})(app || (app = {}));
//# sourceMappingURL=library.service.js.map
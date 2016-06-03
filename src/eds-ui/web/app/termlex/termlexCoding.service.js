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
        var TermlexCodingService = (function (_super) {
            __extends(TermlexCodingService, _super);
            function TermlexCodingService() {
                _super.apply(this, arguments);
            }
            TermlexCodingService.prototype.searchCodes = function (searchData) {
                var vm = this;
                var request = {
                    params: {
                        'term': searchData,
                        'maxResultsSize': 20,
                        'start': 0
                    },
                    withCredentials: false
                };
                var defer = vm.promise.defer();
                vm.http.get('http://termlex.org/search/sct', request)
                    .then(function (response) {
                    var termlexResult = response.data;
                    var matches = termlexResult.results.map(function (t) { return vm.termlexCodeToCodeSetValue(t); });
                    defer.resolve(matches);
                })
                    .catch(function (exception) {
                    defer.reject(exception);
                });
                return defer.promise;
            };
            TermlexCodingService.prototype.getCodeChildren = function (id) {
                var vm = this;
                var request = { withCredentials: false };
                var defer = vm.promise.defer();
                vm.http.get('http://termlex.org/hierarchy/' + id + '/childHierarchy', request)
                    .then(function (response) {
                    var termlexResult = response.data;
                    var matches = termlexResult.map(function (t) { return vm.termlexCodeToCodeSetValue(t); });
                    defer.resolve(matches);
                })
                    .catch(function (exception) {
                    defer.reject(exception);
                });
                return defer.promise;
            };
            TermlexCodingService.prototype.getCodeParents = function (id) {
                var vm = this;
                var request = { withCredentials: false };
                var defer = vm.promise.defer();
                vm.http.get('http://termlex.org/hierarchy/' + id + '/parentHierarchy', request)
                    .then(function (response) {
                    var termlexResult = response.data;
                    var matches = termlexResult.map(function (t) { return vm.termlexCodeToCodeSetValue(t); });
                    defer.resolve(matches);
                })
                    .catch(function (exception) {
                    defer.reject(exception);
                });
                return defer.promise;
            };
            TermlexCodingService.prototype.termlexCodeToCodeSetValue = function (termlexCode) {
                var codeSetValue = {
                    code: termlexCode.id,
                    includeChildren: null,
                    exclusion: null
                };
                return codeSetValue;
            };
            TermlexCodingService.prototype.getPreferredTerm = function (id) {
                var request = { withCredentials: false };
                return this.httpGet('http://termlex.org/concepts/' + id + '/?flavour=ID_LABEL', request);
            };
            return TermlexCodingService;
        })(core.BaseHttpService);
        core.TermlexCodingService = TermlexCodingService;
        angular
            .module('app.core')
            .service('CodingService', TermlexCodingService);
    })(core = app.core || (app.core = {}));
})(app || (app = {}));
//# sourceMappingURL=termlexCoding.service.js.map
/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var core;
    (function (core) {
        'use strict';
        var BaseHttpService = (function () {
            function BaseHttpService(http, promise) {
                this.http = http;
                this.promise = promise;
            }
            BaseHttpService.prototype.httpGet = function (url, request) {
                var defer = this.promise.defer();
                this.http.get(url, request)
                    .then(function (response) {
                    defer.resolve(response.data);
                })
                    .catch(function (exception) {
                    defer.reject(exception);
                });
                return defer.promise;
            };
            BaseHttpService.prototype.httpPost = function (url, request) {
                var defer = this.promise.defer();
                this.http.post(url, request)
                    .then(function (response) {
                    defer.resolve(response.data);
                })
                    .catch(function (exception) {
                    defer.reject(exception);
                });
                return defer.promise;
            };
            BaseHttpService.prototype.httpDelete = function (url, request) {
                var defer = this.promise.defer();
                this.http.delete(url, request)
                    .then(function (response) {
                    defer.resolve(response.data);
                })
                    .catch(function (exception) {
                    defer.reject(exception);
                });
                return defer.promise;
            };
            BaseHttpService.$inject = ['$http', '$q'];
            return BaseHttpService;
        })();
        core.BaseHttpService = BaseHttpService;
    })(core = app.core || (app.core = {}));
})(app || (app = {}));
//# sourceMappingURL=baseHttp.service.js.map
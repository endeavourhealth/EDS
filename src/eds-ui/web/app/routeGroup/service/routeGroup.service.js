/// <reference path="../../../typings/tsd.d.ts" />
var app;
(function (app) {
    var core;
    (function (core) {
        'use strict';
        var RouteGroupService = (function () {
            function RouteGroupService(promise, configService) {
                this.promise = promise;
                this.configService = configService;
                this.configurationId = "b9b14e26-5a52-4f36-ad89-f01e465c1361";
                this.configurationName = "RouteGroup";
            }
            RouteGroupService.prototype.getRouteGroups = function () {
                var defer = this.promise.defer();
                this.configService.getConfig(this.configurationId)
                    .then(function (configResource) {
                    defer.resolve(angular.fromJson(configResource.configurationData));
                })
                    .catch(function (exception) {
                    defer.reject(exception);
                });
                return defer.promise;
            };
            RouteGroupService.prototype.saveRouteGroups = function (routeGroups) {
                var configurationResource = {
                    configurationId: this.configurationId,
                    configurationName: this.configurationName,
                    configurationData: angular.toJson(routeGroups)
                };
                var defer = this.promise.defer();
                this.configService.saveConfig(configurationResource)
                    .then(function () {
                    defer.resolve();
                })
                    .catch(function (exception) {
                    defer.reject(exception);
                });
                return defer.promise;
            };
            RouteGroupService.$inject = ['$q', 'ConfigService'];
            return RouteGroupService;
        })();
        core.RouteGroupService = RouteGroupService;
        angular
            .module('app.routeGroup')
            .service('RouteGroupService', RouteGroupService);
    })(core = app.core || (app.core = {}));
})(app || (app = {}));
//# sourceMappingURL=routeGroup.service.js.map
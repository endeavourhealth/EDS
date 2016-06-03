/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../models/MenuOption.ts" />
/// <reference path="../models/Role.ts" />
/// <reference path="../models/User.ts" />
/// <reference path="../models/UserInRole.ts" />
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
        var SecurityService = (function (_super) {
            __extends(SecurityService, _super);
            function SecurityService() {
                _super.apply(this, arguments);
            }
            SecurityService.prototype.getCurrentUser = function () {
                return this.currentUser;
            };
            SecurityService.prototype.isAuthenticated = function () {
                return this.currentUser != null;
            };
            SecurityService.prototype.login = function (username, password) {
                var vm = this;
                vm.currentUser = null;
                var defer = vm.promise.defer();
                var request = {
                    'username': username,
                    'password': password
                };
                vm.http.post('/api/security/login', request)
                    .then(function (response) {
                    var loginResponse = response.data;
                    vm.currentUser = loginResponse.user;
                    defer.resolve(vm.currentUser);
                })
                    .catch(function (exception) {
                    defer.reject(exception);
                });
                return defer.promise;
            };
            SecurityService.prototype.switchUserInRole = function (userInRoleUuid) {
                var request = '"' + userInRoleUuid + '"';
                return this.httpPost('/api/security/switchUserInRole', request);
            };
            SecurityService.prototype.logout = function () {
                this.currentUser = null;
            };
            return SecurityService;
        })(core.BaseHttpService);
        core.SecurityService = SecurityService;
        angular
            .module('app.core')
            .service('SecurityService', SecurityService);
    })(core = app.core || (app.core = {}));
})(app || (app = {}));
//# sourceMappingURL=security.service.js.map
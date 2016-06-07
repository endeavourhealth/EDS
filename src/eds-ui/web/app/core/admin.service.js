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
        var AdminService = (function (_super) {
            __extends(AdminService, _super);
            function AdminService() {
                _super.apply(this, arguments);
            }
            AdminService.prototype.getMenuOptions = function () {
                return [
                    { caption: 'Dashboard', state: 'app.dashboard', icon: 'fa fa-tachometer' },
                    { caption: 'Library', state: 'app.library', icon: 'fa fa-book' },
                    { caption: 'Reports', state: 'app.reportList', icon: 'fa fa-files-o' },
                    { caption: 'Organisations', state: 'app.organisationSet', icon: 'fa fa-hospital-o' },
                    { caption: 'Queueing', state: 'app.routeGroup', icon: 'fa fa-tasks' },
                    { caption: 'Administration', state: 'app.admin', icon: 'fa fa-users' },
                    { caption: 'Audit', state: 'app.audit', icon: 'fa fa-archive' }
                ];
            };
            AdminService.prototype.setPendingChanges = function () {
                this.pendingChanges = true;
            };
            AdminService.prototype.clearPendingChanges = function () {
                this.pendingChanges = false;
            };
            AdminService.prototype.getPendingChanges = function () {
                return this.pendingChanges;
            };
            AdminService.prototype.getUserList = function () {
                return this.httpGet('/api/admin/getUsers');
            };
            AdminService.prototype.saveUser = function (user) {
                return this.httpPost('/api/admin/saveUser', user);
            };
            return AdminService;
        })(core.BaseHttpService);
        core.AdminService = AdminService;
        angular
            .module('app.core')
            .service('AdminService', AdminService);
    })(core = app.core || (app.core = {}));
})(app || (app = {}));
//# sourceMappingURL=admin.service.js.map
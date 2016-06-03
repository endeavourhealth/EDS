/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/admin.service.ts" />
/// <reference path="../models/MenuOption.ts" />
var app;
(function (app) {
    var layout;
    (function (layout) {
        'use strict';
        var SidebarController = (function () {
            function SidebarController(adminService) {
                this.menuOptions = adminService.getMenuOptions();
            }
            SidebarController.$inject = ['AdminService'];
            return SidebarController;
        })();
        angular.module('app.layout')
            .controller('SidebarController', SidebarController);
    })(layout = app.layout || (app.layout = {}));
})(app || (app = {}));
//# sourceMappingURL=sidebar.controller.js.map
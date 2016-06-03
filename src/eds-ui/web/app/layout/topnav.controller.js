/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/admin.service.ts" />
/// <reference path="../models/Role.ts" />
/// <reference path="../models/User.ts" />
/// <reference path="../models/UserInRole.ts" />
var app;
(function (app) {
    var layout;
    (function (layout) {
        'use strict';
        var TopnavController = (function () {
            function TopnavController(securityService) {
                this.securityService = securityService;
                this.getCurrentUser();
            }
            TopnavController.prototype.getCurrentUser = function () {
                var vm = this;
                vm.currentUser = vm.securityService.getCurrentUser();
                //vm.updateRole(vm.currentUser.currentUserInRoleUuid);
            };
            TopnavController.prototype.updateRole = function (userInRoleUuid) {
                var vm = this;
                var matches = $.grep(vm.currentUser.userInRoles, function (e) {
                    return e.userInRoleUuid === userInRoleUuid;
                });
                if (matches.length === 1) {
                    vm.securityService.switchUserInRole(userInRoleUuid)
                        .then(function (data) {
                        vm.currentUser.currentUserInRoleUuid = userInRoleUuid;
                        vm.selectedRole = matches[0];
                    });
                }
            };
            TopnavController.$inject = ['SecurityService'];
            return TopnavController;
        })();
        angular.module('app.layout')
            .controller('TopnavController', TopnavController);
    })(layout = app.layout || (app.layout = {}));
})(app || (app = {}));
//# sourceMappingURL=topnav.controller.js.map
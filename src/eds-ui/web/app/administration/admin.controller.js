/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../blocks/logger.service.ts" />
var app;
(function (app) {
    var admin;
    (function (admin) {
        var UserEditorController = app.dialogs.UserEditorController;
        'use strict';
        var AdminController = (function () {
            function AdminController(logger, adminService, $modal) {
                this.logger = logger;
                this.adminService = adminService;
                this.$modal = $modal;
                this.userType = 'all';
                this.loadUsers();
            }
            AdminController.prototype.editUser = function (user) {
                var vm = this;
                UserEditorController.open(vm.$modal, user)
                    .result.then(function (editedUser) {
                    vm.adminService.saveUser(editedUser)
                        .then(function (response) {
                        editedUser.uuid = response.uuid;
                        var i = vm.userList.indexOf(user);
                        vm.userList[i] = editedUser;
                        vm.logger.success('User saved', editedUser, 'Edit user');
                    });
                });
            };
            AdminController.prototype.viewUser = function (user) {
                var vm = this;
                UserEditorController.open(vm.$modal, user);
            };
            AdminController.prototype.deleteUser = function (user) {
                this.logger.error('Delete ' + user.username);
            };
            AdminController.prototype.loadUsers = function () {
                var vm = this;
                vm.adminService.getUserList()
                    .then(function (result) {
                    vm.userList = result.users;
                });
            };
            AdminController.$inject = ['LoggerService', 'AdminService', '$uibModal'];
            return AdminController;
        })();
        angular
            .module('app.admin')
            .controller('AdminController', AdminController);
    })(admin = app.admin || (app.admin = {}));
})(app || (app = {}));
//# sourceMappingURL=admin.controller.js.map
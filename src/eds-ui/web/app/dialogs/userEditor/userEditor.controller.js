/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var dialogs;
    (function (dialogs) {
        'use strict';
        var UserEditorController = (function (_super) {
            __extends(UserEditorController, _super);
            function UserEditorController($uibModalInstance, logger, adminService, user) {
                _super.call(this, $uibModalInstance);
                this.$uibModalInstance = $uibModalInstance;
                this.logger = logger;
                this.adminService = adminService;
                this.user = user;
                this.resultData = jQuery.extend(true, {}, user);
            }
            UserEditorController.open = function ($modal, user) {
                var options = {
                    templateUrl: 'app/dialogs/userEditor/userEditor.html',
                    controller: 'UserEditorController',
                    controllerAs: 'userEditor',
                    backdrop: 'static',
                    resolve: {
                        user: function () { return user; }
                    }
                };
                var dialog = $modal.open(options);
                return dialog;
            };
            UserEditorController.$inject = ['$uibModalInstance', 'LoggerService', 'AdminService', 'user'];
            return UserEditorController;
        })(dialogs.BaseDialogController);
        dialogs.UserEditorController = UserEditorController;
        angular
            .module('app.dialogs')
            .controller('UserEditorController', UserEditorController);
    })(dialogs = app.dialogs || (app.dialogs = {}));
})(app || (app = {}));
//# sourceMappingURL=userEditor.controller.js.map
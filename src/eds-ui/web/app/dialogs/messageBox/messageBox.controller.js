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
        var MessageBoxController = (function (_super) {
            __extends(MessageBoxController, _super);
            function MessageBoxController($uibModalInstance, title, message, okText, cancelText) {
                _super.call(this, $uibModalInstance);
                this.$uibModalInstance = $uibModalInstance;
                this.title = title;
                this.message = message;
                this.okText = okText;
                this.cancelText = cancelText;
            }
            MessageBoxController.open = function ($modal, title, message, okText, cancelText) {
                var options = {
                    templateUrl: 'app/dialogs/messageBox/messageBox.html',
                    controller: 'MessageBoxController',
                    controllerAs: 'ctrl',
                    backdrop: 'static',
                    resolve: {
                        title: function () { return title; },
                        message: function () { return message; },
                        okText: function () { return okText; },
                        cancelText: function () { return cancelText; }
                    }
                };
                var dialog = $modal.open(options);
                return dialog;
            };
            MessageBoxController.$inject = ['$uibModalInstance', 'title', 'message', 'okText', 'cancelText'];
            return MessageBoxController;
        })(dialogs.BaseDialogController);
        dialogs.MessageBoxController = MessageBoxController;
        angular
            .module('app.dialogs')
            .controller('MessageBoxController', MessageBoxController);
    })(dialogs = app.dialogs || (app.dialogs = {}));
})(app || (app = {}));
//# sourceMappingURL=messageBox.controller.js.map
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
        var InputBoxController = (function (_super) {
            __extends(InputBoxController, _super);
            function InputBoxController($uibModalInstance, title, message, value) {
                _super.call(this, $uibModalInstance);
                this.$uibModalInstance = $uibModalInstance;
                this.title = title;
                this.message = message;
                this.resultData = value;
            }
            InputBoxController.open = function ($modal, title, message, value) {
                var options = {
                    templateUrl: 'app/dialogs/inputBox/inputBox.html',
                    controller: 'InputBoxController',
                    controllerAs: 'ctrl',
                    backdrop: 'static',
                    resolve: {
                        title: function () { return title; },
                        message: function () { return message; },
                        value: function () { return value; }
                    }
                };
                var dialog = $modal.open(options);
                return dialog;
            };
            InputBoxController.$inject = ['$uibModalInstance', 'title', 'message', 'value'];
            return InputBoxController;
        })(dialogs.BaseDialogController);
        dialogs.InputBoxController = InputBoxController;
        angular
            .module('app.dialogs')
            .controller('InputBoxController', InputBoxController);
    })(dialogs = app.dialogs || (app.dialogs = {}));
})(app || (app = {}));
//# sourceMappingURL=inputBox.controller.js.map
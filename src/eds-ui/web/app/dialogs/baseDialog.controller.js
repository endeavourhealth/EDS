/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var dialogs;
    (function (dialogs) {
        'use strict';
        var BaseDialogController = (function () {
            function BaseDialogController($uibModalInstance) {
                this.$uibModalInstance = $uibModalInstance;
            }
            BaseDialogController.prototype.ok = function () {
                this.$uibModalInstance.close(this.resultData);
                console.log('OK Pressed');
            };
            BaseDialogController.prototype.cancel = function () {
                this.$uibModalInstance.dismiss('cancel');
                console.log('Cancel Pressed');
            };
            return BaseDialogController;
        })();
        dialogs.BaseDialogController = BaseDialogController;
    })(dialogs = app.dialogs || (app.dialogs = {}));
})(app || (app = {}));
//# sourceMappingURL=baseDialog.controller.js.map
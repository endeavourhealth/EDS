/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var routeGroup;
    (function (routeGroup_1) {
        var BaseDialogController = app.dialogs.BaseDialogController;
        'use strict';
        var RouteGroupEditorController = (function (_super) {
            __extends(RouteGroupEditorController, _super);
            function RouteGroupEditorController($uibModalInstance, logger, adminService, routeGroup) {
                _super.call(this, $uibModalInstance);
                this.$uibModalInstance = $uibModalInstance;
                this.logger = logger;
                this.adminService = adminService;
                this.routeGroup = routeGroup;
                this.resultData = jQuery.extend(true, {}, routeGroup);
            }
            RouteGroupEditorController.open = function ($modal, routeGroup) {
                var options = {
                    templateUrl: 'app/routeGroup/editor/routeGroupEditor.html',
                    controller: 'RouteGroupEditorController',
                    controllerAs: 'ctrl',
                    backdrop: 'static',
                    resolve: {
                        routeGroup: function () { return routeGroup; }
                    }
                };
                var dialog = $modal.open(options);
                return dialog;
            };
            RouteGroupEditorController.prototype.addFilter = function (filter) {
                this.resultData.regex += filter;
            };
            RouteGroupEditorController.$inject = ['$uibModalInstance', 'LoggerService', 'AdminService', 'routeGroup'];
            return RouteGroupEditorController;
        })(BaseDialogController);
        routeGroup_1.RouteGroupEditorController = RouteGroupEditorController;
        angular
            .module('app.routeGroup')
            .controller('RouteGroupEditorController', RouteGroupEditorController);
    })(routeGroup = app.routeGroup || (app.routeGroup = {}));
})(app || (app = {}));
//# sourceMappingURL=routeGroupEditor.controller.js.map
/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../models/MenuOption.ts" />
/// <reference path="../models/Role.ts" />
/// <reference path="../models/User.ts" />
/// <reference path="../models/UserInRole.ts" />
var app;
(function (app) {
    var core;
    (function (core) {
        'use strict';
        var ModuleStateService = (function () {
            function ModuleStateService() {
                this.states = {};
            }
            ModuleStateService.prototype.setState = function (moduleName, state) {
                this.states[moduleName] = state;
            };
            ModuleStateService.prototype.getState = function (moduleName) {
                return this.states[moduleName];
            };
            return ModuleStateService;
        })();
        core.ModuleStateService = ModuleStateService;
        angular
            .module('app.core')
            .service('ModuleStateService', ModuleStateService);
    })(core = app.core || (app.core = {}));
})(app || (app = {}));
//# sourceMappingURL=moduleState.service.js.map
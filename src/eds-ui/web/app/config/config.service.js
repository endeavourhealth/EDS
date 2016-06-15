/// <reference path="../../typings/tsd.d.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var config;
    (function (config) {
        'use strict';
        var ConfigService = (function (_super) {
            __extends(ConfigService, _super);
            function ConfigService() {
                _super.apply(this, arguments);
            }
            ConfigService.prototype.getConfig = function (configurationId) {
                var request = {
                    params: {
                        'configurationId': configurationId
                    }
                };
                return this.httpGet('api/config/getConfig', request);
            };
            ConfigService.prototype.saveConfig = function (configResource) {
                return this.httpPost('api/config/saveConfig', configResource);
            };
            return ConfigService;
        })(app.core.BaseHttpService);
        config.ConfigService = ConfigService;
        angular
            .module('app.config')
            .service('ConfigService', ConfigService);
    })(config = app.config || (app.config = {}));
})(app || (app = {}));
//# sourceMappingURL=config.service.js.map
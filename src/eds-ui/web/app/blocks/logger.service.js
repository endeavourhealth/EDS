/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var blocks;
    (function (blocks) {
        'use strict';
        var LoggerService = (function () {
            function LoggerService($log) {
                this.$log = $log;
                toastr.options.timeOut = 4000;
                toastr.options.positionClass = 'toast-bottom-right';
            }
            // straight to console; bypass toastr
            LoggerService.prototype.log = function () {
                var args = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    args[_i - 0] = arguments[_i];
                }
                this.$log.log(args);
            };
            LoggerService.prototype.error = function (message, data, title) {
                toastr.error(message, title);
                this.$log.error('Error: ' + message, '\nSummary:', title, '\nDetails:', data);
            };
            LoggerService.prototype.info = function (message, data, title) {
                toastr.info(message, title);
                this.$log.info('Info: ' + message, '\nSummary:', title, '\nDetails:', data);
            };
            LoggerService.prototype.success = function (message, data, title) {
                toastr.success(message, title);
                this.$log.info('Success: ' + message, '\nSummary:', title, '\nDetails:', data);
            };
            LoggerService.prototype.warning = function (message, data, title) {
                toastr.warning(message, title);
                this.$log.warn('Warning: ' + message, '\nSummary:', title, '\nDetails:', data);
            };
            LoggerService.$inject = ['$log'];
            return LoggerService;
        })();
        blocks.LoggerService = LoggerService;
        angular
            .module('app.blocks')
            .service('LoggerService', LoggerService);
    })(blocks = app.blocks || (app.blocks = {}));
})(app || (app = {}));
//# sourceMappingURL=logger.service.js.map
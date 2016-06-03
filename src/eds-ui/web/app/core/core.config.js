/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var core;
    (function (core) {
        'use strict';
        var Config = (function () {
            function Config($httpProvider, IdleProvider, KeepaliveProvider) {
                $httpProvider.defaults.headers.post['Accept'] = 'application/json';
                $httpProvider.defaults.headers.post['Content-Type'] = 'application/json; charset=utf-8';
                $httpProvider.defaults.withCredentials = true;
                toastr.options.timeOut = 4000;
                toastr.options.positionClass = 'toast-bottom-right';
                IdleProvider.idle(300);
                IdleProvider.timeout(10);
                KeepaliveProvider.interval(10);
            }
            Config.$inject = ['$httpProvider', 'IdleProvider', 'KeepaliveProvider'];
            return Config;
        })();
        angular
            .module('app.core')
            .config(Config);
    })(core = app.core || (app.core = {}));
})(app || (app = {}));
//# sourceMappingURL=core.config.js.map
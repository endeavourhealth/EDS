/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../blocks/logger.service.ts" />
var app;
(function (app) {
    var login;
    (function (login) {
        'use strict';
        var LoginController = (function () {
            function LoginController(logger, Idle, $state, securityService, username) {
                this.logger = logger;
                this.Idle = Idle;
                this.$state = $state;
                this.securityService = securityService;
                this.username = username;
                Idle.unwatch();
                securityService.logout();
            }
            LoginController.prototype.login = function (scope) {
                var vm = this;
                vm.securityService.login(vm.username, vm.password)
                    .then(function (response) {
                    vm.logger.success('User logged in', vm.username, 'Logged In');
                    vm.Idle.watch();
                    if (scope.$close) {
                        scope.$close();
                    }
                    else {
                        vm.$state.transitionTo('app.dashboard');
                    }
                })
                    .catch(function (data) {
                    vm.logger.error(data.statusText, data, 'Login error!');
                });
            };
            LoginController.$inject = ['LoggerService', 'Idle', '$state', 'SecurityService', 'userName'];
            return LoginController;
        })();
        login.LoginController = LoginController;
        angular
            .module('app.login')
            .controller('LoginController', LoginController);
    })(login = app.login || (app.login = {}));
})(app || (app = {}));
//# sourceMappingURL=login.controller.js.map
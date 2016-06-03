/// <reference path="../../typings/tsd.d.ts" />
var app;
(function (app) {
    var layout;
    (function (layout) {
        var ShellController = (function () {
            function ShellController($scope, $modal, $modalStack, $state, securityService) {
                var vm = this;
                function closeModals() {
                    if (vm.warning) {
                        vm.warning.close();
                        vm.warning = null;
                    }
                    if (vm.timedout) {
                        vm.timedout.close();
                        vm.timedout = null;
                    }
                }
                $scope.$on('IdleStart', function () {
                    closeModals();
                    vm.warning = $modal.open({
                        templateUrl: 'warning-dialog.html',
                        windowClass: 'modal-danger'
                    });
                });
                $scope.$on('IdleEnd', function () {
                    closeModals();
                });
                $scope.$on('IdleTimeout', function () {
                    closeModals();
                    $modalStack.dismissAll();
                    var userName = securityService.getCurrentUser().username;
                    var options = {
                        templateUrl: 'app/login/loginModal.html',
                        controller: 'LoginController',
                        controllerAs: 'login',
                        backdrop: 'static',
                        resolve: {
                            userName: function () { return userName; }
                        }
                    };
                    $modal.open(options);
                });
            }
            ShellController.$inject = ['$scope', '$uibModal', '$uibModalStack', '$state', 'SecurityService'];
            return ShellController;
        })();
        angular.module('app.layout')
            .controller('ShellController', ShellController);
    })(layout = app.layout || (app.layout = {}));
})(app || (app = {}));
//# sourceMappingURL=shell.controller.js.map
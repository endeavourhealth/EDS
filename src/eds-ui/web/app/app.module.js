/// <reference path="../typings/tsd.d.ts" />
angular.module('app', [
    'ui.bootstrap',
    'ngIdle',
    'ui.tree',
    'ngDragDrop',
    'angular-uuid-generator',
    'app.core',
    'app.config',
    'app.blocks',
    'app.models',
    'app.layout',
    'app.login',
    'app.dialogs',
    'app.dashboard',
    'app.library',
    'app.protocol',
    'app.reports',
    'app.listOutput',
    'app.codeSet',
    'app.organisation',
    'app.service',
    'app.organisationSet',
    'app.admin',
    'app.query',
    'app.routeGroup',
    'flowChart',
    'dragging',
    'mouseCapture'
])
    .run(['$state', '$rootScope', 'AdminService', 'SecurityService', 'LoggerService', '$uibModal',
    function ($state, $rootScope, adminService, securityService, logger, $modal) {
        $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
            if (toState.unsecured !== true && !securityService.isAuthenticated()) {
                logger.error('You are not logged in');
                event.preventDefault();
                $state.transitionTo('login');
            }
            if (adminService.getPendingChanges()) {
                event.preventDefault();
                var options = {
                    templateUrl: 'app/dialogs/messageBox/messageBox.html',
                    controller: 'MessageBoxController',
                    controllerAs: 'ctrl',
                    backdrop: 'static',
                    resolve: {
                        title: function () { return 'Unsaved changes'; },
                        message: function () { return 'There are unsaved changes, do you wish to continue'; },
                        okText: function () { return 'Yes'; },
                        cancelText: function () { return 'No'; }
                    }
                };
                $modal.open(options)
                    .result
                    .then(function () {
                    adminService.clearPendingChanges();
                    $state.transitionTo(toState);
                });
            }
        });
        $state.go('login', {}, { reload: true });
    }]);
//# sourceMappingURL=app.module.js.map
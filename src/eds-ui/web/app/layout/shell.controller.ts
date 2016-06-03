/// <reference path="../../typings/tsd.d.ts" />

module app.layout {
	import IRootScopeService = angular.IRootScopeService;
	import IStateService = angular.ui.IStateService;
	import IModalService = angular.ui.bootstrap.IModalService;
	import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
	import IModalStackService = angular.ui.bootstrap.IModalStackService;
	import IAdminService = app.core.IAdminService;

	class ShellController {
		warning : IModalServiceInstance;
		timedout : IModalServiceInstance;

		static $inject = ['$scope', '$uibModal', '$uibModalStack', '$state', 'SecurityService'];

		constructor($scope : IRootScopeService,
								$modal : IModalService,
								$modalStack : IModalStackService,
								$state : IStateService,
								securityService : ISecurityService) {
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
					templateUrl:'app/login/loginModal.html',
					controller:'LoginController',
					controllerAs:'login',
					backdrop:'static',
					resolve: {
						userName: () => userName
					}
				};

				$modal.open(options);
			});
		}
	}

	angular.module('app.layout')
		.controller('ShellController', ShellController);
}

/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/admin.service.ts" />
/// <reference path="../models/Role.ts" />
/// <reference path="../models/User.ts" />
/// <reference path="../models/UserInRole.ts" />

module app.layout {
	'use strict';

	class TopnavController {
		currentUser:app.models.User;
		selectedRole:app.models.UserInRole;

		static $inject = ['SecurityService'];

		constructor(private securityService:ISecurityService) {
			this.getCurrentUser();
		}

		getCurrentUser() {
			var vm:TopnavController = this;
			vm.currentUser = vm.securityService.getCurrentUser();
			//vm.updateRole(vm.currentUser.currentUserInRoleUuid);
		}

		updateRole(userInRoleUuid : string) {
			var vm = this;
			var matches = $.grep(vm.currentUser.userInRoles, function (e) {
				return e.userInRoleUuid === userInRoleUuid;
			});
			if (matches.length === 1) {
				vm.securityService.switchUserInRole(userInRoleUuid)
					.then(function(data) {
						vm.currentUser.currentUserInRoleUuid = userInRoleUuid;
						vm.selectedRole = matches[0];
					});
			}
		}
	}

	angular.module('app.layout')
		.controller('TopnavController', TopnavController);
}
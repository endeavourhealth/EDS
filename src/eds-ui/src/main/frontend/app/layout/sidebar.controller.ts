/// <reference path="../../typings/index.d.ts" />
/// <reference path="../core/admin.service.ts" />
/// <reference path="../models/MenuOption.ts" />

module app.layout {
	'use strict';

	class SidebarController {
		menuOptions:app.models.MenuOption[];

		static $inject = ['AdminService', 'SecurityService'];

		constructor(protected adminService:app.core.IAdminService, protected securityService:ISecurityService) {
			this.menuOptions = adminService.getMenuOptions();
		}

		logout() {
			this.securityService.logout();
		}
	}

	angular.module('app.layout')
		.controller('SidebarController', SidebarController);
}
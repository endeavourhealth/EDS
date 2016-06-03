/// <reference path="../../typings/tsd.d.ts" />
/// <reference path="../core/admin.service.ts" />
/// <reference path="../models/MenuOption.ts" />

module app.layout {
	'use strict';

	class SidebarController {
		menuOptions:app.models.MenuOption[];

		static $inject = ['AdminService'];

		constructor(adminService:app.core.IAdminService) {
			this.menuOptions = adminService.getMenuOptions();
		}
	}

	angular.module('app.layout')
		.controller('SidebarController', SidebarController);
}
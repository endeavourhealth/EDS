import {MenuOption} from "../models/MenuOption";
import {SecurityService} from "../core/security.service";
import {AdminService} from "../core/admin.service";

export class SidebarComponent implements ng.IComponentOptions {
	template : string;
	controller : string;
	controllerAs : string;

	constructor () {
		this.template = require('./sidebar.html');
		this.controller = 'SidebarController';
		this.controllerAs = '$ctrl';
	}
}

export class SidebarController {
	menuOptions:MenuOption[];

	static $inject = ['AdminService', 'SecurityService'];

	constructor(adminService:AdminService, private securityService:SecurityService) {
		this.menuOptions = adminService.getMenuOptions();
	}

	logout() {
		this.securityService.logout();
	}
}

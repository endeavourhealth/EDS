import {IAdminService} from "../core/admin.service";
import {MenuOption} from "../models/MenuOption";
import {ISecurityService} from "../core/security.service";

export class SidebarController {
	menuOptions:MenuOption[];

	static $inject = ['AdminService', 'SecurityService'];

	constructor(adminService:IAdminService, private securityService:ISecurityService) {
		this.menuOptions = adminService.getMenuOptions();
	}

	logout() {
		this.securityService.logout();
	}
}

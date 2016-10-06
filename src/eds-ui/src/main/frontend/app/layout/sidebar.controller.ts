import {ISecurityService} from "../core/security.service";
import {IAdminService} from "../core/admin.service";

export class SidebarController {
	menuOptions:app.models.MenuOption[];

	static $inject = ['AdminService', 'SecurityService'];

	constructor(protected adminService:IAdminService, protected securityService:ISecurityService) {
		this.menuOptions = adminService.getMenuOptions();
	}

	logout() {
		this.securityService.logout();
	}
}

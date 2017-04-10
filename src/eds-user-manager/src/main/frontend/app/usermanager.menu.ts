import {Injectable} from "@angular/core";
import {MenuService} from "eds-common-js";
import {MenuOption} from "eds-common-js/dist/layout/models/MenuOption";
import {SecurityService} from "eds-common-js";
import {User} from "eds-common-js/dist/security/models/User";

@Injectable()
export class UserManagerMenuService implements  MenuService {
	currentUser:User;

	getApplicationTitle(): string {
		return 'Discovery Authentication Service';
	}

	constructor(private securityService:SecurityService) {
		let vm = this;
		vm.currentUser = vm.securityService.getCurrentUser();
	}

	getMenuOptions():MenuOption[] {
		switch (this.currentUser.isSuperUser) {
			case true:
				return [
					//options disabled in V1 - enabled for role eds_superuser (only settable via keycloak UI)
					{caption: 'User Manager', state: 'app.userManager', icon: 'fa fa-users'},
					{caption: 'Role Manager', state: 'app.roleManager', icon: 'fa fa-tasks'},

					{caption: 'Client Manager', state: 'app.clientManager', icon: 'fa fa-laptop'},
					//{caption: 'Management', state: 'app.managerList', icon: 'fa fa-user'},
				];
			case false:
				return [];
			default:
				return [];
		}
	}
}
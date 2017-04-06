import {Injectable} from "@angular/core";
import {MenuService} from "eds-common-js";
import {MenuOption} from "eds-common-js/dist/layout/models/MenuOption";

@Injectable()
export class UserManagerMenuService implements  MenuService {
	getApplicationTitle(): string {
		return 'Discovery Authentication Service';
	}
	getMenuOptions():MenuOption[] {
		return [
			//options disabled in V1 - enabled for role eds_superuser (only settable via keycloak UI)
			{caption: 'User Manager', state: 'app.userManager', icon: 'fa fa-users'},
			{caption: 'Role Manager', state: 'app.roleManager', icon: 'fa fa-tasks'},

			//{caption: 'Client Manager', state: 'app.appManager', icon: 'fa fa-laptop'},
			//{caption: 'Management', state: 'app.managerList', icon: 'fa fa-user'},
		];
	}
}
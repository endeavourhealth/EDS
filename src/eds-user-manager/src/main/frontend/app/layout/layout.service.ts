import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {MenuOption} from "./models/MenuOption";
import {Observable} from "rxjs";
import {SecurityService} from "../security/security.service";
import {User} from "../security/models/User";

@Injectable()
export class LayoutService extends BaseHttp2Service {
	currentUser: User;

	constructor(http : Http, private securityService:SecurityService) {
		super(http);

		let vm = this;
		vm.getCurrentUser();
	}

	getMenuOptions():MenuOption[] {
		if (this.currentUser.isSuperUser) {
			return [
				//options disabled in V1 - enabled for role eds_superuser (only settable via keycloak UI)
				{caption: 'User Manager', state: 'app.userManager', icon: 'fa fa-users'},
				{caption: 'Role Manager', state: 'app.roleManager', icon: 'fa fa-tasks'},

				//{caption: 'Client Manager', state: 'app.appManager', icon: 'fa fa-laptop'},
				//{caption: 'Management', state: 'app.managerList', icon: 'fa fa-user'},
			];
		} else {
			return [];
		}
	}

	getCurrentUser() {
		this.currentUser = this.securityService.getCurrentUser();
	}



}
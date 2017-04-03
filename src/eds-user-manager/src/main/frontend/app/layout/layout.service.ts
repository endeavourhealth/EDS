import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {MenuOption} from "./models/MenuOption";
import {Observable} from "rxjs";

@Injectable()
export class LayoutService extends BaseHttp2Service {
	constructor(http : Http) { super(http); }

	getMenuOptions():MenuOption[] {
		return [
			//options disabled in V1
			//{caption: 'User Manager', state: 'app.userManager', icon: 'fa fa-users'},
			//{caption: 'Role Manager', state: 'app.roleManager', icon: 'fa fa-tasks'},

			//{caption: 'Client Manager', state: 'app.appManager', icon: 'fa fa-laptop'},
			//{caption: 'Management', state: 'app.managerList', icon: 'fa fa-user'},
		];
	}
}
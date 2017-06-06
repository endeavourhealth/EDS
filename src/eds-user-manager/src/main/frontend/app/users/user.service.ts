import {Injectable} from "@angular/core";
import {User} from "./models/User";
import {Observable} from "rxjs/Observable";
import {Http, Response, URLSearchParams} from "@angular/http";
import {BaseHttp2Service} from "eds-common-js";
import {UserRole} from "./models/UserRole";
import {Client} from "./models/Client";
import {Group} from "./models/Group";

@Injectable()
export class UserService extends BaseHttp2Service {
	constructor(http: Http) {
		super(http);
	}

	getUsers(): Observable<User[]> {
		return this.httpGet('api/usermanager/users');
	}

	getUsersSearch(searchData: string): Observable<User[]> {
		let params = new URLSearchParams();
		params.set('searchData', searchData);
		return this.httpGet('api/usermanager/users', {search: params});
	}

	getUser(userId: string): Observable<User> {
		let params = new URLSearchParams();
		params.set('userId', userId);
		return this.httpGet('api/usermanager/users/user', {search: params});
	}

	getAvailableRealmRoles(userId: string): Observable<UserRole[]> {
		let params = new URLSearchParams();
		params.set('userId', userId);
		return this.httpGet('api/usermanager/users/roles/realm/available', {search: params});
	}

	getRealmRoles(): Observable<UserRole[]> {
		return this.httpGet('api/usermanager/roles/realm');
	}

	getRealmClients(): Observable<Client[]> {
		return this.httpGet('api/usermanager/users/clients/realm');
	}

	getAssignedRoles(userId: string): Observable<UserRole[]> {
		let params = new URLSearchParams();
		params.set('userId', userId);
		return this.httpGet('api/usermanager/users/roles/realm/assigned', {search: params});
	}

	saveUser(editedUser: User, editMode: Boolean): Observable<User> {
		let params = new URLSearchParams();
		params.set('editMode', editMode == true ? "1":"0");
		return this.httpPost('api/usermanager/users/save', editedUser, {search: params});
	}

	deleteUser(userId: string) {
		let params = new URLSearchParams();
		params.set('userId', userId);
		return this.httpDelete ('api/usermanager/users/delete', {search: params});
	}

	saveRole(editedRole: UserRole, editMode: Boolean): Observable<UserRole> {
		let params = new URLSearchParams();
		params.set('editMode', editMode == true ? "1":"0");
		return this.httpPost('api/usermanager/users/roles/save', editedRole, {search: params});
	}

	deleteRole(roleName: string) {
		let params = new URLSearchParams();
		params.set('roleName', roleName);
		return this.httpDelete ('api/usermanager/users/role/delete', {search: params});
	}

	getGroups(): Observable<Group[]> {
		return this.httpGet('api/usermanager/groups');
	}

	getUserGroups(userId: string): Observable<Group[]> {
		let params = new URLSearchParams();
		params.set('userId', userId);
		return this.httpGet('api/usermanager/users/user/groups', {search: params});
	}

	createGroup(): Observable<any> {
		return this.httpPost('api/usermanager/groups/save');
	}
}
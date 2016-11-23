import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {UserList} from "../models/UserList";
import {User} from "../models/User";

@Injectable()
export class AdminService extends BaseHttp2Service{
	constructor(http : Http) { super (http); }

	pendingChanges : boolean;

	setPendingChanges() : void {
		this.pendingChanges = true;
	}

	clearPendingChanges() : void {
		this.pendingChanges = false;
	}

	getPendingChanges() : boolean {
		return this.pendingChanges;
	}


	getUserList() : Observable<UserList> {
		return this.httpGet('/api/admin/getUsers');
	}

	saveUser(user : User) : Observable<{uuid : string}> {
		return this.httpPost('/api/admin/saveUser', user);
	}
}
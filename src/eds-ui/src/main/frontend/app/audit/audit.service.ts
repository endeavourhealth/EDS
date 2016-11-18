import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "../core/baseHttp2.service";

import {AuditEvent} from "../models/AuditEvent";
import {User} from "../models/User";

@Injectable()
export class AuditService extends BaseHttp2Service {
	constructor(http: Http) { super(http); }

	getUsers():Observable<User[]> {
		return this.httpGet('api/audit/users');
	}

	getUserAudit(module : string, userId : string, month : Date, organisationId : string):Observable<AuditEvent[]> {
		let params = new URLSearchParams();
		params.set('module',module);
		params.set('userId',userId);
		params.set('month',month.valueOf().toString());
		params.set('organisationId',organisationId);

		return this.httpGet('api/audit', {search : params});
	}

	getModules():Observable<string[]> {
		return this.httpGet('api/audit/modules');
	}

	getSubmodules(module : string):Observable<string[]> {
		let params = new URLSearchParams();
		params.set('module',module);

		return this.httpGet('api/audit/submodules', {search : params});
	}

	getActions():Observable<string[]> {
		return this.httpGet('api/audit/actions');
	}
}

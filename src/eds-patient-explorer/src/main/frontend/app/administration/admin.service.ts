import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import {BaseHttp2Service} from "../core/baseHttp2.service";

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
}
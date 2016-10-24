import {BaseHttp2Service} from "../core/baseHttp2.service";
import {Http} from "@angular/http";

export class AuditService extends BaseHttp2Service {
	constructor(http : Http) { super (http); }
}

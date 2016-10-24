import {BaseHttp2Service} from "../core/baseHttp2.service";
import {Http} from "@angular/http";
import {Injectable} from "@angular/core";

@Injectable()
export class AuditService extends BaseHttp2Service {
	constructor(http : Http) { super (http); }
}

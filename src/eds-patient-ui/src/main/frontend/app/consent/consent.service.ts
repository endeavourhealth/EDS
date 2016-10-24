import {Http} from "@angular/http";
import {BaseHttp2Service} from "../core/baseHttp2.service";

export class ConsentService extends BaseHttp2Service {
	constructor(http : Http) { super (http); }
}

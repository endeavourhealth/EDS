import {Http} from "@angular/http";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {Injectable} from "@angular/core";

@Injectable()
export class ConsentService extends BaseHttp2Service {
	constructor(http : Http) { super (http); }
}

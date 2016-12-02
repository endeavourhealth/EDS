import {Injectable} from "@angular/core";
import {Http} from "@angular/http";
import {System} from "./models/System";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "../core/baseHttp2.service";

@Injectable()
export class SystemService extends BaseHttp2Service {
	constructor(http : Http) { super(http); }

	getSystems(): Observable<System[]> {
		return this.httpGet('api/library/getSystems');
	}
}
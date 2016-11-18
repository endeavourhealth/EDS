import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {Observable} from "rxjs";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {LoggingEvent} from "../models/LoggingEvent";

@Injectable()
export class LoggingService extends BaseHttp2Service {
	constructor(http: Http) { super(http); }

	getLoggingEvents(serviceId : string, level : string, page : number):Observable<LoggingEvent[]> {
		let params = new URLSearchParams();
		params.set('serviceId',serviceId);
		params.set('level',level);
		params.set('page',page.toString());

		return this.httpGet('api/logging/getLoggingEvents', {search : params});
	}

	getStackTrace(eventId : string):Observable<string> {
		let params = new URLSearchParams();
		params.set('eventId',eventId);

		return this.httpGet('api/logging/getStackTrace', {search : params});
	}
}

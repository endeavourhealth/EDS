import {Injectable} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {BaseHttp2Service} from "../core/baseHttp2.service";
import {MenuOption} from "./models/MenuOption";
import {Observable} from "rxjs";

@Injectable()
export class LayoutService extends BaseHttp2Service {
	constructor(http : Http) { super(http); }

	getMenuOptions():MenuOption[] {
		return [
			{caption: 'Patient explorer', state: 'app.recordViewer', icon: 'fa fa-heart'},
			// {caption: 'Patients', state: 'app.patientIdentity', icon: 'fa fa-user'},
			// {caption: 'Resources', state: 'app.resourceList', icon: 'fa fa-fire'},
			{caption: 'Standard reports', state: 'app.countReports', icon: 'fa fa-balance-scale'},
			{caption: 'SQL editor', state: 'app.sqlEditor', icon: 'fa fa-database'}
		];
	}

	getServiceName(uuid : string) : Observable<string> {
		let params = new URLSearchParams();
		params.set('serviceId', uuid);
		return this.httpGet('api/recordViewer/getServiceName', {search: params});
	}
}
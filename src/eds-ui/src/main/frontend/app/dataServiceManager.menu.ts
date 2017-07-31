import {Injectable} from "@angular/core";
import {MenuService} from "eds-common-js";
import {MenuOption} from "eds-common-js/dist/layout/models/MenuOption";

@Injectable()
export class DataServiceManagerMenuService implements  MenuService {
	getApplicationTitle(): string {
		return 'Data Service Manager';
	}
	getMenuOptions():MenuOption[] {
		return [
			{caption: 'Dashboard', state: 'app.dashboard', icon: 'fa fa-tachometer'},
			{caption: 'Protocols', state: 'app.library', icon: 'fa fa-share-alt'},
			{caption: 'Organisations', state: 'app.organisationList', icon: 'fa fa-hospital-o'},
			{caption: 'Services', state: 'app.serviceList', icon: 'fa fa-building-o'},
			{caption: 'Queueing', state: 'app.queueing', icon: 'fa fa-tasks'},
			//
			{caption: 'Monitoring', state: 'app.monitoring', icon: 'fa fa-list-alt'},
			{caption: 'Transform Errors', state: 'app.transformErrors', icon: 'fa fa-exchange'},
			{caption: 'Statistics', state: 'app.stats', icon: 'fa fa-line-chart'},
			{caption: 'Audit', state: 'app.audit', icon: 'fa fa-list-ul'}
			// {caption: 'Admin', state: 'app.admin', icon: 'fa fa-user'}
		];
	}
}
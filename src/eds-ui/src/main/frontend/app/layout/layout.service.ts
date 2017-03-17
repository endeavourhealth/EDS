import {Injectable} from "@angular/core";
import {MenuOption} from "./models/MenuOption";

@Injectable()
export class LayoutService {
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
			{caption: 'Audit', state: 'app.audit', icon: 'fa fa-list-ul'},
			{caption: 'Organisation Manager', state: 'app.organisationManager', icon: 'fa fa-hospital-o'},
			{caption: 'Regions', state: 'app.region', icon: 'fa fa-globe'},
			{caption: 'Cohorts', state: 'app.cohort', icon: 'fa fa-user-circle'},
			{caption: 'Data Flows', state: 'app.dataFlow', icon: 'fa fa-rocket'},
			// {caption: 'Admin', state: 'app.admin', icon: 'fa fa-user'}
		];
	}
}
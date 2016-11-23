import {Injectable} from "@angular/core";
import {MenuOption} from "../models/MenuOption";

@Injectable()
export class LayoutService {
	getMenuOptions():MenuOption[] {
		return [
			{caption: 'Dashboard', state: 'dashboard', icon: 'fa fa-tachometer'},
			{caption: 'Protocols', state: 'library', icon: 'fa fa-share-alt'},
			{caption: 'Organisations', state: 'organisationList', icon: 'fa fa-hospital-o'},
			{caption: 'Services', state: 'serviceList', icon: 'fa fa-building-o'},
			{caption: 'Queueing', state: 'queueing', icon: 'fa fa-tasks'},
			//
			{caption: 'Patients', state: 'patientIdentity', icon: 'fa fa-user'},
			{caption: 'Record Viewer', state: 'recordViewer', icon: 'fa fa-heart'},
			{caption: 'Resources', state: 'resourceList', icon: 'fa fa-fire'},
			//
			{caption: 'Monitoring', state: 'monitoring', icon: 'fa fa-list-alt'},
			{caption: 'Transform Errors', state: 'transformErrors', icon: 'fa fa-exchange'},
			{caption: 'Statistics', state: 'stats', icon: 'fa fa-line-chart'},
			{caption: 'Audit', state: 'audit', icon: 'fa fa-list-ul'}
			// {caption: 'Admin', state: 'admin', icon: 'fa fa-user'}
		];
	}
}
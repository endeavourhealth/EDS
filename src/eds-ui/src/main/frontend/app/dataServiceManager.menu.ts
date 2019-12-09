import {Injectable} from "@angular/core";
import {MenuService} from "eds-common-js";
import {MenuOption} from "eds-common-js/dist/layout/models/MenuOption";

@Injectable()
export class DataServiceManagerMenuService implements  MenuService {
	getApplicationTitle(): string {
		return 'Data Service Manager';
	}

    getClientId(): string {
        return 'eds-ui';
    }

	getMenuOptions():MenuOption[] {
		return [
			{caption: 'Dashboard', state: 'app.dashboard', icon: 'fa fa-tachometer', role: 'eds-ui:dashboard'},
			{caption: 'Protocols', state: 'app.library', icon: 'fa fa-share-alt', role: 'eds-ui:protocols'},
			{caption: 'Organisations', state: 'app.organisationList', icon: 'fa fa-hospital-o', role: 'eds-ui:organisations'},
			{caption: 'Services', state: 'app.serviceList', icon: 'fa fa-building-o', role: 'eds-ui:services'},
			{caption: 'Queueing', state: 'app.queueing', icon: 'fa fa-map-signs', role: 'eds-ui:queueing'},
			//
			{caption: 'Queue Readers', state: 'app.queueReaderStatus', icon: 'fa fa-tachometer', role: 'eds-ui:monitoring'},
			{caption: 'Frailty API', state: 'app.frailtyApi', icon: 'fa fa-flask', role: 'eds-ui:monitoring'},
			{caption: 'HL7 Receiver', state: 'app.hl7Receiver', icon: 'fa fa-fire', role: 'eds-ui:monitoring'},
			{caption: 'SFTP Reader', state: 'app.sftpReader', icon: 'fa fa-table', role: 'eds-ui:monitoring'},
			{caption: 'Transform Errors', state: 'app.transformErrors', icon: 'fa fa-exchange', role: 'eds-ui:transform-errors'},
			{caption: 'DB Stats', state: 'app.databaseStats', icon: 'fa fa-database', role: 'eds-ui:statistics'},
			{caption: 'Statistics', state: 'app.stats', icon: 'fa fa-line-chart', role: 'eds-ui:statistics'},
			{caption: 'Audit', state: 'app.audit', icon: 'fa fa-list-ul', role: 'eds-ui:audit'},
			{caption: 'Monitoring', state: 'app.monitoring', icon: 'fa fa-list-alt', role: 'eds-ui:monitoring'},
            {caption: 'Remote Filing', state: 'app.remoteFiling', icon: 'fa fa-external-link-square', role: 'eds-ui:audit'}
		];
	}
}
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

		var o;
		var ret = [] as MenuOption[];

		o = {caption: 'Publishers', state: 'app.serviceList', icon: 'fa  fa-toggle-up', role: 'eds-ui:services'} as MenuOption;
		ret.push(o);

		o = {caption: 'Publisher Errors', state: 'app.transformErrors', icon: 'fa fa-exchange', role: 'eds-ui:transform-errors'};
		ret.push(o);

		o = {caption: 'Subscribers', state: 'app.subscribers', icon: 'fa fa-toggle-down', role: 'eds-ui:services'};
		ret.push(o);

		o = {caption: 'Queue Readers', state: 'app.queueReaderStatus', icon: 'fa fa-tachometer', role: 'eds-ui:monitoring'};
		ret.push(o);

		o = {caption: 'Frailty API', state: 'app.frailtyApi', icon: 'fa fa-flask', role: 'eds-ui:monitoring'};
		ret.push(o);

		o = {caption: 'HL7 Receiver', state: 'app.hl7Receiver', icon: 'fa fa-fire', role: 'eds-ui:monitoring'};
		ret.push(o);

		o = {caption: 'SFTP Reader', state: 'app.sftpReader', icon: 'fa fa-table', role: 'eds-ui:monitoring'};
		ret.push(o);

		o = {caption: 'Scheduled Tasks', state: 'app.scheduledTasks', icon: 'fa fa-clock-o', role: 'eds-ui:monitoring'};
		ret.push(o);

		o = {caption: 'Statistics', state: 'app.stats', icon: 'fa fa-line-chart', role: 'eds-ui:monitoring'};
		ret.push(o);

		o = {caption: 'Audit', state: 'app.audit', icon: 'fa fa-list-ul', role: 'eds-ui:audit'};
		ret.push(o);

		o = {caption: 'Monitoring', state: 'app.monitoring', icon: 'fa fa-list-alt', role: 'eds-ui:monitoring'};
		ret.push(o);

		o = {caption: 'Remote Filing', state: 'app.remoteFiling', icon: 'fa fa-external-link-square', role: 'eds-ui:audit'};
		ret.push(o);

		o = {caption: 'Routing Config', state: 'app.queueing', icon: 'fa fa-map-signs', role: 'eds-ui:queueing'};
		ret.push(o);

		o = {caption: 'System Config', state: 'app.systemList', icon: 'fa fa-building-o', role: 'eds-ui:services'};
		ret.push(o);

		o = {caption: 'Config Manager', state: 'app.configManager', icon: 'fa-umbrella', role: 'eds-ui:services'}
		ret.push(o);

		return ret;

	}
}
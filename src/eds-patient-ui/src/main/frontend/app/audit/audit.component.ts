import {ILoggerService} from "../blocks/logger.service";
import {AuditService} from "./audit.service";

export class AuditComponent implements ng.IComponentOptions {
	template : string;
	controller : string;
	controllerAs : string;

	constructor () {
		this.template = require('./audit.html');
		this.controller = 'AuditController';
		this.controllerAs = '$ctrl';
	}
}

export class AuditController {

	static $inject = ['AuditService', 'LoggerService', '$state'];

	constructor(private auditService:AuditService,
							private logger:ILoggerService) {
	}
}

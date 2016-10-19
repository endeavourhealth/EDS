import {ILoggerService} from "../blocks/logger.service";
import {ConsentService} from "./consent.service";

export class ConsentComponent implements ng.IComponentOptions {
	template : string;
	controller : string;
	controllerAs : string;

	constructor () {
		this.template = require('./consent.html');
		this.controller = 'ConsentController';
		this.controllerAs = '$ctrl';
	}
}

export class ConsentController {

	static $inject = ['ConsentService', 'LoggerService'];

	constructor(private consentService:ConsentService,
							private logger:ILoggerService) {
	}
}


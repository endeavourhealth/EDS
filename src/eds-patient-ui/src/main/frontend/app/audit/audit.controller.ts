import {ILoggerService} from "../blocks/logger.service";
import {IAuditService} from "../core/audit.service";
import {IStateService} from "angular-ui-router";

export class AuditController {

		static $inject = ['AuditService', 'LoggerService', '$state'];

		constructor(private auditService:IAuditService,
								private logger:ILoggerService,
								private $state : IStateService) {
				//this.refresh();
		}
}

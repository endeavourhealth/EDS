import {AuditController} from "./audit.controller";
import {AuditRoute} from "./audit.route";

angular.module('app.audit', [])
	.controller('AuditController', AuditController)
	.config(AuditRoute);
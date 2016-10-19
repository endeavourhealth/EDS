import {AuditController, AuditComponent} from "./audit.component";
import {AuditRoute} from "./audit.route";
import {AuditService} from "./audit.service";

angular.module('app.audit', [])
	.controller('AuditController', AuditController)
	.service('AuditService', AuditService)
	.component('auditComponent', new AuditComponent())
	.config(AuditRoute);
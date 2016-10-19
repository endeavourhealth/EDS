import {AuditComponent} from "./audit.component";
import {AuditRoute} from "./audit.route";
import {AuditService} from "./audit.service";
import {upgradeAdapter} from "../upgradeAdapter";

angular.module('app.audit', [])
	.service('AuditService', AuditService)
	.directive('auditComponent', <angular.IDirectiveFactory>upgradeAdapter.downgradeNg2Component(AuditComponent))
	.config(AuditRoute);
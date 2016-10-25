import {AuditComponent} from "./audit.component";
import {AuditRoute} from "./audit.route";
import {upgradeAdapter} from "../upgradeAdapter";

angular.module('app.audit', [])
	.config(AuditRoute)

	// Hybrid
	.directive('auditComponent', <angular.IDirectiveFactory>upgradeAdapter.downgradeNg2Component(AuditComponent));

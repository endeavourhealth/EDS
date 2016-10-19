import {ConsentComponent} from "./consent.component";
import {ConsentRoute} from "./consent.route";
import {ConsentService} from "./consent.service";
import {upgradeAdapter} from "../upgradeAdapter";

angular.module('app.consent', [])
	.service('ConsentService', ConsentService)
	.config(ConsentRoute)

	// Hybrid
	.directive('consentComponent',  <angular.IDirectiveFactory>upgradeAdapter.downgradeNg2Component(ConsentComponent));
upgradeAdapter.upgradeNg1Provider('ConsentService');
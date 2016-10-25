import {ConsentComponent} from "./consent.component";
import {ConsentRoute} from "./consent.route";
import {upgradeAdapter} from "../upgradeAdapter";

angular.module('app.consent', [])
	.config(ConsentRoute)

	// Hybrid
	.directive('consentComponent',  <angular.IDirectiveFactory>upgradeAdapter.downgradeNg2Component(ConsentComponent));

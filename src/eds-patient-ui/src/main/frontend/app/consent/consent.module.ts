import {ConsentController,ConsentComponent} from "./consent.component";
import {ConsentRoute} from "./consent.route";
import {ConsentService} from "./consent.service";

angular.module('app.consent', [])
	.controller('ConsentController', ConsentController)
	.service('ConsentService', ConsentService)
	.component('consentComponent', new ConsentComponent())
	.config(ConsentRoute);
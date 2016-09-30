import {ConsentController} from "./consent.controller";
import {ConsentRoute} from "./consent.route";

angular.module('app.consent', [])
	.controller('ConsentController', ConsentController)
	.config(ConsentRoute);
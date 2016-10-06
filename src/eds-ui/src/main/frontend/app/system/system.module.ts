import {SystemController} from "./system.controller";
import {systemRoute} from "./system.route";

angular.module('app.system', [])
	.controller('SystemController', SystemController)
	.config(systemRoute);
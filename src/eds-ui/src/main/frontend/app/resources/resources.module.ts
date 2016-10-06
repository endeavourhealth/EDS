import {ResourcesController} from "./resources.controller";
import {ResourcesRoute} from "./resources.route";

angular.module('app.resources', [])
	.controller('ResourcesController', ResourcesController)
	.config(ResourcesRoute);
import {QueryRoute} from "./query.route";
import {QueryController} from "./query.controller";

angular.module('app.query', [])
	.controller('QueryController', QueryController)
	.config(QueryRoute);
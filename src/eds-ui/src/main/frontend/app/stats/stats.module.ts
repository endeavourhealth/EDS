import {StatsController} from "./stats.controller";
import {statsRoute} from "./stats.route";

angular.module('app.stats', [])
	.controller('StatsController', StatsController)
	.config(statsRoute);
import {DashboardController} from './dashboard.controller';
import {DashboardRoute} from './dashboard.route';

angular.module('app.dashboard', [])
	.controller('DashboardController', DashboardController)
	.config(DashboardRoute);
import {AdminController} from "./admin.controller"
import {AdminRoute} from "./admin.route"

angular.module('app.admin', [])
	.controller('AdminController', AdminController)
	.config(AdminRoute);
import {TransformErrorsController} from "./transformErrors.controller";
import {TransformErrorsRoute} from "./transformErrors.route";

angular.module('app.transformErrors', [])
	.controller('TransformErrorsController', TransformErrorsController)
	.config(TransformErrorsRoute);
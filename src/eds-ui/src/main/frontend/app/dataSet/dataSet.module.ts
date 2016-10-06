import {DataSetController} from "./dataSet.controller";
import {DataSetRoute} from "./dataSet.route";

angular.module('app.dataSet', [])
	.controller('DataSetController', DataSetController)
	.config(DataSetRoute);
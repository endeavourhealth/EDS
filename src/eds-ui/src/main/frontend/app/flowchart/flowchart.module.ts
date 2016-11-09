import {FlowChartController} from "./flowchart.controller";
import {FlowChartDirective} from "./flowchart.directives";

angular.module('flowChart', ['dragging'])
	.controller('FlowChartController', FlowChartController)
	.directive('flowChart', FlowChartDirective.factory());
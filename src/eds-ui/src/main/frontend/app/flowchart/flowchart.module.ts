import {FlowChartController} from "./flowchart.controller";
import {FlowChartDirective, ChartJSonEdit} from "./flowchart.directives";

angular.module('flowChart', ['dragging'])
	.controller('FlowChartController', FlowChartController)
	.directive('flowChart', FlowChartDirective.factory())
	.directive('chartJsonEdit', ChartJSonEdit.factory());
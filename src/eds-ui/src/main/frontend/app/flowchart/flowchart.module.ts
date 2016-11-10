import {FlowChartController, FlowChartComponent} from "./flowchart.controller";

angular.module('flowChart', ['dragging'])
	.controller('FlowChartController', FlowChartController)
	.component('flowChart', new FlowChartComponent());
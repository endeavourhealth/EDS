/// <reference path="../../typings/index.d.ts" />

import FlowChartController = app.flowchart.FlowChartController;
import FlowChartDirective = app.flowchart.FlowChartDirective;
import ChartJSonEdit = app.flowchart.ChartJSonEdit;

angular.module('flowChart', ['dragging'])
	.controller('FlowChartController', FlowChartController)
	.directive('flowChart', FlowChartDirective.factory())
	.directive('chartJsonEdit', ChartJSonEdit.factory());
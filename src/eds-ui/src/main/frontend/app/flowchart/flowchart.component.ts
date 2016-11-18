import {flowchart} from "./flowchart.viewmodel";
import {SvgElement} from "../common/svg.helper";
import {Component, Input, Output, EventEmitter} from "@angular/core";
import {DraggingService} from "../mouseCapture/dragging.service";

@Component({
	selector : 'flow-chart',
	template : require('./flowchart.template.html')
})
export class FlowChartComponent {
	@Input() chart : any;
	@Output() onRuleDescription = new EventEmitter();
	@Output() onRulePassAction = new EventEmitter();
	@Output() onRuleFailAction = new EventEmitter();
	@Output() onEditTest = new EventEmitter();

	private ruleClass;
	private destRuleId : number;
	private draggingConnection : boolean;
	private connectorSize : number;
	private mouseOverRule : any;
	private dragPoint1 : any;
	private dragPoint2 : any;
	private mouseDownRule : any;

	constructor(private dragging: DraggingService) {

		this.destRuleId = 0;
		//
		// Init data-model variables.
		//
		this.draggingConnection = false;
		this.connectorSize = 10;

		//
		// Reference to the connection, connector or rule that the mouse is currently over.
		//
		this.mouseOverRule = null;

		this.ruleClass = 'rule';
	}

	// Translate the coordinates so they are relative to the svg element.
	translateCoordinates(x, y) {
		var svg_elem: SvgElement = <SvgElement>document.getElementsByTagName("svg")[0];
		var s = document.getElementById('flowChart').style.zoom;
		if (s == "90%") {
			x = x + (x * 12 / 100);
			y = y + (y * 12 / 100);
		}
		else if (s == "80%") {
			x = x + (x * 26 / 100);
			y = y + (y * 26 / 100);
		}
		else if (s == "70%") {
			x = x + (x * 42 / 100);
			y = y + (y * 42 / 100);
		}
		else if (s == "60%") {
			x = x + (x * 68 / 100);
			y = y + (y * 68 / 100);
		}
		else if (s == "50%") {
			x = x + (x * 102 / 100);
			y = y + (y * 102 / 100);
		}
		var matrix = svg_elem.getScreenCTM();
		var point = svg_elem.createSVGPoint();
		point.x = x;
		point.y = y;
		var r = point.matrixTransform(matrix.inverse());
		return r;
	}

	// Handle mousedown on a rule.
	ruleMouseDown(evt, rule) {
		var vm = this;
		vm.mouseDownRule = rule;
		var chart = this.chart;
		var lastMouseCoords;

		this.dragging.startDrag(evt, {

			//
			// Rule dragging has commenced.
			//
			dragStarted: function (x, y) {

				lastMouseCoords = vm.translateCoordinates(
					x, y);

				//
				// If nothing is selected when dragging starts,
				// at least select the rule we are dragging.
				//
				if (!vm.mouseDownRule.selected()) {
					chart.deselectAll();
					vm.mouseDownRule.select();
				}
			},

			//
			// Dragging selected rule... update their x,y coordinates.
			//
			dragging: function (x, y) {

				var curCoords = vm.translateCoordinates(x, y);
				var deltaX = curCoords.x - lastMouseCoords.x;
				var deltaY = curCoords.y - lastMouseCoords.y;

				chart.updateSelectedRuleLocation(deltaX, deltaY);

				lastMouseCoords = curCoords;
			},

			//
			// The rule wasn't dragged... it was clicked.
			//
			clicked: function () {
				chart.handleRuleClicked(vm.mouseDownRule, evt.ctrlKey);

				vm.onRuleDescription.emit({description : vm.mouseDownRule.description()});
				vm.onRulePassAction.emit({action : vm.mouseDownRule.onPassAction()});
				vm.onRuleFailAction.emit({action : vm.mouseDownRule.onFailAction()});
			},

		});
	}

	editTest(evt, rule) {
		this.onEditTest.emit({ruleId : rule.id()});
	};

	connectorMouseUp(evt, rule, connector, connectorIndex, isInputConnector) {
		this.destRuleId = rule.id();
	}

	// Handle mousedown on an input connector.
	connectorMouseDown(evt, rule, connector, connectorIndex, isInputConnector) {
		var vm = this;
		//
		// Initiate dragging out of a connection.
		//
		vm.dragging.startDrag(evt, {

			//
			// Called when the mouse has moved greater than the threshold distance
			// and dragging has commenced.
			//
			dragStarted: function (x, y) {

				var curCoords = vm.translateCoordinates(x, y);

				vm.draggingConnection = true;
				vm.dragPoint1 = flowchart.computeConnectorPos(rule, connectorIndex, isInputConnector);
				vm.dragPoint2 = {
					x: curCoords.x,
					y: curCoords.y
				};
			},

			//
			// Called on mousemove while dragging out a connection.
			//
			dragging: function (x, y, evt) {
				var startCoords = vm.translateCoordinates(x, y);
				vm.dragPoint1 = flowchart.computeConnectorPos(rule, connectorIndex, isInputConnector);
				vm.dragPoint2 = {
					x: startCoords.x,
					y: startCoords.y
				};
			},

			//
			// Clean up when dragging has finished.
			//
			dragEnded: function () {

				var sourceRuleId = rule.id();
				var destRuleId = vm.destRuleId;

				if (destRuleId > 0) {

					//
					// Dragging has ended...
					// The mouse is over a valid connector...
					// Create a new connection.
					//
					vm.chart.createNewConnection(rule, sourceRuleId, destRuleId, connectorIndex);
					if (connectorIndex == 0) {
						vm.onRulePassAction.emit({action : 'GOTO_RULES'});
					}
					else if (connectorIndex == 1) {
						vm.onRuleFailAction.emit({action : 'GOTO_RULES'});
					}
					vm.destRuleId = 0;
				}

				vm.draggingConnection = false;
				delete vm.dragPoint1;
				delete vm.dragPoint2;
			}

		});
	}

	findDestRuleX(ruleId) {
		var chart = this.chart;
		var x = 0;
		for (var i = 0; i < chart.rule.length; ++i) {
			var rule = chart.rule[i];
			if (rule.data.id == ruleId) {
				x = rule.data.layout.x;
			}
		}
		return x;
	}

	findDestRuleY(ruleId) {
		var chart = this.chart;
		var y = 0;
		for (var i = 0; i < chart.rule.length; ++i) {
			var rule = chart.rule[i];
			if (rule.data.id == ruleId) {
				y = rule.data.layout.y;
			}
		}
		return y;
	}

	stringSplitter(str, width) {
		if (str.length > width) {
			var p = width
			for (; p > 0 && str[p] != ' '; p--) {
			}
			if (p > 0) {
				var left = str.substring(0, p);
				var right = str.substring(p + 1);
				return left + '~' + this.stringSplitter(right, width);
			}
		}
		return str;
	}

	firstString(str, width) {
		var s = this.stringSplitter(str, width);
		return s.split('~')[0];
	}

	secondString(str, width) {
		var s = this.stringSplitter(str, width);
		return s.split('~')[1];
	}

}
import {flowchart} from "./flowchart.viewmodel";
import {SvgHelper, SvgElement} from "../blocks/svg.helper";

export class FlowChartController {
	static $inject = ['$scope', 'dragging', '$element']
	private ruleClass;

	constructor(private $scope: any, private dragging: any, private $element: any) {

		var controller = this;

		$scope.destRuleId = 0;
		//
		// Init data-model variables.
		//
		$scope.draggingConnection = false;
		$scope.connectorSize = 10;

		//
		// Reference to the connection, connector or rule that the mouse is currently over.
		//
		$scope.mouseOverConnection = null;
		$scope.mouseOverRule = null;

		this.ruleClass = 'rule';

	}

	// Search up the HTML element tree for an element the requested class.
	searchUp(element, parentClass) {

		//
		// Reached the root.
		//
		if (element == null || element.length == 0) {
			return null;
		}

		//
		// Check if the element has the class that identifies it as a connector.
		//
		if (SvgHelper.hasClassSVG(element, parentClass)) {
			//
			// Found the connector element.
			//
			return element;
		}

		//
		// Recursively search parent elements.
		//
		return this.searchUp(element.parent(), parentClass);
	}

	// Hit test and retrieve rule and connector that was hit at the specified coordinates.
	hitTest(clientX, clientY) {

		//
		// Retrieve the element the mouse is currently over.
		//

		return document.elementFromPoint(clientX, clientY);
	}

	// Hit test and retrieve rule and connector that was hit at the specified coordinates.
	checkForHit(mouseOverElement, whichClass) {

		//
		// Find the parent element, if any, that is a connector.
		//
		var hoverElement = this.searchUp(jQuery(mouseOverElement), whichClass);
		if (!hoverElement) {
			return null;
		}

		return angular.element(hoverElement).scope();

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

	// Called for each mouse move on the svg element.
	mouseMove(evt) {
		//
		// Clear out all cached mouse over elements.
		//
		this.$scope.mouseOverConnection = null;
		this.$scope.mouseOverRule = null;

		var mouseOverElement = this.hitTest(evt.clientX, evt.clientY);
		if (mouseOverElement == null) {
			// Mouse isn't over anything, just clear all.
			return;
		}

		// Figure out if the mouse is over a rule.
		var scope: any = this.checkForHit(mouseOverElement, this.ruleClass);
		this.$scope.mouseOverRule = (scope && scope.rule) ? scope.rule : null;
	}

	// Handle mousedown on a rule.
	ruleMouseDown(evt, rule) {
		var vm = this;
		var chart = this.$scope.chart;
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
				if (!rule.selected()) {
					chart.deselectAll();
					rule.select();
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
				chart.handleRuleClicked(rule, evt.ctrlKey);

				vm.$scope.$emit('ruleDescription', rule.description());
				vm.$scope.$emit('rulePassAction', rule.onPassAction());
				vm.$scope.$emit('ruleFailAction', rule.onFailAction());
			},

		});
	}

	editTest(evt, rule) {
		this.$scope.$emit('editTest', rule.id());
	};

	connectorMouseUp(evt, rule, connector, connectorIndex, isInputConnector) {
		this.$scope.destRuleId = rule.id();
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

				vm.$scope.draggingConnection = true;
				vm.$scope.dragPoint1 = flowchart.computeConnectorPos(rule, connectorIndex, isInputConnector);
				vm.$scope.dragPoint2 = {
					x: curCoords.x,
					y: curCoords.y
				};
			},

			//
			// Called on mousemove while dragging out a connection.
			//
			dragging: function (x, y, evt) {
				var startCoords = vm.translateCoordinates(x, y);
				vm.$scope.dragPoint1 = flowchart.computeConnectorPos(rule, connectorIndex, isInputConnector);
				vm.$scope.dragPoint2 = {
					x: startCoords.x,
					y: startCoords.y
				};
			},

			//
			// Clean up when dragging has finished.
			//
			dragEnded: function () {

				var sourceRuleId = rule.id();
				var destRuleId = vm.$scope.destRuleId;

				if (destRuleId > 0) {

					//
					// Dragging has ended...
					// The mouse is over a valid connector...
					// Create a new connection.
					//
					vm.$scope.chart.createNewConnection(rule, sourceRuleId, destRuleId, connectorIndex);
					if (connectorIndex == 0) {
						vm.$scope.$emit('rulePassAction', 'GOTO_RULES');
					}
					else if (connectorIndex == 1) {
						vm.$scope.$emit('ruleFailAction', 'GOTO_RULES');
					}
					vm.$scope.destRuleId = 0;
				}

				vm.$scope.draggingConnection = false;
				delete vm.$scope.dragPoint1;
				delete vm.$scope.dragPoint2;
			}

		});
	}

	findDestRuleX(ruleId) {
		var chart = this.$scope.chart;
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
		var chart = this.$scope.chart;
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
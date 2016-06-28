//
// Flowchart module.
//
angular.module('flowChart', ['dragging'] )

	//
	// Directive that generates the rendered chart from the data model.
	//
	.directive('flowChart', function() {
		return {
			restrict: 'E',
			templateUrl: "app/query/flowchart/flowchart.template.html",
			replace: true,
			scope: {
				chart: "=chart",
			},
			controller: 'FlowChartController',
		};
	})

	//
	// Directive that allows the chart to be edited as json in a textarea.
	//
	.directive('chartJsonEdit', function () {
		return {
			restrict: 'A',
			scope: {
				viewModel: "="
			},
			link: function (scope, elem, attr) {

				//
				// Serialize the data model as json and update the textarea.
				//
				var updateJson = function () {
					if (scope.viewModel) {
						var obj = scope.viewModel.data;
						obj = removeAllNull(obj);

						var json = JSON.stringify(obj, null, 4);
						$(elem).val(json);
					}
				};

				function removeAllNull(JsonObj) {
					$.each(JsonObj, function(key, value) {
						if (value === null) {
							delete JsonObj[key];
						} else if (typeof(value) === "object") {
							//JsonObj[key] = removeAllNull(value);
						}
					});
					return JsonObj;
				}

				//
				// First up, set the initial value of the textarea.
				//
				updateJson();

				//
				// Watch for changes in the data model and update the textarea whenever necessary.
				//
				scope.$watch("viewModel.data", updateJson, true);

				//
				// Handle the change event from the textarea and update the data model
				// from the modified json.
				//
				$(elem).bind("input propertychange", function () {
					var json = $(elem).val();
					var dataModel = JSON.parse(json);
					scope.viewModel = new flowchart.ChartViewModel(dataModel);

					scope.$apply();
				});
			}
		}

	})


	//
	// Controller for the flowchart directive.
	//
	.controller('FlowChartController', ['$scope', 'dragging', '$element',
		function FlowChartController ($scope, dragging, $element) {

			var controller = this;

			$scope.destRuleId = 0;

			this.document = document;

			//
			// Wrap jQuery so it can easily be  mocked for testing.
			//
			this.jQuery = function (element) {
				return $(element);
			}

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

			//
			// Search up the HTML element tree for an element the requested class.
			//
			this.searchUp = function (element, parentClass) {

				//
				// Reached the root.
				//
				if (element == null || element.length == 0) {
					return null;
				}

				//
				// Check if the element has the class that identifies it as a connector.
				//
				if (hasClassSVG(element, parentClass)) {
					//
					// Found the connector element.
					//
					return element;
				}

				//
				// Recursively search parent elements.
				//
				return this.searchUp(element.parent(), parentClass);
			};

			//
			// Hit test and retrieve rule and connector that was hit at the specified coordinates.
			//
			this.hitTest = function (clientX, clientY) {

				//
				// Retrieve the element the mouse is currently over.
				//

				return this.document.elementFromPoint(clientX, clientY);
			};

			//
			// Hit test and retrieve rule and connector that was hit at the specified coordinates.
			//
			this.checkForHit = function (mouseOverElement, whichClass) {

				//
				// Find the parent element, if any, that is a connector.
				//
				var hoverElement = this.searchUp(this.jQuery(mouseOverElement), whichClass);
				if (!hoverElement) {
					return null;
				}

				return angular.element(hoverElement).scope();

			};

			//
			// Translate the coordinates so they are relative to the svg element.
			//
			this.translateCoordinates = function(x, y) {
				svg_elem = document.getElementsByTagName("svg")[0];
				var s = document.getElementById('flowChart').style.zoom;
				if (s=="90%") {
					x=x+(x*12/100);
					y=y+(y*12/100);
				}
				else if (s=="80%") {
					x=x+(x*26/100);
					y=y+(y*26/100);
				}
				else if (s=="70%") {
					x=x+(x*42/100);
					y=y+(y*42/100);
				}
				else if (s=="60%") {
					x=x+(x*68/100);
					y=y+(y*68/100);
				}
				else if (s=="50%") {
					x=x+(x*102/100);
					y=y+(y*102/100);
				}
				var matrix = svg_elem.getScreenCTM();
				var point = svg_elem.createSVGPoint();
				point.x = x;
				point.y = y;
				var r = point.matrixTransform(matrix.inverse());
				return r;
			};

			//
			// Called for each mouse move on the svg element.
			//
			$scope.mouseMove = function (evt) {
				//
				// Clear out all cached mouse over elements.
				//
				$scope.mouseOverConnection = null;
				$scope.mouseOverRule = null;

				var mouseOverElement = controller.hitTest(evt.clientX, evt.clientY);
				if (mouseOverElement == null) {
					// Mouse isn't over anything, just clear all.
					return;
				}

				// Figure out if the mouse is over a rule.
				var scope = controller.checkForHit(mouseOverElement, controller.ruleClass);
				$scope.mouseOverRule = (scope && scope.rule) ? scope.rule : null;
			};

			//
			// Handle mousedown on a rule.
			//
			$scope.ruleMouseDown = function (evt, rule) {

				var chart = $scope.chart;
				var lastMouseCoords;

				dragging.startDrag(evt, {

					//
					// Rule dragging has commenced.
					//
					dragStarted: function (x, y) {

						lastMouseCoords = controller.translateCoordinates(
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

						var curCoords = controller.translateCoordinates(x, y);
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

						$scope.$emit('ruleDescription', rule.description());
						$scope.$emit('rulePassAction', rule.onPassAction());
						$scope.$emit('ruleFailAction', rule.onFailAction());
					},

				});
			};

			$scope.editTest = function (evt, rule) {
				$scope.$emit('editTest', rule.id());
			};

			$scope.connectorMouseUp = function (evt, rule, connector, connectorIndex, isInputConnector) {
				$scope.destRuleId = rule.id();
			};

			$scope.findDestRuleX = function (ruleId) {
				var chart = $scope.chart;
				var x = 0;
				for (var i = 0; i < chart.rule.length; ++i) {
					var rule = chart.rule[i];
					if (rule.data.id == ruleId) {
						x = rule.data.layout.x;
					}
				}
				return x;
			}

			$scope.findDestRuleY = function (ruleId) {
				var chart = $scope.chart;
				var y = 0;
				for (var i = 0; i < chart.rule.length; ++i) {
					var rule = chart.rule[i];
					if (rule.data.id == ruleId) {
						y = rule.data.layout.y;
					}
				}
				return y;
			}

			$scope.stringSplitter = function (str, width) {
				if (str.length>width) {
					var p=width
					for (;p>0 && str[p]!=' ';p--) {
					}
					if (p>0) {
						var left = str.substring(0, p);
						var right = str.substring(p+1);
						return left + '~' + $scope.stringSplitter(right, width);
					}
				}
				return str;
			}

			$scope.firstString = function (str, width) {
				var s = $scope.stringSplitter(str, width);
				return s.split('~')[0];
			}

			$scope.secondString = function (str, width) {
				var s = $scope.stringSplitter(str, width);
				return s.split('~')[1];
			}

			//
			// Handle mousedown on an input connector.
			//
			$scope.connectorMouseDown = function (evt, rule, connector, connectorIndex, isInputConnector) {

				//
				// Initiate dragging out of a connection.
				//
				dragging.startDrag(evt, {

					//
					// Called when the mouse has moved greater than the threshold distance
					// and dragging has commenced.
					//
					dragStarted: function (x, y) {

						var curCoords = controller.translateCoordinates(x, y);

						$scope.draggingConnection = true;
						$scope.dragPoint1 = flowchart.computeConnectorPos(rule, connectorIndex, isInputConnector);
						$scope.dragPoint2 = {
							x: curCoords.x,
							y: curCoords.y
						};
					},

					//
					// Called on mousemove while dragging out a connection.
					//
					dragging: function (x, y, evt) {
						var startCoords = controller.translateCoordinates(x, y);
						$scope.dragPoint1 = flowchart.computeConnectorPos(rule, connectorIndex, isInputConnector);
						$scope.dragPoint2 = {
							x: startCoords.x,
							y: startCoords.y
						};
					},

					//
					// Clean up when dragging has finished.
					//
					dragEnded: function () {

						var sourceRuleId = rule.id();
						var destRuleId = $scope.destRuleId;

						if (destRuleId>0) {

							//
							// Dragging has ended...
							// The mouse is over a valid connector...
							// Create a new connection.
							//
							$scope.chart.createNewConnection(rule, sourceRuleId, destRuleId, connectorIndex);
							if (connectorIndex==0) {
								$scope.$emit('rulePassAction', 'GOTO_RULES');
							}
							else if (connectorIndex==1) {
								$scope.$emit('ruleFailAction', 'GOTO_RULES');
							}
							$scope.destRuleId = 0;
						}

						$scope.draggingConnection = false;
						delete $scope.dragPoint1;
						delete $scope.dragPoint2;
					}

				});
			};
		}])
;

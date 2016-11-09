import IModalService = angular.ui.bootstrap.IModalService;
import {ILoggerService} from "../blocks/logger.service";
import {ILibraryService} from "../core/library.service";
import {IAdminService} from "../core/admin.service";
import {ExpressionType} from "../models/ExpressionType";
import {ExpressionEditorController} from "../dialogs/expressionEditor/expressionEditor.controller";
import {Test} from "../models/Test";
import {TestEditorController} from "../dialogs/testEditor/testEditor.controller";
import {QuerySelection} from "../models/QuerySelection";
import {QueryPickerController} from "../dialogs/queryPicker/queryPicker.controller";
import {LibraryItem} from "../models/LibraryItem";
import {StartingRules} from "../models/StartingRules";
import {Query} from "../models/Query";
import {flowchart} from "../flowchart/flowchart.viewmodel";
import {MessageBoxController} from "../dialogs/messageBox/messageBox.controller";

export class QueryController {

	static $inject = ['LoggerService', '$scope', '$stateParams', '$uibModal', '$window', 'LibraryService', 'AdminService'];

	constructor(logger: ILoggerService, $scope: any, $stateParams: any, $modal: IModalService, $window: any,
							libraryService: ILibraryService, adminService: IAdminService) {
		$scope.queryName = "";
		$scope.queryDescription = "";
		$scope.disableRuleProps = false;
		$scope.zoomPercent = "100%";

		$scope.zoomNumber = 100;

		$scope.nextRuleID = 1;

		$scope.queryNameChange = function () {
			$scope.chartViewModel.data.name = $scope.queryName;
		};

		$scope.queryDescriptionChange = function () {
			$scope.chartViewModel.data.description = $scope.queryDescription;
		};

		$scope.ruleDescriptionChange = function () {
			var selectedRule = $scope.chartViewModel.getSelectedRule();
			selectedRule.data.description = $scope.ruleDescription;
		};

		$scope.rulePassActionChange = function () {
			var selectedRule = $scope.chartViewModel.getSelectedRule();
			selectedRule.data.onPass.action = $scope.rulePassAction;
			if ($scope.rulePassAction != "GOTO_RULES") {
				selectedRule.data.onPass.ruleId = <any>[];
			}
		};

		$scope.ruleFailActionChange = function () {
			var selectedRule = $scope.chartViewModel.getSelectedRule();
			selectedRule.data.onFail.action = $scope.ruleFailAction;
			if ($scope.ruleFailAction != "GOTO_RULES") {
				selectedRule.data.onFail.ruleId = <any>[];
			}
		};

		$scope.results = [
			{value: 'GOTO_RULES', displayName: 'Go to rule'},
			{value: 'INCLUDE', displayName: 'Include patient in final result'},
			{value: 'NO_ACTION', displayName: 'No further action'}
		];

		$scope.$on('editTest', function (event: any, ruleId: any) {
			if (ruleId != "0") {
				$scope.ruleId = ruleId;

				var selectedRule = $scope.chartViewModel.getSelectedRule();
				if (selectedRule.data.expression) {
					var rules = <any>[];

					for (var i = 0; i < $scope.chartViewModel.data.query.rule.length; ++i) {
						if ($scope.chartViewModel.data.query.rule[i].description != "START" && !$scope.chartViewModel.data.query.rule[i].expression) {
							var rule = {
								value: $scope.chartViewModel.data.query.rule[i].id,
								displayName: $scope.chartViewModel.data.query.rule[i].description
							}
							rules.push(rule);
						}

					}
					var expression: ExpressionType = selectedRule.data.expression;

					ExpressionEditorController.open($modal, expression, rules)
						.result.then(function (resultData: ExpressionType) {

						selectedRule.data.expression = resultData;
					});
				}
				else if (!selectedRule.data.queryLibraryItemUUID) {
					var test: Test = selectedRule.data.test;

					var originalResultData = jQuery.extend(true, {}, test);

					TestEditorController.open($modal, originalResultData, false)
						.result.then(function (resultData: Test) {

						selectedRule.data.test = resultData;
					});
				}
			}
		});

		$scope.$on('ruleDescription', function (event: any, description: any) {
			if (description == "START") {
				$scope.disableRuleProps = true;
			}
			else {
				$scope.disableRuleProps = false;
			}
			$scope.ruleDescription = description;
		});

		$scope.$on('rulePassAction', function (event: any, action: any) {
			$scope.rulePassAction = action;
		});

		$scope.$on('ruleFailAction', function (event: any, action: any) {
			$scope.ruleFailAction = action;
		});

		$scope.dataModel = false;
		$scope.ShowDataModel = function () {
			$scope.dataModel = !$scope.dataModel;
		};

		$scope.clearQuery = function() {
			MessageBoxController.open($modal, 'Clear Rules', 'Are you sure you want to clear the rules in this query (changes will not be saved)?', 'Yes', 'No')
				.result.then(function() {
				$scope.chartViewModel.clearQuery();
				$scope.ruleDescription = "";
				$scope.rulePassAction = "";
				$scope.ruleFailAction = "";
				$scope.nextRuleID = 1;
			});
		}

		$scope.cancelChanges = function() {
			MessageBoxController.open($modal, 'Cancel Changes', 'Are you sure you want to cancel the editing of this query (changes will not be saved) ?', 'Yes', 'No')
				.result.then(function() {
				adminService.clearPendingChanges();
				logger.error('Query not saved');
				$window.history.back();
			});
		}

		$scope.zoomIn = function () {
			$scope.zoomNumber = $scope.zoomNumber + 10;
			if ($scope.zoomNumber > 100)
				$scope.zoomNumber = 100;
			$scope.zoomPercent = $scope.zoomNumber.toString() + "%";
		};

		$scope.zoomOut = function () {
			$scope.zoomNumber = $scope.zoomNumber - 10;
			if ($scope.zoomNumber < 50)
				$scope.zoomNumber = 50;
			$scope.zoomPercent = $scope.zoomNumber.toString() + "%";
		};

		//
		// Add a new rule to the chart.
		//
		$scope.addNewRule = function (mode: any) {
			//
			// Template for a new rule.
			//

			if ($scope.nextRuleID == 1) { // Add to new Query

				if (mode == 1 || mode == 3) { // Rule or Expression
					this.createStartRule(-162, 25);

					this.createNewRule(194, 5);
				}
				else if (mode == 2) { // Query as a Rule
					var querySelection: QuerySelection;
					var vm = this;
					QueryPickerController.open($modal, querySelection)
						.result.then(function (resultData: QuerySelection) {
						vm.createStartRule(-162, 25);
						vm.createNewQueryRule(194, 5, resultData);
					});
				}

				$scope.chartViewModel.addStartingRule(1);
			}
			else { // Add to existing Query

				switch (mode) {
					case "1": // normal Rule
						this.createNewRule(566, 7);
						break;
					case "2": // Query as a Rule
						var querySelection: QuerySelection;
						var vm = this;
						QueryPickerController.open($modal, querySelection)
							.result.then(function (resultData: QuerySelection) {
							vm.createNewQueryRule(566, 7, resultData);
						});
						break;
					case "3": // Expression
						this.createNewExpression(566, 7);
						break;
				}
			}
		};

		$scope.createStartRule = function (x: any, y: any) {
			var newStartRuleDataModel = {
				description: "START",
				id: 0,
				layout: {
					x: x,
					y: y
				},
				onPass: {
					action: "",
					ruleId: <any>[]
				},
				onFail: {
					action: "",
					ruleId: <any>[]
				}
			};

			$scope.chartViewModel.addRule(newStartRuleDataModel);
		}

		$scope.createNewRule = function (x: any, y: any) {
			var newRuleDataModel = {
				description: "Rule Description",
				id: $scope.nextRuleID++,
				layout: {
					x: x,
					y: y
				},
				onPass: {
					action: "INCLUDE",
					ruleId: <any>[]
				},
				onFail: {
					action: "NO_ACTION",
					ruleId: <any>[]
				}
			};
			$scope.chartViewModel.addRule(newRuleDataModel);
		}

		$scope.createNewExpression = function (x: any, y: any) {
			var newExpressionRuleDataModel = {
				description: "Expression Description",
				id: $scope.nextRuleID++,
				layout: {
					x: x,
					y: y
				},
				onPass: {
					action: "INCLUDE",
					ruleId: <any>[]
				},
				onFail: {
					action: "NO_ACTION",
					ruleId: <any>[]
				},
				expression: {
					expressionText: "",
					variable: <any>[]
				}
			};
			$scope.chartViewModel.addRule(newExpressionRuleDataModel);
		}

		$scope.createNewQueryRule = function (x: any, y: any, resultData: any) {
			var newQueryRuleDataModel = {
				description: resultData.name + "~" + resultData.description,
				id: $scope.nextRuleID++,
				layout: {
					x: x,
					y: y
				},
				onPass: {
					action: "INCLUDE",
					ruleId: <any>[]
				},
				onFail: {
					action: "NO_ACTION",
					ruleId: <any>[]
				},
				queryLibraryItemUUID: resultData.id
			};

			$scope.chartViewModel.addRule(newQueryRuleDataModel);
		}

		//
		// Delete selected rule and connections.
		//
		$scope.deleteSelected = function () {
			$scope.chartViewModel.deleteSelected();
		};

		$scope.save = function (close: boolean) {

			if ($scope.queryName == "") {
				logger.error('Please enter a name for the query');
				return;
			}

			if ($scope.chartViewModel.data.query.rule.length == 0) {
				logger.error('Please create a rule in this query');
				return;
			}

			for (var i = 0; i < $scope.chartViewModel.data.query.rule.length; ++i) {
				var rule = $scope.chartViewModel.data.query.rule[i];
				if (!rule.test && !rule.expression && !rule.queryLibraryItemUUID && rule.description != "START") {
					logger.error('Rule "' + rule.description + '" does not have a test');
					return;
				}
			}

			for (var i = 0; i < $scope.chartViewModel.data.query.rule.length; ++i) {
				var rule = $scope.chartViewModel.data.query.rule[i];
				if (!rule.test && (rule.expression && rule.expression.variable.length == 0) && rule.description != "START") {
					logger.error('Expression "' + rule.description + '" does not have any variables');
					return;
				}
			}

			for (var i = 0; i < $scope.chartViewModel.data.query.rule.length; ++i) {
				var rule = $scope.chartViewModel.data.query.rule[i];
				if (rule.description != "START") {
					if (rule.onPass.action == "") {
						logger.error('Rule "' + rule.description + '" does not have a PASS action');
						return;
					}
					if (rule.onFail.action == "") {
						logger.error('Rule "' + rule.description + '" does not have a FAIL action');
						return;
					}
				}
			}

			for (var i = 0; i < $scope.chartViewModel.data.query.rule.length; ++i) {
				if ($scope.chartViewModel.data.query.rule[i].description == "START") {
					$scope.chartViewModel.data.query.rule.splice(i, 1);
					$scope.chartViewModel.rule.splice(i, 1);
				}

			}

			var libraryItem = $scope.chartViewModel.data;

			libraryService.saveLibraryItem(libraryItem)
				.then(function (libraryItem: LibraryItem) {
					$scope.chartViewModel.data.uuid = libraryItem.uuid;

					$scope.chartViewModel = new flowchart.ChartViewModel($scope.chartViewModel.data);

					var newStartRuleDataModel = {
						description: "START",
						id: 0,
						layout: {
							x: -162,
							y: 25
						},
						onPass: {
							action: "",
							ruleId: <any>[]
						},
						onFail: {
							action: "",
							ruleId: <any>[]
						}
					};

					$scope.chartViewModel.addRule(newStartRuleDataModel);

					adminService.clearPendingChanges();
					logger.success('Query saved successfully', libraryItem, 'Saved');

					if (close) {
						$window.history.back();
					}
				})
				.catch(function (data) {
					logger.error('Error saving query', data, 'Error');
				});
			;
		}

		//
		// Setup the data-model for the chart.
		//

		var startingRules: StartingRules = {
			ruleId: []
		}

		var query: Query = {
			parentQueryUuid: null,
			startingRules: startingRules,
			rule: []
		}

		var libraryItem: LibraryItem = {
			uuid: null,
			name: null,
			description: null,
			folderUuid: $stateParams.itemUuid,
			query: query,
			codeSet: null,
			dataSet: null,
			protocol: null,
			system: null
		};

		//
		// Create the view-model for the chart and attach to the scope.
		//
		$scope.chartViewModel = new flowchart.ChartViewModel(libraryItem);

		switch ($stateParams.itemAction) {
			case "view":
			case "edit":
				libraryService.getLibraryItem($stateParams.itemUuid)
					.then(function (libraryItem: LibraryItem) {
						$scope.chartViewModel = new flowchart.ChartViewModel(libraryItem);

						$scope.queryName = libraryItem.name;
						$scope.queryDescription = libraryItem.description;


						var newStartRuleDataModel = {
							description: "START",
							id: 0,
							layout: {
								x: -162,
								y: 25
							},
							onPass: {
								action: "",
								ruleId: <any>[]
							},
							onFail: {
								action: "",
								ruleId: <any>[]
							}
						};

						$scope.chartViewModel.addRule(newStartRuleDataModel);

						var highestId = 1;
						for (var i = 0; i < $scope.chartViewModel.data.query.rule.length; ++i) {
							var id = $scope.chartViewModel.data.query.rule[i].id;
							if (parseInt(id) > highestId) {
								highestId = parseInt(id);
							}
						}
						$scope.nextRuleID = highestId + 1;

					})
					.catch(function (data) {
						logger.error('Error loading query', data, 'Error');
					});
				;
				break;
			default:
		}


	}
}
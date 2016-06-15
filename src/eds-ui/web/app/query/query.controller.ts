/// <reference path="../../typings/tsd.d.ts" />

var flowchart : any;

module app.query {
	import TestEditorController = app.dialogs.TestEditorController;
	import ExpressionEditorController = app.dialogs.ExpressionEditorController;
	import QueryPickerController = app.dialogs.QueryPickerController;
	import IModalService = angular.ui.bootstrap.IModalService;
	import IModalSettings = angular.ui.bootstrap.IModalSettings;
	import Test = app.models.Test;
	import ILibraryService = app.core.ILibraryService;
	import LibraryItem = app.models.LibraryItem;
	import Query = app.models.Query;
	import StartingRules = app.models.StartingRules;
	import ExpressionType = app.models.ExpressionType;
	import QuerySelection = app.models.QuerySelection;

	'use strict';

	class QueryController {
	}

	angular
		.module('app.query')
		.directive('clearQuery', function () {
			return {
				template: '<div>' +
				'<div class="modal-dialog">' +
				'<div class="modal-content">' +
				'<div class="modal-header">' +
				'<button type="button" class="close" ng-click="toggleClearQuery()" aria-hidden="true">&times;</button>' +
				'<h4 class="modal-title">{{ title }}</h4>' +
				'</div>' +
				'<div class="modal-body" ng-transclude></div>' +
				'</div>' +
				'</div>' +
				'</div>',
				restrict: 'E',
				transclude: true,
				replace:true,
				scope:true,
				link: function postLink(scope: any, element: any, attrs: any) {
					scope.title = attrs.title;
				}
			};
		})
		.directive('cancelChanges', function () {
			return {
				template: '<div>' +
				'<div class="modal-dialog">' +
				'<div class="modal-content">' +
				'<div class="modal-header">' +
				'<button type="button" class="close" ng-click="toggleCancelChanges()" aria-hidden="true">&times;</button>' +
				'<h4 class="modal-title">{{ title }}</h4>' +
				'</div>' +
				'<div class="modal-body" ng-transclude></div>' +
				'</div>' +
				'</div>' +
				'</div>',
				restrict: 'E',
				transclude: true,
				replace: true,
				scope: true,
				link: function postLink(scope: any, element: any, attrs: any) {
					scope.title = attrs.title;
				}
			};
		})
		.controller('QueryController', ['LoggerService', '$scope', '$stateParams', '$uibModal','$window','LibraryService','AdminService',
			function QueryController (logger : app.blocks.ILoggerService, $scope : any, $stateParams : any, $modal : IModalService, $window : any,
									  libraryService : ILibraryService, adminService : IAdminService) {

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
					if ($scope.rulePassAction!="GOTO_RULES") {
						selectedRule.data.onPass.ruleId = <any>[];
					}
				};

				$scope.ruleFailActionChange = function () {
					var selectedRule = $scope.chartViewModel.getSelectedRule();
					selectedRule.data.onFail.action = $scope.ruleFailAction;
					if ($scope.ruleFailAction!="GOTO_RULES") {
						selectedRule.data.onFail.ruleId = <any>[];
					}
				};

				$scope.results = [
					{value: 'GOTO_RULES', displayName: 'Go to rule'},
					{value: 'INCLUDE', displayName: 'Include patient in final result'},
					{value: 'NO_ACTION', displayName: 'No further action'}
				];

				$scope.$on('editTest', function(event : any, ruleId : any) {
					if (ruleId!="0") {
						$scope.ruleId = ruleId;

						var selectedRule = $scope.chartViewModel.getSelectedRule();
						if (selectedRule.data.expression) {
							var rules = <any>[];

							for (var i = 0; i < $scope.chartViewModel.data.query.rule.length; ++i) {
								if ($scope.chartViewModel.data.query.rule[i].description!="START" &&
									!$scope.chartViewModel.data.query.rule[i].expression) {
									var rule = {
										value: $scope.chartViewModel.data.query.rule[i].id,
										displayName: $scope.chartViewModel.data.query.rule[i].description
									}
									rules.push(rule);
								}

							}
							var expression : ExpressionType = selectedRule.data.expression;

							ExpressionEditorController.open($modal, expression, rules)
								.result.then(function(resultData : ExpressionType){

								selectedRule.data.expression = resultData;
							});
						}
						else if (!selectedRule.data.queryLibraryItemUUID) {
							var test : Test = selectedRule.data.test;

							var originalResultData = jQuery.extend(true, {}, test);

							TestEditorController.open($modal, originalResultData, false)
								.result.then(function(resultData : Test){

								selectedRule.data.test = resultData;
							});
						}
					}
				});

				$scope.$on('ruleDescription', function(event : any, description : any) {
					if (description=="START") {
						$scope.disableRuleProps = true;
					}
					else {
						$scope.disableRuleProps = false;
					}
					$scope.ruleDescription = description;
				});

				$scope.$on('rulePassAction', function(event : any, action : any) {
					$scope.rulePassAction = action;
				});

				$scope.$on('ruleFailAction', function(event : any, action : any) {
					$scope.ruleFailAction = action;
				});

				$scope.dataModel = false;
				$scope.ShowDataModel = function () {
					$scope.dataModel = !$scope.dataModel;
				};

				$scope.showClearQuery = false;
				$scope.toggleClearQuery = function () {
					$scope.showClearQuery = !$scope.showClearQuery;
				};

				$scope.clearQueryYes = function () {
					$scope.chartViewModel.clearQuery();
					$scope.ruleDescription = "";
					$scope.rulePassAction = "";
					$scope.ruleFailAction = "";
					$scope.nextRuleID = 1;
					this.toggleClearQuery();
				};

				$scope.showCancelChanges = false;
				$scope.toggleCancelChanges = function () {
					$scope.showCancelChanges = !$scope.showCancelChanges;
				};

				$scope.cancelChangesYes = function () {
					adminService.clearPendingChanges();
					logger.error('Query not saved');
					$window.history.back();
					this.toggleCancelChanges();
				};

				$scope.zoomIn = function () {
					$scope.zoomNumber=$scope.zoomNumber+10;
					if ($scope.zoomNumber>100)
						$scope.zoomNumber = 100;
					$scope.zoomPercent = $scope.zoomNumber.toString()+"%";
				};

				$scope.zoomOut = function () {
					$scope.zoomNumber=$scope.zoomNumber-10;
					if ($scope.zoomNumber<50)
						$scope.zoomNumber = 50;
					$scope.zoomPercent = $scope.zoomNumber.toString()+"%";
				};

				//
				// Add a new rule to the chart.
				//
				$scope.addNewRule = function (mode : any) {
					//
					// Template for a new rule.
					//

					if ($scope.nextRuleID==1) { // Add to new Query

						if (mode==1||mode==3) { // Rule or Expression
							this.createStartRule(-162, 25);

							this.createNewRule(194, 5);
						}
						else if (mode==2) { // Query as a Rule
							var querySelection : QuerySelection;
							var vm = this;
							QueryPickerController.open($modal, querySelection)
								.result.then(function(resultData : QuerySelection){
								vm.createStartRule(-162,25);
								vm.createNewQueryRule(194, 5, resultData);
							});
						}

						$scope.chartViewModel.addStartingRule(1);
					}
					else { // Add to existing Query

						switch(mode) {
							case "1": // normal Rule
								this.createNewRule(566, 7);
								break;
							case "2": // Query as a Rule
								var querySelection : QuerySelection;
								var vm = this;
								QueryPickerController.open($modal, querySelection)
									.result.then(function(resultData : QuerySelection){
									vm.createNewQueryRule(566, 7, resultData);
								});
								break;
							case "3": // Expression
								this.createNewExpression(566,7);
								break;
						}
					}
				};

				$scope.createStartRule = function(x:any, y:any) {
					var newStartRuleDataModel = {
						description: "START",
						id: 0,
						layout: {
							x: x,
							y: y
						},
						onPass: {
							action: "",
							ruleId : <any>[]
						},
						onFail: {
							action: "",
							ruleId: <any>[]
						}
					};

					$scope.chartViewModel.addRule(newStartRuleDataModel);
				}

				$scope.createNewRule = function(x:any, y:any) {
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

				$scope.createNewExpression = function(x:any, y:any) {
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

				$scope.createNewQueryRule = function(x:any, y:any, resultData:any) {
					var newQueryRuleDataModel = {
						description: resultData.name+"~"+resultData.description,
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

				$scope.save = function (close : boolean) {

					if ($scope.queryName=="") {
						logger.error('Please enter a name for the query');
						return;
					}

					if ($scope.chartViewModel.data.query.rule.length==0) {
						logger.error('Please create a rule in this query');
						return;
					}

					for (var i = 0; i < $scope.chartViewModel.data.query.rule.length; ++i) {
						var rule = $scope.chartViewModel.data.query.rule[i];
						if (!rule.test && !rule.expression && !rule.queryLibraryItemUUID && rule.description!="START") {
							logger.error('Rule "'+rule.description+'" does not have a test');
							return;
						}
					}

					for (var i = 0; i < $scope.chartViewModel.data.query.rule.length; ++i) {
						var rule = $scope.chartViewModel.data.query.rule[i];
						if (!rule.test && (rule.expression && rule.expression.variable.length==0) && rule.description!="START") {
							logger.error('Expression "'+rule.description+'" does not have any variables');
							return;
						}
					}

					for (var i = 0; i < $scope.chartViewModel.data.query.rule.length; ++i) {
						var rule = $scope.chartViewModel.data.query.rule[i];
						if (rule.description!="START") {
							if (rule.onPass.action=="") {
								logger.error('Rule "'+rule.description+'" does not have a PASS action');
								return;
							}
							if (rule.onFail.action=="") {
								logger.error('Rule "'+rule.description+'" does not have a FAIL action');
								return;
							}
						}
					}

					for (var i = 0; i < $scope.chartViewModel.data.query.rule.length; ++i) {
						if ($scope.chartViewModel.data.query.rule[i].description=="START") {
							$scope.chartViewModel.data.query.rule.splice(i,1);
							$scope.chartViewModel.rule.splice(i,1);
						}

					}

					var libraryItem = $scope.chartViewModel.data;

					libraryService.saveLibraryItem(libraryItem)
						.then(function(libraryItem : LibraryItem) {
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
									ruleId : <any>[]
								},
								onFail: {
									action: "",
									ruleId: <any>[]
								}
							};

							$scope.chartViewModel.addRule(newStartRuleDataModel);

							adminService.clearPendingChanges();
							logger.success('Query saved successfully', libraryItem, 'Saved');

							if (close) { $window.history.back(); }
						})
						.catch(function(data) {
							logger.error('Error saving query', data, 'Error');
						});;
				}

				//
				// Setup the data-model for the chart.
				//

				var startingRules : StartingRules = {
					ruleId : []
				}

				var query : Query = {
					parentQueryUuid  : null,
					startingRules : startingRules,
					rule : []
				}

				var libraryItem : LibraryItem = {
					uuid: null,
					name: null,
					description: null,
					folderUuid: $stateParams.itemUuid,
					query: query,
					codeSet : null,
					listReport : null,
					protocol : null,
					system : null
				};

				//
				// Create the view-model for the chart and attach to the scope.
				//
				$scope.chartViewModel = new flowchart.ChartViewModel(libraryItem);

				switch($stateParams.itemAction) {
					case "view":
					case "edit":
						libraryService.getLibraryItem($stateParams.itemUuid)
							.then(function(libraryItem : LibraryItem) {
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
										ruleId : <any>[]
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
								$scope.nextRuleID = highestId+1;

							})
							.catch(function(data) {
								logger.error('Error loading query', data, 'Error');
							});;
						break;
					default:
				}


			}])

}
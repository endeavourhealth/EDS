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
	private queryName : string;
	private queryDescription : string;
	private disableRuleProps : boolean;
	private zoomPercent : string;
	private zoomNumber : number;
	private ruleId : number;
	private nextRuleID : number;
	private chartViewModel : any;
	private results : any;
	private dataModel : boolean;
	private startingRules: StartingRules;
	private query: Query;
	private libraryItem: LibraryItem;
	private rulePassAction : string;
	private ruleFailAction : string;
	private ruleDescription : string;

	static $inject = ['LoggerService', '$scope', '$stateParams', '$uibModal', '$window', 'LibraryService', 'AdminService'];

	constructor(private logger: ILoggerService, $scope: any, $stateParams: any, private $modal: IModalService, private $window: any,
							private libraryService: ILibraryService, private adminService: IAdminService) {
		this.queryName = "";
		this.queryDescription = "";
		this.disableRuleProps = false;
		this.zoomPercent = "100%";
		this.zoomNumber = 100;
		this.nextRuleID = 1;
		this.results = [
			{value: 'GOTO_RULES', displayName: 'Go to rule'},
			{value: 'INCLUDE', displayName: 'Include patient in final result'},
			{value: 'NO_ACTION', displayName: 'No further action'}
		];
		this.dataModel = false;


		this.startingRules = {
			ruleId: []
		}

		this.query = {
			parentQueryUuid: null,
			startingRules: this.startingRules,
			rule: []
		}

		this.libraryItem = {
			uuid: null,
			name: null,
			description: null,
			folderUuid: $stateParams.itemUuid,
			query: this.query,
			codeSet: null,
			dataSet: null,
			protocol: null,
			system: null
		};

		this.createModel(this.libraryItem);

		this.performAction($stateParams.itemAction, $stateParams.itemUuid);
		this.subscribeFlowchartEvents($scope);
	}

	private subscribeFlowchartEvents(scope : any) {
		var vm = this;
		scope.$on('ruleDescription', function (event: any, description: any) {
			if (description == "START") {
				vm.disableRuleProps = true;
			}
			else {
				vm.disableRuleProps = false;
			}
			vm.ruleDescription = description;
		});
		scope.$on('rulePassAction', function (event: any, action: any) {
			vm.rulePassAction = action;
		});
		scope.$on('ruleFailAction', function (event: any, action: any) {
			vm.ruleFailAction = action;
		});
		scope.$on('editTest', function (event: any, ruleId: any) {
			if (ruleId != "0") {
				vm.ruleId = ruleId;

				var selectedRule = vm.chartViewModel.getSelectedRule();
				if (selectedRule.data.expression) {
					var rules = <any>[];

					for (var i = 0; i < vm.chartViewModel.data.query.rule.length; ++i) {
						if (vm.chartViewModel.data.query.rule[i].description != "START" && !vm.chartViewModel.data.query.rule[i].expression) {
							var rule = {
								value: vm.chartViewModel.data.query.rule[i].id,
								displayName: vm.chartViewModel.data.query.rule[i].description
							}
							rules.push(rule);
						}

					}
					var expression: ExpressionType = selectedRule.data.expression;

					ExpressionEditorController.open(vm.$modal, expression, rules)
						.result.then(function (resultData: ExpressionType) {

						selectedRule.data.expression = resultData;
					});
				}
				else if (!selectedRule.data.queryLibraryItemUUID) {
					var test: Test = selectedRule.data.test;

					var originalResultData = jQuery.extend(true, {}, test);

					TestEditorController.open(vm.$modal, originalResultData, false)
						.result.then(function (resultData: Test) {

						selectedRule.data.test = resultData;
					});
				}
			}
		});
	}

	private createModel(libraryItem : LibraryItem) {
		this.chartViewModel = new flowchart.ChartViewModel(libraryItem);
	}

	private performAction(itemAction : string, itemUuid : string) {
		var vm = this;
		switch (itemAction) {
			case "view":
			case "edit":
				vm.libraryService.getLibraryItem(itemUuid)
					.then(function (libraryItem: LibraryItem) {
						vm.createModel(libraryItem);

						vm.queryName = libraryItem.name;
						vm.queryDescription = libraryItem.description;


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

						vm.chartViewModel.addRule(newStartRuleDataModel);

						var highestId = 1;
						for (var i = 0; i < vm.chartViewModel.data.query.rule.length; ++i) {
							var id = vm.chartViewModel.data.query.rule[i].id;
							if (parseInt(id) > highestId) {
								highestId = parseInt(id);
							}
						}
						vm.nextRuleID = highestId + 1;

					})
					.catch(function (data) {
						vm.logger.error('Error loading query', data, 'Error');
					});
				break;
			default:
		}
	}


	queryNameChange() {
		this.chartViewModel.data.name = this.queryName;
	};

		queryDescriptionChange() {
	this.chartViewModel.data.description = this.queryDescription;
		};

		ruleDescriptionChange() {
			var selectedRule = this.chartViewModel.getSelectedRule();
			selectedRule.data.description = this.ruleDescription;
		};

		rulePassActionChange() {
			var selectedRule = this.chartViewModel.getSelectedRule();
			selectedRule.data.onPass.action = this.rulePassAction;
			if (this.rulePassAction != "GOTO_RULES") {
				selectedRule.data.onPass.ruleId = <any>[];
			}
		};

		ruleFailActionChange() {
			var selectedRule = this.chartViewModel.getSelectedRule();
			selectedRule.data.onFail.action = this.ruleFailAction;
			if (this.ruleFailAction != "GOTO_RULES") {
				selectedRule.data.onFail.ruleId = <any>[];
			}
		};

		ShowDataModel() {
			this.dataModel = !this.dataModel;
		};

		clearQuery() {
			var vm = this;
			MessageBoxController.open(vm.$modal, 'Clear Rules', 'Are you sure you want to clear the rules in this query (changes will not be saved)?', 'Yes', 'No')
				.result.then(function() {
				vm.chartViewModel.clearQuery();
				vm.ruleDescription = "";
				vm.rulePassAction = "";
				vm.ruleFailAction = "";
				vm.nextRuleID = 1;
			});
		}

		cancelChanges() {
			var vm = this;
			MessageBoxController.open(vm.$modal, 'Cancel Changes', 'Are you sure you want to cancel the editing of this query (changes will not be saved) ?', 'Yes', 'No')
				.result.then(function() {
				vm.adminService.clearPendingChanges();
				vm.logger.error('Query not saved');
				vm.$window.history.back();
			});
		}

		zoomIn() {
			this.zoomNumber = this.zoomNumber + 10;
			if (this.zoomNumber > 100)
				this.zoomNumber = 100;
			this.zoomPercent = this.zoomNumber.toString() + "%";
		};

		zoomOut() {
			this.zoomNumber = this.zoomNumber - 10;
			if (this.zoomNumber < 50)
				this.zoomNumber = 50;
			this.zoomPercent = this.zoomNumber.toString() + "%";
		};

		//
		// Add a new rule to the chart.
		//
		addNewRule(mode: any) {
			//
			// Template for a new rule.
			//

			if (this.nextRuleID == 1) { // Add to new Query

				if (mode == 1 || mode == 3) { // Rule or Expression
					this.createStartRule(-162, 25);

					this.createNewRule(194, 5);
				}
				else if (mode == 2) { // Query as a Rule
					var querySelection: QuerySelection;
					var vm = this;
					QueryPickerController.open(this.$modal, querySelection)
						.result.then(function (resultData: QuerySelection) {
						vm.createStartRule(-162, 25);
						vm.createNewQueryRule(194, 5, resultData);
					});
				}

				this.chartViewModel.addStartingRule(1);
			}
			else { // Add to existing Query

				switch (mode) {
					case "1": // normal Rule
						this.createNewRule(566, 7);
						break;
					case "2": // Query as a Rule
						var querySelection: QuerySelection;
						var vm = this;
						QueryPickerController.open(this.$modal, querySelection)
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

		createStartRule (x: any, y: any) {
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

			this.chartViewModel.addRule(newStartRuleDataModel);
		}

		createNewRule(x: any, y: any) {
			var newRuleDataModel = {
				description: "Rule Description",
				id: this.nextRuleID++,
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
			this.chartViewModel.addRule(newRuleDataModel);
		}

		createNewExpression(x: any, y: any) {
			var newExpressionRuleDataModel = {
				description: "Expression Description",
				id: this.nextRuleID++,
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
			this.chartViewModel.addRule(newExpressionRuleDataModel);
		}

		createNewQueryRule(x: any, y: any, resultData: any) {
			var newQueryRuleDataModel = {
				description: resultData.name + "~" + resultData.description,
				id: this.nextRuleID++,
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

			this.chartViewModel.addRule(newQueryRuleDataModel);
		}

		//
		// Delete selected rule and connections.
		//
		deleteSelected() {
			this.chartViewModel.deleteSelected();
		};

		save(close: boolean) {
			var vm = this;
			if (vm.queryName == "") {
				vm.logger.error('Please enter a name for the query');
				return;
			}

			if (vm.chartViewModel.data.query.rule.length == 0) {
				vm.logger.error('Please create a rule in this query');
				return;
			}

			for (var i = 0; i < vm.chartViewModel.data.query.rule.length; ++i) {
				var rule = vm.chartViewModel.data.query.rule[i];
				if (!rule.test && !rule.expression && !rule.queryLibraryItemUUID && rule.description != "START") {
					vm.logger.error('Rule "' + rule.description + '" does not have a test');
					return;
				}
			}

			for (var i = 0; i < vm.chartViewModel.data.query.rule.length; ++i) {
				var rule = vm.chartViewModel.data.query.rule[i];
				if (!rule.test && (rule.expression && rule.expression.variable.length == 0) && rule.description != "START") {
					vm.logger.error('Expression "' + rule.description + '" does not have any variables');
					return;
				}
			}

			for (var i = 0; i < vm.chartViewModel.data.query.rule.length; ++i) {
				var rule = vm.chartViewModel.data.query.rule[i];
				if (rule.description != "START") {
					if (rule.onPass.action == "") {
						vm.logger.error('Rule "' + rule.description + '" does not have a PASS action');
						return;
					}
					if (rule.onFail.action == "") {
						vm.logger.error('Rule "' + rule.description + '" does not have a FAIL action');
						return;
					}
				}
			}

			for (var i = 0; i < vm.chartViewModel.data.query.rule.length; ++i) {
				if (vm.chartViewModel.data.query.rule[i].description == "START") {
					vm.chartViewModel.data.query.rule.splice(i, 1);
					vm.chartViewModel.rule.splice(i, 1);
				}

			}

			var libraryItem = vm.chartViewModel.data;

			vm.libraryService.saveLibraryItem(libraryItem)
				.then(function (libraryItem: LibraryItem) {
					vm.chartViewModel.data.uuid = libraryItem.uuid;

					vm.createModel(vm.chartViewModel.data);

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

					vm.chartViewModel.addRule(newStartRuleDataModel);

					vm.adminService.clearPendingChanges();
					vm.logger.success('Query saved successfully', libraryItem, 'Saved');

					if (close) {
						vm.$window.history.back();
					}
				})
				.catch(function (data) {
					vm.logger.error('Error saving query', data, 'Error');
				});
		}
}
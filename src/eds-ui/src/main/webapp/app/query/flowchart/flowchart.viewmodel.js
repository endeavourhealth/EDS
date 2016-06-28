
//
// Global accessor.
//
var flowchart = {};

// Module.
(function () {

	flowchart.ruleWidth = 250;
	flowchart.ruleDescriptionHeight = 60;
	flowchart.connectorHeight = 55;

	//
	// Compute the Y coordinate of a connector, given its index.
	//
	flowchart.computeConnectorY = function (connectorIndex) {
		return flowchart.ruleDescriptionHeight + (connectorIndex * flowchart.connectorHeight);
	}

	//
	// Compute the position of a connector in the graph.
	//
	flowchart.computeConnectorPos = function (rule, connectorIndex, inputConnector) {
		return {
			x: rule.x() + (inputConnector ? 0 : flowchart.ruleWidth),
			y: rule.y() + flowchart.computeConnectorY(connectorIndex),
		};
	};

	//
	// View model for a startingRule.
	//
	flowchart.StartingRulesViewModel = function (startingRulesDataModel) {

		this.data = startingRulesDataModel;

		this.ruleId = function () {
			return this.data;
		}

	};

	//
	// View model for a rule.
	//
	flowchart.RuleViewModel = function (ruleDataModel) {

		this.data = ruleDataModel;

		this._selected = false;

		this.description = function () {
			var d = this.data.description;
			var r = "";
			var desc = d.split('~');
			r = desc[0];
			return r || "";
		};

		this.extendedDescription = function () {
			var d = this.data.description;
			var r = "";
			var desc = d.split('~');
			if (desc.length>1)
				r = desc[1];
			return r || "";
		};

		this.id = function () {
			return this.data.id;
		};

		this.test = function () {
			return this.data.test;
		}

		this.testLibraryItemUUID = function () {
			return this.data.testLibraryItemUUID;
		}

		this.queryLibraryItemUUID = function () {
			return this.data.queryLibraryItemUUID;
		}

		this.expression = function () {
			return this.data.expression;
		}

		this.onPassAction = function () {
			return this.data.onPass.action;
		}

		this.onPassRuleId = function () {
			return this.data.onPass.ruleId;
		}

		this.onFailAction = function () {
			return this.data.onFail.action;
		}

		this.onFailRuleId = function () {
			return this.data.onFail.ruleId;
		}

		this.x = function () {
			return this.data.layout.x;
		};

		this.y = function () {
			return this.data.layout.y;
		};

		this.width = function () {
			return flowchart.ruleWidth;
		}

		this.height = function () {
			var numConnectors = 2;
			return flowchart.computeConnectorY(numConnectors);
		}

		this.select = function () {
			this._selected = true;
		};

		this.deselect = function () {
			this._selected = false;
		};

		this.toggleSelected = function () {
			this._selected = !this._selected;
		};

		this.selected = function () {
			return this._selected;
		};

	};

	var createStartingRulesViewModel = function (startingRulesDataModel) {
		var startingRulesViewModel = [];

		if (startingRulesDataModel) {
			for (var i = 0; i < startingRulesDataModel.length; ++i) {
				startingRulesViewModel.push(new flowchart.StartingRulesViewModel(startingRulesDataModel[i]));
			}
		}

		return startingRulesViewModel;
	};

	var createRuleViewModel = function (ruleDataModel) {
		var ruleViewModel = [];

		if (ruleDataModel) {
			for (var i = 0; i < ruleDataModel.length; ++i) {
				ruleViewModel.push(new flowchart.RuleViewModel(ruleDataModel[i]));
			}
		}

		return ruleViewModel;
	};

	//
	// View model for the chart.
	//
	flowchart.ChartViewModel = function (libraryItem) {

		// reference to the libraryItem data
		this.data = libraryItem;

		// create rules view model
		this.rule = createRuleViewModel(this.data.query.rule);

		// create startingRules view model
		this.startingRules = createStartingRulesViewModel(this.data.query.startingRules.ruleId);

		//
		// Create a view model for a new connection.
		//
		this.createNewConnection = function (sourceRule, sourceRuleId, destRuleId, connectorIndex) {

			if (sourceRule.description()=="START") {
				this.addStartingRule(destRuleId);
			}
			else {
				if (connectorIndex==0) {
					sourceRule.data.onPass.action = "GOTO_RULES";
					if (!sourceRule.data.onPass.ruleId) {
						sourceRule.data.onPass.ruleId = [];
					}
					sourceRule.data.onPass.ruleId.push(destRuleId);
				}
				else if (connectorIndex==1) {
					sourceRule.data.onFail.action = "GOTO_RULES";
					if (!sourceRule.data.onFail.ruleId) {
						sourceRule.data.onFail.ruleId = [];
					}
					sourceRule.data.onFail.ruleId.push(destRuleId);
				}
			}
		};

		this.addStartingRule = function (destRuleId) {
			if (!this.data.query.startingRules.ruleId) {
				this.data.query.startingRules.ruleId = [];
			}
			this.data.query.startingRules.ruleId.push(destRuleId);
			//
			// Update the startingRules view model.
			//
			this.startingRules.push(new flowchart.StartingRulesViewModel(destRuleId));
		}

		//
		// Add a rule to the view model.
		//
		this.addRule = function (ruleDataModel) {
			if (!this.data.query.rule) {
				this.data.query.rule = [];
			}

			//
			// Update the query document data model.
			//
			this.data.query.rule.push(ruleDataModel);

			//
			// Update the rule view model.
			//
			this.rule.push(new flowchart.RuleViewModel(ruleDataModel));
		}

		this.clearQuery = function () {
			this.data.query.rule.length = 0;
			this.data.query.startingRules.ruleId.length = 0;
			this.rule.length = 0;
			this.startingRules.length = 0;

		}

		//
		// Deselect all rules in the chart.
		//
		this.deselectAll = function () {
			var rule = this.rule;
			for (var i = 0; i < rule.length; ++i) {
				var r = rule[i];
				r.deselect();
			}
		};

		//
		// Update the location of the rule and its connectors.
		//
		this.updateSelectedRuleLocation = function (deltaX, deltaY) {

			var selectedRule = this.getSelectedRule();

			selectedRule.data.layout.x += deltaX;
			selectedRule.data.layout.y += deltaY;
		};

		//
		// Handle mouse click on a particular rule.
		//
		this.handleRuleClicked = function (rule, ctrlKey) {

			if (ctrlKey) {
				rule.toggleSelected();
			}
			else {
				this.deselectAll();
				rule.select();
			}

			// Move rule to the end of the list so it is rendered after all the other.
			// This is the way Z-order is done in SVG.

			var ruleIndex = this.rule.indexOf(rule);
			if (ruleIndex == -1) {
				throw new Error("Failed to find rule in view model!");
			}
			this.rule.splice(ruleIndex, 1);
			this.rule.push(rule);
		};

		//
		// Delete all rules that are selected.
		//
		this.deleteSelected = function () {

			var newRuleViewModels = [];
			var newRuleDataModels = [];

			var deletedRuleIds = [];

			//
			// Sort rule into:
			//		rule to keep and
			//		rule to delete.
			//

			var selectedRule = this.getSelectedRule();
			var selectedRuleId = selectedRule.data.id;

			for (var ruleIndex = 0; ruleIndex < this.rule.length; ++ruleIndex) {
				var newNextRuleViewModels = [];

				var r = this.rule[ruleIndex];

				if (r.data.onPass.ruleId==null)
					continue;

				for (var nextRulesIndex = 0; nextRulesIndex < r.data.onPass.ruleId.length; ++nextRulesIndex) {
					var ruleId = r.data.onPass.ruleId[nextRulesIndex];
					if (selectedRuleId!=ruleId) {
						// Only retain non-selected GOTO_RULESs.
						newNextRuleViewModels.push(ruleId);
					}
				}

				r.data.onPass.ruleId.length = 0;
				r.data.onPass.ruleId.push.apply(r.data.onPass.ruleId, newNextRuleViewModels);
				if (r.data.onPass.ruleId.length==0) {
					r.data.onPass.action = "";
				}
			}

			for (var ruleIndex = 0; ruleIndex < this.rule.length; ++ruleIndex) {
				var newNextRuleViewModels = [];

				var r = this.rule[ruleIndex];

				if (r.data.onFail.ruleId==null)
					continue;

				for (var nextRulesIndex = 0; nextRulesIndex < r.data.onFail.ruleId.length; ++nextRulesIndex) {
					var ruleId = r.data.onFail.ruleId[nextRulesIndex];
					if (selectedRuleId!=ruleId) {
						// Only retain non-selected GOTO_RULESs.
						newNextRuleViewModels.push(ruleId);
					}
				}

				r.data.onFail.ruleId.length = 0;
				r.data.onFail.ruleId.push.apply(r.data.onFail.ruleId, newNextRuleViewModels);
				if (r.data.onFail.ruleId.length==0) {
					r.data.onFail.action = "";
				}
			}

			var newStartingRuleViewModels = [];
			var newStartingRuleDataModels = [];

			for (var startingRulesIndex = 0; startingRulesIndex < this.startingRules.length; ++startingRulesIndex) {
				var rule = this.startingRules[startingRulesIndex];
				var ruleId = rule.ruleId();

				if (selectedRuleId!=ruleId) {
					// Only retain non-selected starting rules.
					newStartingRuleViewModels.push(rule);
					newStartingRuleDataModels.push(rule.data);
				}
			}

			for (var ruleIndex = 0; ruleIndex < this.rule.length; ++ruleIndex) {
				var rule = this.rule[ruleIndex];
				if (!rule.selected()) {
					// Only retain non-selected rule.
					newRuleViewModels.push(rule);
					newRuleDataModels.push(rule.data);
				}
				else {
					deletedRuleIds.push(rule.data.id);
				}
			}

			//
			// Update rules
			//
			this.rule = newRuleViewModels;
			this.data.query.rule = newRuleDataModels;

			this.startingRules = newStartingRuleViewModels;
			this.data.query.startingRules.ruleId = newStartingRuleDataModels;
		};

		//
		// Get the array of rule that are currently selected.
		//
		this.getSelectedRule = function () {
			for (var i = 0; i < this.rule.length; ++i) {
				var rule = this.rule[i];
				if (rule.selected()) {
					break;
				}
			}

			return rule;
		};

	};

})();
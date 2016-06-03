/// <reference path="../../typings/tsd.d.ts" />
var flowchart;
var app;
(function (app) {
    var query;
    (function (query_1) {
        var TestEditorController = app.dialogs.TestEditorController;
        var ExpressionEditorController = app.dialogs.ExpressionEditorController;
        var QueryPickerController = app.dialogs.QueryPickerController;
        'use strict';
        var QueryController = (function () {
            function QueryController() {
            }
            return QueryController;
        })();
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
                replace: true,
                scope: true,
                link: function postLink(scope, element, attrs) {
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
                link: function postLink(scope, element, attrs) {
                    scope.title = attrs.title;
                }
            };
        })
            .controller('QueryController', ['LoggerService', '$scope', '$stateParams', '$uibModal', '$window', 'LibraryService', 'AdminService',
            function QueryController(logger, $scope, $stateParams, $modal, $window, libraryService, adminService) {
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
                        selectedRule.data.onPass.ruleId = [];
                    }
                };
                $scope.ruleFailActionChange = function () {
                    var selectedRule = $scope.chartViewModel.getSelectedRule();
                    selectedRule.data.onFail.action = $scope.ruleFailAction;
                    if ($scope.ruleFailAction != "GOTO_RULES") {
                        selectedRule.data.onFail.ruleId = [];
                    }
                };
                $scope.results = [
                    { value: 'GOTO_RULES', displayName: 'Go to rule' },
                    { value: 'INCLUDE', displayName: 'Include patient in final result' },
                    { value: 'NO_ACTION', displayName: 'No further action' }
                ];
                $scope.$on('editTest', function (event, ruleId) {
                    if (ruleId != "0") {
                        $scope.ruleId = ruleId;
                        var selectedRule = $scope.chartViewModel.getSelectedRule();
                        if (selectedRule.data.expression) {
                            var rules = [];
                            for (var i = 0; i < $scope.chartViewModel.data.query.rule.length; ++i) {
                                if ($scope.chartViewModel.data.query.rule[i].description != "START" &&
                                    !$scope.chartViewModel.data.query.rule[i].expression) {
                                    var rule = {
                                        value: $scope.chartViewModel.data.query.rule[i].id,
                                        displayName: $scope.chartViewModel.data.query.rule[i].description
                                    };
                                    rules.push(rule);
                                }
                            }
                            var expression = selectedRule.data.expression;
                            ExpressionEditorController.open($modal, expression, rules)
                                .result.then(function (resultData) {
                                selectedRule.data.expression = resultData;
                            });
                        }
                        else if (!selectedRule.data.queryLibraryItemUUID) {
                            var test = selectedRule.data.test;
                            var originalResultData = jQuery.extend(true, {}, test);
                            TestEditorController.open($modal, originalResultData, false)
                                .result.then(function (resultData) {
                                selectedRule.data.test = resultData;
                            });
                        }
                    }
                });
                $scope.$on('ruleDescription', function (event, description) {
                    if (description == "START") {
                        $scope.disableRuleProps = true;
                    }
                    else {
                        $scope.disableRuleProps = false;
                    }
                    $scope.ruleDescription = description;
                });
                $scope.$on('rulePassAction', function (event, action) {
                    $scope.rulePassAction = action;
                });
                $scope.$on('ruleFailAction', function (event, action) {
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
                $scope.addNewRule = function (mode) {
                    //
                    // Template for a new rule.
                    //
                    if ($scope.nextRuleID == 1) {
                        if (mode == 1 || mode == 3) {
                            this.createStartRule(-162, 25);
                            this.createNewRule(194, 5);
                        }
                        else if (mode == 2) {
                            var querySelection;
                            var vm = this;
                            QueryPickerController.open($modal, querySelection)
                                .result.then(function (resultData) {
                                vm.createStartRule(-162, 25);
                                vm.createNewQueryRule(194, 5, resultData);
                            });
                        }
                        $scope.chartViewModel.addStartingRule(1);
                    }
                    else {
                        switch (mode) {
                            case "1":
                                this.createNewRule(566, 7);
                                break;
                            case "2":
                                var querySelection;
                                var vm = this;
                                QueryPickerController.open($modal, querySelection)
                                    .result.then(function (resultData) {
                                    vm.createNewQueryRule(566, 7, resultData);
                                });
                                break;
                            case "3":
                                this.createNewExpression(566, 7);
                                break;
                        }
                    }
                };
                $scope.createStartRule = function (x, y) {
                    var newStartRuleDataModel = {
                        description: "START",
                        id: 0,
                        layout: {
                            x: x,
                            y: y
                        },
                        onPass: {
                            action: "",
                            ruleId: []
                        },
                        onFail: {
                            action: "",
                            ruleId: []
                        }
                    };
                    $scope.chartViewModel.addRule(newStartRuleDataModel);
                };
                $scope.createNewRule = function (x, y) {
                    var newRuleDataModel = {
                        description: "Rule Description",
                        id: $scope.nextRuleID++,
                        layout: {
                            x: x,
                            y: y
                        },
                        onPass: {
                            action: "INCLUDE",
                            ruleId: []
                        },
                        onFail: {
                            action: "NO_ACTION",
                            ruleId: []
                        }
                    };
                    $scope.chartViewModel.addRule(newRuleDataModel);
                };
                $scope.createNewExpression = function (x, y) {
                    var newExpressionRuleDataModel = {
                        description: "Expression Description",
                        id: $scope.nextRuleID++,
                        layout: {
                            x: x,
                            y: y
                        },
                        onPass: {
                            action: "INCLUDE",
                            ruleId: []
                        },
                        onFail: {
                            action: "NO_ACTION",
                            ruleId: []
                        },
                        expression: {
                            expressionText: "",
                            variable: []
                        }
                    };
                    $scope.chartViewModel.addRule(newExpressionRuleDataModel);
                };
                $scope.createNewQueryRule = function (x, y, resultData) {
                    var newQueryRuleDataModel = {
                        description: resultData.name + "~" + resultData.description,
                        id: $scope.nextRuleID++,
                        layout: {
                            x: x,
                            y: y
                        },
                        onPass: {
                            action: "INCLUDE",
                            ruleId: []
                        },
                        onFail: {
                            action: "NO_ACTION",
                            ruleId: []
                        },
                        queryLibraryItemUUID: resultData.id
                    };
                    $scope.chartViewModel.addRule(newQueryRuleDataModel);
                };
                //
                // Delete selected rule and connections.
                //
                $scope.deleteSelected = function () {
                    $scope.chartViewModel.deleteSelected();
                };
                $scope.save = function (close) {
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
                        .then(function (libraryItem) {
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
                                ruleId: []
                            },
                            onFail: {
                                action: "",
                                ruleId: []
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
                };
                //
                // Setup the data-model for the chart.
                //
                var startingRules = {
                    ruleId: []
                };
                var query = {
                    parentQueryUuid: null,
                    startingRules: startingRules,
                    rule: []
                };
                var libraryItem = {
                    uuid: null,
                    name: null,
                    description: null,
                    folderUuid: $stateParams.itemUuid,
                    query: query,
                    codeSet: null,
                    listReport: null,
                    protocol: null
                };
                //
                // Create the view-model for the chart and attach to the scope.
                //
                $scope.chartViewModel = new flowchart.ChartViewModel(libraryItem);
                switch ($stateParams.itemAction) {
                    case "view":
                    case "edit":
                        libraryService.getLibraryItem($stateParams.itemUuid)
                            .then(function (libraryItem) {
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
                                    ruleId: []
                                },
                                onFail: {
                                    action: "",
                                    ruleId: []
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
            }]);
    })(query = app.query || (app.query = {}));
})(app || (app = {}));
//# sourceMappingURL=query.controller.js.map
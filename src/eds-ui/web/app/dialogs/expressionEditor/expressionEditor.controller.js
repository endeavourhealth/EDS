/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var app;
(function (app) {
    var dialogs;
    (function (dialogs) {
        'use strict';
        var ExpressionEditorController = (function (_super) {
            __extends(ExpressionEditorController, _super);
            function ExpressionEditorController($uibModalInstance, logger, $modal, expression, rules) {
                _super.call(this, $uibModalInstance);
                this.$uibModalInstance = $uibModalInstance;
                this.logger = logger;
                this.$modal = $modal;
                this.expression = expression;
                this.expressionText = "";
                this.variableLabelDisabled = false;
                this.operatorDisabled = true;
                this.numberInPeriodDisabled = true;
                this.periodDisabled = true;
                this.editMode = false;
                this.sortorders = ['', 'ASCENDING', 'DESCENDING'];
                this.periods = ['DAYS', 'WEEKS', 'MONTHS', 'YEARS'];
                this.labels = ['A', 'B'];
                this.operators = ['+', '-', '=', '<', '>', '<=', '>=', 'AND'];
                this.rules = [];
                this.fields = ['', 'EFFECTIVE_DATE', 'TIMESTAMP', 'VALUE'];
                this.functions = ['', 'AVERAGE', 'COUNT', 'MINIMUM', 'MAXIMUM'];
                var vm = this;
                this.resultData = expression;
                vm.newExpression = {
                    expressionText: "",
                    variable: []
                };
                if (this.resultData.variable.length > 0)
                    this.initialiseEditMode(this.resultData);
                vm.rules = rules;
            }
            ExpressionEditorController.open = function ($modal, expression, rules) {
                var options = {
                    templateUrl: 'app/dialogs/expressionEditor/expressionEditor.html',
                    controller: 'ExpressionEditorController',
                    controllerAs: 'expressionEditor',
                    size: 'lg',
                    backdrop: 'static',
                    resolve: {
                        expression: function () { return expression; },
                        rules: function () { return rules; }
                    }
                };
                var dialog = $modal.open(options);
                return dialog;
            };
            ExpressionEditorController.prototype.initialiseEditMode = function (resultData) {
                var vm = this;
                vm.editMode = true;
                if (resultData.variable === null) {
                    resultData.variable = [];
                }
                vm.expressionText = resultData.expressionText;
                vm.expressionVariableName1 = resultData.variable[0].variableName;
                vm.expressionRuleId1 = resultData.variable[0].ruleId.toString();
                if (resultData.variable[0].restriction) {
                    vm.expressionRestrictionFieldName1 = resultData.variable[0].restriction.fieldName;
                    vm.expressionRestrictionOrderDirection1 = resultData.variable[0].restriction.orderDirection;
                    vm.expressionRestrictionCount1 = resultData.variable[0].restriction.count.toString();
                }
                vm.expressionTestField1 = resultData.variable[0].fieldName;
                vm.expressionFunction1 = resultData.variable[0].function;
                if (resultData.variable.length > 1) {
                    vm.expressionVariableName2 = resultData.variable[1].variableName;
                    vm.expressionRuleId2 = resultData.variable[1].ruleId.toString();
                    if (resultData.variable[1].restriction) {
                        vm.expressionRestrictionFieldName2 = resultData.variable[1].restriction.fieldName;
                        vm.expressionRestrictionOrderDirection2 = resultData.variable[1].restriction.orderDirection;
                        vm.expressionRestrictionCount2 = resultData.variable[1].restriction.count.toString();
                    }
                    vm.expressionTestField2 = resultData.variable[1].fieldName;
                    vm.expressionFunction2 = resultData.variable[1].function;
                }
            };
            ExpressionEditorController.prototype.variableLabelChange = function () {
                var vm = this;
                vm.expressionText = vm.expressionText + " " + vm.variableLabel;
                vm.variableLabelDisabled = true;
                vm.operatorDisabled = false;
                vm.numberInPeriodDisabled = true;
                vm.periodDisabled = true;
            };
            ExpressionEditorController.prototype.operatorChange = function () {
                var vm = this;
                vm.expressionText = vm.expressionText + " " + vm.operator;
                vm.variableLabelDisabled = false;
                vm.operatorDisabled = true;
                vm.numberInPeriodDisabled = false;
                vm.periodDisabled = true;
            };
            ExpressionEditorController.prototype.numberInPeriodChange = function () {
                var vm = this;
                vm.expressionText = vm.expressionText + " " + vm.numberInPeriod;
                vm.variableLabelDisabled = true;
                vm.operatorDisabled = false;
                vm.numberInPeriodDisabled = true;
                vm.periodDisabled = false;
            };
            ExpressionEditorController.prototype.periodChange = function () {
                var vm = this;
                vm.expressionText = vm.expressionText + " " + vm.period;
                vm.variableLabelDisabled = true;
                vm.operatorDisabled = false;
                vm.numberInPeriodDisabled = true;
                vm.periodDisabled = true;
            };
            ExpressionEditorController.prototype.backSpace = function () {
                var vm = this;
                if (vm.expressionText.substring(vm.expressionText.length - 1, vm.expressionText.length) == "S") {
                    vm.expressionText = vm.expressionText.replace(" DAYS", "");
                    vm.expressionText = vm.expressionText.replace(" WEEKS", "");
                    vm.expressionText = vm.expressionText.replace(" MONTHS", "");
                    vm.expressionText = vm.expressionText.replace(" YEARS", "");
                }
                else
                    vm.expressionText = vm.expressionText.substring(0, vm.expressionText.length - 2);
                vm.variableLabelDisabled = false;
                vm.operatorDisabled = false;
                vm.numberInPeriodDisabled = false;
                vm.periodDisabled = false;
            };
            ExpressionEditorController.prototype.save = function () {
                var vm = this;
                if (!vm.expressionRuleId1) {
                    this.logger.error('Please select a data source for variable A');
                    return;
                }
                if (!vm.expressionTestField1) {
                    this.logger.error('Please select a test field for variable A');
                    return;
                }
                if (!vm.expressionText) {
                    this.logger.error('Please enter an expression calculation');
                    return;
                }
                if (vm.expressionRuleId2) {
                    if (!vm.expressionTestField2) {
                        this.logger.error('Please select a test field for variable B');
                        return;
                    }
                }
                vm.resultData = vm.newExpression;
                vm.resultData.expressionText = vm.expressionText;
                if (vm.expressionFunction1 == "")
                    vm.expressionFunction1 = null;
                if (vm.expressionFunction2 == "")
                    vm.expressionFunction2 = null;
                var restriction = {
                    fieldName: vm.expressionRestrictionFieldName1,
                    orderDirection: vm.expressionRestrictionOrderDirection1,
                    count: Number(vm.expressionRestrictionCount1)
                };
                if (!vm.expressionRestrictionFieldName1 || vm.expressionRestrictionFieldName1 == "undefined" || vm.expressionRestrictionFieldName1 == "") {
                    restriction = null;
                }
                var variableType = {
                    variableName: "A",
                    ruleId: Number(vm.expressionRuleId1),
                    restriction: restriction,
                    fieldName: vm.expressionTestField1,
                    function: vm.expressionFunction1
                };
                vm.resultData.variable.push(variableType);
                var restriction = {
                    fieldName: vm.expressionRestrictionFieldName2,
                    orderDirection: vm.expressionRestrictionOrderDirection2,
                    count: Number(vm.expressionRestrictionCount2)
                };
                if (!vm.expressionRestrictionFieldName2 || vm.expressionRestrictionFieldName2 == "undefined" || vm.expressionRestrictionFieldName2 == "") {
                    restriction = null;
                }
                var variableType = {
                    variableName: "B",
                    ruleId: Number(vm.expressionRuleId2),
                    restriction: restriction,
                    fieldName: vm.expressionTestField2,
                    function: vm.expressionFunction2
                };
                if (vm.expressionRuleId2)
                    vm.resultData.variable.push(variableType);
                this.ok();
            };
            ExpressionEditorController.$inject = ['$uibModalInstance', 'LoggerService', '$uibModal', 'expression', 'rules'];
            return ExpressionEditorController;
        })(dialogs.BaseDialogController);
        dialogs.ExpressionEditorController = ExpressionEditorController;
        angular
            .module('app.dialogs')
            .controller('ExpressionEditorController', ExpressionEditorController);
    })(dialogs = app.dialogs || (app.dialogs = {}));
})(app || (app = {}));
//# sourceMappingURL=expressionEditor.controller.js.map
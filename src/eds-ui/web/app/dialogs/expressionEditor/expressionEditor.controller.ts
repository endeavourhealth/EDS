/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../blocks/logger.service.ts" />

module app.dialogs {
    import IModalServiceInstance = angular.ui.bootstrap.IModalServiceInstance;
    import IModalSettings = angular.ui.bootstrap.IModalSettings;
    import IModalService = angular.ui.bootstrap.IModalService;
    import ExpressionType = app.models.ExpressionType;
    import Restriction = app.models.Restriction;
    import VariableType = app.models.VariableType;

    'use strict';

    export class ExpressionEditorController extends BaseDialogController {
        expressionText: string = "";

        expressionVariableName1: string;
        expressionRuleId1: string;
        expressionRestrictionFieldName1: string;
        expressionRestrictionOrderDirection1: string;
        expressionRestrictionCount1: string;
        expressionTestField1: string;
        expressionFunction1: string;

        expressionVariableName2: string;
        expressionRuleId2: string;
        expressionRestrictionFieldName2: string;
        expressionRestrictionOrderDirection2: string;
        expressionRestrictionCount2: string;
        expressionTestField2: string;
        expressionFunction2: string;

        variableLabelDisabled: Boolean = false;
        operatorDisabled: Boolean = true;
        numberInPeriodDisabled: Boolean = true;
        periodDisabled: Boolean = true;

        newExpression : ExpressionType;

        editMode : boolean = false;

        variableLabel: string;
        operator: string;
        numberInPeriod: string;
        period: string;

        sortorders = ['','ASCENDING','DESCENDING'];
        periods = ['DAYS','WEEKS','MONTHS','YEARS'];
        labels = ['A','B'];
        operators = ['+','-','=','<','>','<=','>=','AND'];
        rules = <any>[];
        fields = ['','EFFECTIVE_DATE','TIMESTAMP','VALUE'];
        functions = ['','AVERAGE','COUNT','MINIMUM','MAXIMUM'];

        public static open($modal : IModalService, expression : ExpressionType, rules : any) : IModalServiceInstance {
            var options : IModalSettings = {
                templateUrl:'app/dialogs/expressionEditor/expressionEditor.html',
                controller:'ExpressionEditorController',
                controllerAs:'expressionEditor',
                size:'lg',
                backdrop: 'static',
                resolve:{
                    expression : () => expression,
                    rules : () => rules
                }
            };

            var dialog = $modal.open(options);
            return dialog;
        }

        static $inject = ['$uibModalInstance', 'LoggerService', '$uibModal', 'expression', 'rules'];

        constructor(protected $uibModalInstance : IModalServiceInstance,
                    private logger : app.blocks.ILoggerService,
                    private $modal : IModalService,
                    private expression: ExpressionType,
                     rules: any) {

            super($uibModalInstance);

            var vm = this;

            this.resultData = expression;

            vm.newExpression = {
                expressionText : "",
                variable : []
            };

            if (this.resultData.variable.length>0)
                this.initialiseEditMode(this.resultData);

            vm.rules = rules;

        }

        initialiseEditMode(resultData : ExpressionType) {
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

            if (resultData.variable.length>1) {
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

        }

        variableLabelChange() {
            var vm = this;

            vm.expressionText = vm.expressionText+" "+vm.variableLabel;

            vm.variableLabelDisabled = true;
            vm.operatorDisabled = false;
            vm.numberInPeriodDisabled = true;
            vm.periodDisabled = true;
        }

        operatorChange() {
            var vm = this;

            vm.expressionText = vm.expressionText+" "+vm.operator;

            vm.variableLabelDisabled = false;
            vm.operatorDisabled = true;
            vm.numberInPeriodDisabled = false;
            vm.periodDisabled = true;
        }

        numberInPeriodChange() {
            var vm = this;

            vm.expressionText = vm.expressionText+" "+vm.numberInPeriod;

            vm.variableLabelDisabled = true;
            vm.operatorDisabled = false;
            vm.numberInPeriodDisabled = true;
            vm.periodDisabled = false;
        }

        periodChange() {
            var vm = this;

            vm.expressionText = vm.expressionText+" "+vm.period;

            vm.variableLabelDisabled = true;
            vm.operatorDisabled = false;
            vm.numberInPeriodDisabled = true;
            vm.periodDisabled = true;
        }

        backSpace() {
            var vm = this;
            if (vm.expressionText.substring(vm.expressionText.length-1,vm.expressionText.length)=="S") {
                vm.expressionText = vm.expressionText.replace(" DAYS","");
                vm.expressionText = vm.expressionText.replace(" WEEKS","");
                vm.expressionText = vm.expressionText.replace(" MONTHS","");
                vm.expressionText = vm.expressionText.replace(" YEARS","");
            }
            else
                vm.expressionText = vm.expressionText.substring(0,vm.expressionText.length-2);

            vm.variableLabelDisabled = false;
            vm.operatorDisabled = false;
            vm.numberInPeriodDisabled = false;
            vm.periodDisabled = false;
        }

        save() {
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

            if (vm.expressionFunction1=="")
                vm.expressionFunction1 = null;

            if (vm.expressionFunction2=="")
                vm.expressionFunction2 = null;

            var restriction : Restriction = {
                fieldName : vm.expressionRestrictionFieldName1,
                orderDirection : vm.expressionRestrictionOrderDirection1,
                count : Number(vm.expressionRestrictionCount1)
            }

            if (!vm.expressionRestrictionFieldName1||vm.expressionRestrictionFieldName1=="undefined"||vm.expressionRestrictionFieldName1=="") {
                restriction = null;
            }

            var variableType : VariableType = {
                variableName: "A",
                ruleId: Number(vm.expressionRuleId1),
                restriction: restriction,
                fieldName: vm.expressionTestField1,
                function: vm.expressionFunction1
            };

            vm.resultData.variable.push(variableType);

            var restriction : Restriction = {
                fieldName : vm.expressionRestrictionFieldName2,
                orderDirection : vm.expressionRestrictionOrderDirection2,
                count : Number(vm.expressionRestrictionCount2)
            }

            if (!vm.expressionRestrictionFieldName2||vm.expressionRestrictionFieldName2=="undefined"||vm.expressionRestrictionFieldName2=="") {
                restriction = null;
            }

            var variableType : VariableType = {
                variableName: "B",
                ruleId: Number(vm.expressionRuleId2),
                restriction: restriction,
                fieldName: vm.expressionTestField2,
                function: vm.expressionFunction2
            };

            if (vm.expressionRuleId2)
                vm.resultData.variable.push(variableType);

            this.ok();
        }


    }

    angular
        .module('app.dialogs')
        .controller('ExpressionEditorController', ExpressionEditorController);
}

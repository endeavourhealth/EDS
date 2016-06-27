module app.models {
    'use strict';

    export class VariableType {
        variableName: string;
        ruleId: number;
        restriction: Restriction;
        fieldName: string;
        function: string;
    }

}
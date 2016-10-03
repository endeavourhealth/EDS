module app.models {
    'use strict';

    export class UICondition {
        status: string;
        performedBy: UIPractitioner;
        enteredBy: UIPractitioner;
        code: UICodeableConcept;
        period: UIPeriod;
        significance: UICode;
    }
}
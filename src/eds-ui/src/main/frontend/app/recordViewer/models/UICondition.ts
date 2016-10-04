module app.models {
    'use strict';

    export class UICondition extends UIResource {
        code: UICodeableConcept;
        onsetDate: Date;
        endDate: Date;
        hasEnded: Boolean;
        period: UIPeriod;
        significance: UICode;
        notes: string;

    }
}
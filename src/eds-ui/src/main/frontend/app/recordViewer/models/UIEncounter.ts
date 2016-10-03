module app.models {
    'use strict';

    export class UIEncounter extends UIResource {
        status: string;
        performedBy: UIPractitioner;
        enteredBy: UIPractitioner;
        reason: UICode[];
        period: UIPeriod;
    }
}
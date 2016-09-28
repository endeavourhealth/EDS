module app.models {
    'use strict';

    export class Encounter {
        status: string;
        performedBy: Practitioner;
        enteredBy: Practitioner;
        date: string;
        reason: Code2[];
    }
}
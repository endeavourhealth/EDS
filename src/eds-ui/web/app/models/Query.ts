module app.models {
    'use strict';

    export class Query {
        parentQueryUuid  : string;
        startingRules : StartingRules;
        rule : Rule[];

    }
}
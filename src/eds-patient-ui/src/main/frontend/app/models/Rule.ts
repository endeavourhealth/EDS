module app.models {
    'use strict';

    export class Rule {
        description : string;
        id : string;
        test : Test;
        testLibraryItemUUID : string;
        queryLibraryItemUUID : string;
        expression : ExpressionType;
        onPass : RuleAction;
        onFail : RuleAction;
        layout : LayoutType;
    }
}
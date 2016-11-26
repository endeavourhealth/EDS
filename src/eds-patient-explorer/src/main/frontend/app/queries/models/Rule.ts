import {RuleAction} from "./RuleAction";
import {ExpressionType} from "./ExpressionType";
import {Test} from "../../tests/models/Test";
import {LayoutType} from "./LayoutType";

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

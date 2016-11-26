import {CodeSetValue} from "../../codeSet/models/CodeSetValue";

export class ExclusionTreeNode {
	codeSetValue : CodeSetValue;
	children : ExclusionTreeNode[];
}

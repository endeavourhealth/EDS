import {CalculationType} from "./CalculationType";
import {FieldTest} from "../../tests/models/FieldTest";
import {Restriction} from "../../expressions/models/Restriction";

export class Resource {
		heading : string;
		resourceUuid : string[];
		calculation : CalculationType;
		filter : FieldTest[];
		restriction : Restriction;
	}

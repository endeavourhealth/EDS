	import {CalculationType} from "../../calculations/models/CalculationType";
	import {FieldTest} from "../../tests/models/FieldTest";
	import {Restriction} from "../../queries/models/Restriction";

	export class Resource {
		heading : string;
		resourceUuid : string[];
		calculation : CalculationType;
		filter : FieldTest[];
		restriction : Restriction;
	}

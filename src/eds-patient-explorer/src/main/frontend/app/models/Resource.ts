	import {CalculationType} from "./CalculationType";
	import {FieldTest} from "./FieldTest";
	import {Restriction} from "./Restriction";

	export class Resource {
		heading : string;
		resourceUuid : string[];
		calculation : CalculationType;
		filter : FieldTest[];
		restriction : Restriction;
	}

export class CodeSetValue {
	code : string;
	term? : string;
	includeChildren : boolean;
	exclusion : CodeSetValue[];
}

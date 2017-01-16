export class ReportParams {
	RunDate : { prompt : boolean, value? : string};
	SnomedCode : { prompt : boolean, value? : string};
	OriginalCode : { prompt : boolean, value? : string};
	ValueMin : { prompt : boolean, value? : string};
	ValueMax : { prompt : boolean, value? : string};

	constructor() {
		this.RunDate = { prompt : false };
		this.SnomedCode = { prompt : false };
		this.OriginalCode = { prompt : false };
		this.ValueMin = { prompt : false };
		this.ValueMax = { prompt : false };
	}
}
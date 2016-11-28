import {Component, Input} from "@angular/core";
import {UIProblem} from "../models/resources/clinical/UIProblem";

@Component({
	selector : 'problems',
	template : require('./problems.html')
})
export class ProblemsComponent {
	@Input() title : string;
	@Input() problems : UIProblem[];
	@Input() placeholder : string;
}
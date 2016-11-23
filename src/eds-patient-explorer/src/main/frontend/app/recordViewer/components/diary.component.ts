import {Component, Input} from "@angular/core";
import {UIDiary} from "../models/resources/clinical/UIDiary";
@Component({
	selector : 'diary',
	template : require('./diary.html')
})
export class DiaryComponent {
	@Input() diaries : UIDiary[];
}
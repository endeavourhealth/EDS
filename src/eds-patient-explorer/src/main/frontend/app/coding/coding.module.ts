import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {TreeModule} from "angular2-tree-component";
import {CodePickerDialog} from "./codePicker.dialog";
import {CodingService} from "./coding.service";
import {EkbCodingService} from "./ekb/ekbCoding.service";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		TreeModule,
	],
	declarations : [
		CodePickerDialog
	],
	entryComponents : [
		CodePickerDialog
	],
	providers : [
		{ provide : CodingService, useClass : EkbCodingService }
	],
})
export class CodingModule {}
import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {PractitionerPickerDialog} from "./practitionerPicker.dialog";
import {PractitionerService} from "./practitioner.service";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
	],
	declarations : [
		PractitionerPickerDialog
	],
	entryComponents : [
		PractitionerPickerDialog
	],
	providers : [
		PractitionerService
	],
})
export class PractitionerModule {}
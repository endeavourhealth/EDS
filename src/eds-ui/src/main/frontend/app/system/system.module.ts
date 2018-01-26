import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {SystemEditComponent} from "./systemEditor.component";
import {SystemService} from "./system.service";
import {SystemPickerDialog} from "./systemPicker.dialog";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule
	],
	declarations : [
		SystemEditComponent,
		SystemPickerDialog,
	],
	entryComponents : [
		SystemPickerDialog
	],
	providers : [
		SystemService
	]
})
export class SystemModule {}
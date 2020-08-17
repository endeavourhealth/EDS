import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {SystemEditComponent} from "./systemEditor.component";
import {SystemService} from "./system.service";
import {SystemPickerDialog} from "./systemPicker.dialog";
import {SystemListComponent} from "./systemList.component";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule
	],
	declarations : [
		SystemEditComponent,
		SystemListComponent,
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
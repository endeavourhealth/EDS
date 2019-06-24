import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {ServiceListComponent} from "./serviceList.component";
import {ServiceEditComponent} from "./serviceEditor.component";
import {ServicePickerDialog} from "./servicePicker.dialog";
import {ServiceService} from "./service.service";
import {OdsSearchDialog} from "./odsSearch.dialog";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		NgbModule,
	],
	declarations : [
		ServiceListComponent,
		ServiceEditComponent,
		ServicePickerDialog,
		OdsSearchDialog
	],
	entryComponents : [
		ServicePickerDialog,
		OdsSearchDialog
	],
	providers : [
		ServiceService
	]
})
export class ServicesModule {}
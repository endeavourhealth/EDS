import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {DataSetComponent} from "./dataSet.component";
import {DataSetEditComponent} from "./dataSetEditor.component";
import {DataSetService} from "./dataSet.service";
import {DataSetPickerDialog} from "./dataSetPicker.dialog";


@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
	],
	declarations : [
		DataSetComponent,
		DataSetEditComponent,
		DataSetPickerDialog,
	],
	entryComponents : [
		DataSetPickerDialog
	],
	providers : [
		DataSetService
	]
})
export class DataSetModule {}


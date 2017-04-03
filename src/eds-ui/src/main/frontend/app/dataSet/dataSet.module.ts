import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {DataSetEditComponent} from "./dataSetEditor.component";
import {DataSetService} from "./dataSet.service";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
	],
	declarations : [
		DataSetEditComponent,
	],
	providers : [
		DataSetService
	]
})
export class DataSetModule {}
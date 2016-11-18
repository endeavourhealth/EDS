import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";

import {DataSetEditComponent} from "./dataSetEditor.component";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
	],
	declarations : [
		DataSetEditComponent,
	]
})
export class DataSetModule {}
import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

import {FlowchartModule} from "../flowchart/flowchart.module";
import {FolderModule} from "../folder/folder.module";

import {QueryEditComponent} from "./queryEditor.component";
import {QueryPickerDialog} from "./queryPicker.dialog";
import {TestsModule} from "../tests/tests.module";
import {ExpressionsModule} from "../expressions/expressions.module";
import {DialogsModule} from "../dialogs/dialogs.module";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		NgbModule,
		FlowchartModule,
		FolderModule,
		DialogsModule,
		ExpressionsModule,
		TestsModule
	],
	declarations : [
		QueryEditComponent,
		QueryPickerDialog
	],
	entryComponents : [
		QueryPickerDialog
	]
})
export class QueryModule {}
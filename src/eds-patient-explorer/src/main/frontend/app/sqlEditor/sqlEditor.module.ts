import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NKDatetimeModule} from "ng2-datetime/ng2-datetime";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {SqlEditorService} from "./sqlEditor.service";
import {SqlEditorComponent} from "./sqlEditor.component";
import {UploadCsvDialog} from "./uploadCsv.dialog";

@NgModule({
	imports:[
		BrowserModule,
		FormsModule,
		NgbModule,
		NKDatetimeModule,
	],
	declarations:[
		SqlEditorComponent,
		UploadCsvDialog
	],
	entryComponents:[
		UploadCsvDialog
	],
	providers:[
		SqlEditorService
	]
})
export class SqlEditorModule {}
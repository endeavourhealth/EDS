import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NKDatetimeModule} from "ng2-datetime/ng2-datetime";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {CountReportComponent} from "./countReport.component";
import {FolderModule} from "../folder/folder.module";
import {LibraryModule} from "../library/library.module";
import {CountReportService} from "./countReport.service";
import {ReportParamsDialog} from "./reportParams.dialog";
import {CodingModule} from "../coding/coding.module";
import {PractitionerModule} from "../practitioner/practitioner.module";

@NgModule({
	imports:[
		BrowserModule,
		FormsModule,
		NgbModule,
		NKDatetimeModule,

		FolderModule,
		LibraryModule,
		CodingModule,
		PractitionerModule,
	],
	declarations:[
		CountReportComponent,
		ReportParamsDialog,
	],
	entryComponents:[
		ReportParamsDialog,
	],
	providers:[
		CountReportService
	]
})
export class CountReportModule {}
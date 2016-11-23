import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {RecordViewerComponent} from "./recordViewer.component";
import {PatientFindDialog} from "./patientFind.dialog";
import {RecordViewerService} from "./recordViewer.service";
import {CodeReadTerm, CodeReadCode, CodeSnomedCode, CodeSnomedLink, CodeSignificance} from "./pipes/coding";
import {
	CuiDate, CuiDateOfBirth, CuiName, CuiNhsNumber,
	CuiSingleLineAddress, CuiGender
} from "./pipes/cui";
import {Parentheses} from "./pipes/general";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {ProblemsComponent} from "./components/problems.component";
import {DiaryComponent} from "./components/diary.component";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		NgbModule
	],
	declarations : [
		RecordViewerComponent,
		PatientFindDialog,

		ProblemsComponent,
		DiaryComponent,

		CodeReadTerm,
		CodeReadCode,
		CodeSnomedCode,
		CodeSnomedLink,
		CodeSignificance,

		CuiDate,
		CuiDateOfBirth,
		CuiName,
		CuiNhsNumber,
		CuiSingleLineAddress,
		CuiGender,

		Parentheses
	],
	entryComponents : [
		PatientFindDialog
	],
	providers :
	[
		RecordViewerService
	],
	exports : [
		CodeReadTerm,
		CodeReadCode,
		CodeSnomedCode,
		CodeSnomedLink,
		CodeSignificance,

		CuiDate,
		CuiDateOfBirth,
		CuiName,
		CuiNhsNumber,
		CuiSingleLineAddress,
		CuiGender,

		Parentheses
	]
})
export class RecordViewerModule {}
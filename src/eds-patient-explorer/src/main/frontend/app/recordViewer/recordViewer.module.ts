import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {RecordViewerComponent} from "./recordViewer.component";
import {PatientFindDialog} from "./patientFind.dialog";
import {RecordViewerService} from "./recordViewer.service";
import {
	CodeReadTerm, CodeReadCode, CodeSnomedCode, CodeSnomedLink, CodeSignificance,
	CodeAnyTerm
} from "./pipes/coding";
import {
	CuiDate, CuiDateOfBirth, CuiName, CuiNhsNumber,
	CuiSingleLineAddress, CuiGender
} from "./pipes/cui";
import {Parentheses} from "./pipes/general";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {ProblemsComponent} from "./components/problems.component";
import {DiaryComponent} from "./components/diary.component";
import {PrecisComponent} from "./components/precis.component";
import {SummaryComponent} from "./components/summary.component";
import {ConsultationsComponent} from "./components/consultations.component";
import {MedicationComponent} from "./components/medication.component";
import {InvestigationsComponent} from "./components/investigations.component";
import {CareHistoryComponent} from "./components/careHistory.component";
import {TreeModule} from "angular2-tree-component";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		NgbModule,

		TreeModule,
	],
	declarations : [
		RecordViewerComponent,
		PatientFindDialog,

		PrecisComponent,
		SummaryComponent,
		ConsultationsComponent,
		MedicationComponent,
		ProblemsComponent,
		InvestigationsComponent,
		CareHistoryComponent,
		DiaryComponent,

		CodeAnyTerm,
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
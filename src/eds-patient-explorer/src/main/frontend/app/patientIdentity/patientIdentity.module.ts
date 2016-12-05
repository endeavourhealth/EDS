import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {PatientIdentityComponent} from "./patientIdentity.component";
import {PatientIdentityService} from "./patientIdentity.service";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		NgbModule,
	],
	declarations : [ PatientIdentityComponent ],
	providers : [ PatientIdentityService ]
})
export class PatientIdentityModule {}
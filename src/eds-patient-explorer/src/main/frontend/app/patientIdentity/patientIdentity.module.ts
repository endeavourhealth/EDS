import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {PatientIdentityComponent} from "./patientIdentity.component";
import {PatientIdentityService} from "./patientIdentity.service";
import {ServicesModule} from "../services/services.module";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		NgbModule,

		ServicesModule,
	],
	declarations : [ PatientIdentityComponent ],
	providers : [ PatientIdentityService ]
})
export class PatientIdentityModule {}
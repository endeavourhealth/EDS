import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {OrganisationListComponent} from "./organisationList.component";
import {OrganisationEditComponent} from "./organisationEditor.component";
import {OrganisationPickerDialog} from "./organisationPicker.dialog";
import {OrganisationService} from "./organisation.service";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		NgbModule,
	],
	declarations : [
		OrganisationListComponent,
		OrganisationEditComponent,
		OrganisationPickerDialog,
	],
	entryComponents : [
		OrganisationPickerDialog
	],
	providers : [
		OrganisationService
	]
})
export class OrganisationsModule {}
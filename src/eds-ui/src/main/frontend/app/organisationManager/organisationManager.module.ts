import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {OrganisationManagerComponent} from "./organisationManager.component";
import {OrganisationManagerEditorComponent} from "./organisationManagerEditor.component";
import {OrganisationManagerService} from "./organisationManager.service";
import {OrganisationManagerPickerDialog} from './organisationManagerPicker.dialog';

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule,
    ],
    declarations : [
        OrganisationManagerComponent,
        OrganisationManagerEditorComponent,
        OrganisationManagerPickerDialog
    ],
    entryComponents : [
        OrganisationManagerPickerDialog
    ],
    providers : [
        OrganisationManagerService
    ]
})
export class OrganisationManagerModule {}
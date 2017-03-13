import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {OrganisationManagerComponent} from "./organisationManager.component";
import {OrganisationManagerService} from "./organisationManager.service";

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule,
    ],
    declarations : [
        OrganisationManagerComponent
    ],
    entryComponents : [

    ],
    providers : [
        OrganisationManagerService
    ]
})
export class OrganisationManagerModule {}
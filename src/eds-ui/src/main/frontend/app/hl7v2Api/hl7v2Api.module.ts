import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {Hl7v2ApiService} from "./hl7v2Api.service";
import {Hl7v2ApiComponent} from "./hl7v2Api.component";

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule
    ],
    declarations : [
        Hl7v2ApiComponent
    ],
    providers : [
        Hl7v2ApiService
    ]
})
export class Hl7v2ApiModule {}
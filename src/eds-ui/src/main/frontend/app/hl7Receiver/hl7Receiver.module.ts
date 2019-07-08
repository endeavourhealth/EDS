import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {Hl7ReceiverComponent} from "./hl7Receiver.component";
import {Hl7ReceiverService} from "./hl7Receiver.service";

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule
    ],
    declarations : [
        Hl7ReceiverComponent
    ],
    providers : [
        Hl7ReceiverService
    ]
})
export class Hl7ReceiverModule {}
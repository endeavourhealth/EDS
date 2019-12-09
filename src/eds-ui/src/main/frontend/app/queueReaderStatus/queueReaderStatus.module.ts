import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {QueueReaderStatusService} from "./queueReaderStatus.service";
import {QueueReaderStatusComponent} from "./queueReaderStatus.component";

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule
    ],
    declarations : [
        QueueReaderStatusComponent
    ],
    providers : [
        QueueReaderStatusService
    ]
})
export class QueueReaderStatusModule {}
import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NKDatetimeModule} from "ng2-datetime/ng2-datetime";
import {RemoteFilingComponent} from "./remoteFiling.component";
import {RemoteFilingService} from "./remoteFiling.service";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {RemoteFilingFilesDialog} from "./remoteFilingFiles.dialog";

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NKDatetimeModule,
        NgbModule
    ],
    declarations : [
        RemoteFilingComponent,
        RemoteFilingFilesDialog
    ],
    entryComponents : [
        RemoteFilingFilesDialog
    ],
    providers : [
        RemoteFilingService
    ]
})
export class RemoteFilingModule {}
import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {SftpReaderComponent} from "./sftpReader.component";
import {SftpReaderService} from "./sftpReader.service";

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule
    ],
    declarations : [
        SftpReaderComponent
    ],
    providers : [
        SftpReaderService
    ]
})
export class SftpReaderModule {}
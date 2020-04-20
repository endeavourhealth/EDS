import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {ConfigManagerService} from "./configManager.service";
import {ConfigManagerComponent} from "./configManager.component";

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule
    ],
    declarations : [
        ConfigManagerComponent
    ],
    providers : [
        ConfigManagerService
    ]
})
export class ConfigManagerModule {}
import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {FrailtyApiComponent} from "./frailtyApi.component";
import {FrailtyApiService} from "./frailtyApi.service";

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule
    ],
    declarations : [
        FrailtyApiComponent
    ],
    providers : [
        FrailtyApiService
    ]
})
export class FrailtyApiModule {}
import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {SubscribersComponent} from "./subscribers.component";
import {SubscribersService} from "./subscribers.service";

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule
    ],
    declarations : [
        SubscribersComponent

    ],
    entryComponents : [

    ],
    providers : [
        SubscribersService
    ]
})
export class SubscribersModule {}
import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {SubscribersComponent} from "./subscribers.component";
import {SubscribersService} from "./subscribers.service";
import {SubscriberDetailComponent} from "./subscriberDetail.component";
import {PublisherStatusDialog} from "./publisherStatus.dialog";

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule
    ],
    declarations : [
        SubscribersComponent,
        SubscriberDetailComponent,
        PublisherStatusDialog
    ],
    entryComponents : [
        PublisherStatusDialog
    ],
    providers : [
        SubscribersService
    ]
})
export class SubscribersModule {}
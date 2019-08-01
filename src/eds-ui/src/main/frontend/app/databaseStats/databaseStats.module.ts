import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {DatabaseStatsService} from "./databaseStats.service";
import {DatabaseStatsComponent} from "./databaseStats.component";

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule
    ],
    declarations : [
        DatabaseStatsComponent
    ],
    providers : [
        DatabaseStatsService
    ]
})
export class DatabaseStatsModule {}
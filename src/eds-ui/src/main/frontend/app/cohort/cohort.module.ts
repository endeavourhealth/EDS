import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {CohortComponent} from "./cohort.component";
import {CohortEditorComponent} from "./cohortEditor.component";
import {CohortService} from "./cohort.service";

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule,
    ],
    declarations : [
        CohortComponent,
        CohortEditorComponent
    ],
    entryComponents : [
    ],
    providers : [
        CohortService
    ]
})
export class CohortModule {}
import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {DataSharingSummaryOverviewComponent} from "./dataSharingSummaryOverview.component";
import {DataSharingSummaryComponent} from "./dataSharingSummary.component";
import {DataSharingSummaryEditorComponent} from "./dataSharingSummaryEditor.component";
import {DataSharingSummaryService} from "./dataSharingSummary.service";
import {NKDatetimeModule} from "ng2-datetime/ng2-datetime";
import {DataSharingSummaryPickerDialog} from './dataSharingSummaryPicker.dialog'

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule,
        NKDatetimeModule
    ],
    declarations : [
        DataSharingSummaryComponent,
        DataSharingSummaryOverviewComponent,
        DataSharingSummaryEditorComponent,
        DataSharingSummaryPickerDialog
    ],
    entryComponents : [
        DataSharingSummaryPickerDialog
    ],
    providers : [
        DataSharingSummaryService
    ]
})
export class DataSharingSummaryModule {}
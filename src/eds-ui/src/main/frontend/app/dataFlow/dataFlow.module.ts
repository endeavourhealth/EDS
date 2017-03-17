import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {DataFlowComponent} from "./dataFlow.component";
import {DataFlowEditorComponent} from "./dataFlowEditor.component";
import {DataFlowService} from "./dataFlow.service";

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule,
    ],
    declarations : [
        DataFlowComponent,
        DataFlowEditorComponent
    ],
    entryComponents : [
    ],
    providers : [
        DataFlowService
    ]
})
export class DataFlowModule {}
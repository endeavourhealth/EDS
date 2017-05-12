import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {DataFlowComponent} from "./dataFlow.component";
import {DataFlowEditorComponent} from "./dataFlowEditor.component";
import {DataFlowService} from "./dataFlow.service";
import {DataFlowPickerDialog} from './dataFlowPicker.dialog';
import {EntityViewComponentsModule} from "eds-common-js";
import { AccordionModule } from 'ngx-bootstrap';

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule,
        EntityViewComponentsModule,
        AccordionModule.forRoot()
    ],
    declarations : [
        DataFlowComponent,
        DataFlowEditorComponent,
        DataFlowPickerDialog
    ],
    entryComponents : [
        DataFlowPickerDialog
    ],
    providers : [
        DataFlowService
    ]
})
export class DataFlowModule {}
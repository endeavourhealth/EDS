import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {DpaComponent} from "./dpa.component";
import {DpaEditorComponent} from "./dpaEditor.component";
import {DpaService} from "./dpa.service";
import {DpaPickerDialog} from './dpaPicker.dialog';
import {ControlsModule} from "eds-common-js";
import {EntityViewComponentsModule} from "eds-common-js";
import { AccordionModule } from 'ngx-bootstrap';
import { PdfViewerComponent } from 'ng2-pdf-viewer';

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule,
        ControlsModule,
        EntityViewComponentsModule,
        AccordionModule.forRoot()
    ],
    declarations : [
        DpaComponent,
        DpaEditorComponent,
        DpaPickerDialog,
        PdfViewerComponent
    ],
    entryComponents : [
        DpaPickerDialog
    ],
    providers : [
        DpaService
    ]
})
export class DpaModule {}
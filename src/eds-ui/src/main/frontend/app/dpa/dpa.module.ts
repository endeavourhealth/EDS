import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {DpaComponent} from "./dpa.component";
import {DpaEditorComponent} from "./dpaEditor.component";
import {DpaService} from "./dpa.service";
import {DpaPickerDialog} from './dpaPicker.dialog';

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule,
    ],
    declarations : [
        DpaComponent,
        DpaEditorComponent,
        DpaPickerDialog
    ],
    entryComponents : [
        DpaPickerDialog
    ],
    providers : [
        DpaService
    ]
})
export class DpaModule {}
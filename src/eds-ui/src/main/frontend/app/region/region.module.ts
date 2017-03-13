import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {RegionComponent} from "./region.component";
import {RegionEditorComponent} from "./regionEditor.component";
import {RegionService} from "./region.service";

@NgModule({
    imports : [
        BrowserModule,
        FormsModule,
        NgbModule,
    ],
    declarations : [
        RegionComponent,
        RegionEditorComponent
    ],
    entryComponents : [

    ],
    providers : [
        RegionService
    ]
})
export class RegionModule {}
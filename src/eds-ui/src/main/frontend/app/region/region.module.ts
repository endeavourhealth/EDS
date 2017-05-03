import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {RegionComponent} from "./region.component";
import {RegionEditorComponent} from "./regionEditor.component";
import {RegionService} from "./region.service";
import {RegionPickerDialog} from "./regionPicker.dialog";
import {AgmCoreModule, MapsAPILoader} from 'angular2-google-maps/core';
import {CustomLazyAPIKeyLoader} from "./CustomLazyAPIKeyLoader";

@NgModule({
    imports : [
        AgmCoreModule.forRoot(),
        BrowserModule,
        FormsModule,
        NgbModule
    ],
    declarations : [
        RegionComponent,
        RegionEditorComponent,
        RegionPickerDialog
    ],
    entryComponents : [
        RegionPickerDialog
    ],
    providers : [
        RegionService,
        {provide: MapsAPILoader, useClass: CustomLazyAPIKeyLoader }
    ]
})
export class RegionModule {}

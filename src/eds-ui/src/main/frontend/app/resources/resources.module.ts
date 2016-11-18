import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

import {ResourcesComponent} from "./resources.component";
import {ResourcesService} from "./resources.service";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		NgbModule,
	],
	declarations : [
		ResourcesComponent
	],
	providers : [
		ResourcesService
	]
})
export class ResourcesModule {}
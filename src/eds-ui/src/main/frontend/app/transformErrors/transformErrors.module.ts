import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {TransformErrorsComponent} from "./transformErrors.component";
import {TransformErrorsService} from "./transformErrors.service";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule
	],
	declarations : [
		TransformErrorsComponent
	],
	providers : [
		TransformErrorsService
	]
})
export class TransformErrorsModule {}
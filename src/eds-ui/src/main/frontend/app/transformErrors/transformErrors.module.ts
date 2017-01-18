import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {TransformErrorsComponent} from "./transformErrors.component";
//import {TransformErrorsService} from "./transformErrors.service";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		NgbModule
	],
	declarations : [
		TransformErrorsComponent
	]
	/*,
	providers : [
		TransformErrorsService
	]*/
})
export class TransformErrorsModule {}
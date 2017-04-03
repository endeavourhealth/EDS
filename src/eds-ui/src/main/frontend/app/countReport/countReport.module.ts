import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {CountReportEditComponent} from "./countReport.component";
import {CountReportService} from "./countReport.service";

@NgModule({
	imports:[
		BrowserModule,
		FormsModule
	],
	declarations:[
		CountReportEditComponent,
	],
	providers: [
		CountReportService
	]
})
export class CountReportModule {}
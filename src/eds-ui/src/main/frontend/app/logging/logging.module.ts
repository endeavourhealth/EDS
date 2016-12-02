import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {LogEntryDialog} from "./logEntry.dialog";
import {LoggingComponent} from "./logging.component";
import {LoggingService} from "./logging.service";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule
	],
	declarations : [
		LogEntryDialog,
		LoggingComponent,
	],
	entryComponents : [
		LogEntryDialog
	],
	providers : [
		LoggingService
	]
})
export class LoggingModule {}
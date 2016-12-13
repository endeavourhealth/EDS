import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NKDatetimeModule} from "ng2-datetime/ng2-datetime";
import {AuditComponent} from "./audit.component";
import {AuditService} from "./audit.service";
import {AuditEventDialog} from "./auditEvent.dialog";

@NgModule({
	imports:[
		BrowserModule,
		FormsModule,
		NKDatetimeModule,

	],
	declarations:[
		AuditComponent,
		AuditEventDialog,
	],
	entryComponents: [
		AuditEventDialog
	],
	providers:[
		AuditService
	]

})
export class AuditModule {}
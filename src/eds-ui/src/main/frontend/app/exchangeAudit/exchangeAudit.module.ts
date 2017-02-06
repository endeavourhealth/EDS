import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {ExchangeAuditComponent} from "./exchangeAudit.component";
import {ExchangeAuditService} from "./exchangeAudit.service";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {FormatBoolean} from "./format-boolean";
import {TransformErrorsDialog} from "./transformErrors.dialog";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		NgbModule
	],
	declarations : [
		ExchangeAuditComponent,
		FormatBoolean,
		TransformErrorsDialog
	],
	entryComponents : [
		TransformErrorsDialog
	],
	providers : [
		ExchangeAuditService
	]
})
export class ExchangeAuditModule {}
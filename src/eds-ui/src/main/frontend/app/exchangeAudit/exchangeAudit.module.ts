import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {ExchangeAuditComponent} from "./exchangeAudit.component";
import {ExchangeAuditService} from "./exchangeAudit.service";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		NgbModule
	],
	declarations : [
		ExchangeAuditComponent
	],
	providers : [
		ExchangeAuditService
	]
})
export class ExchangeAuditModule {}
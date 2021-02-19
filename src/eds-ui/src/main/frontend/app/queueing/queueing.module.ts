import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {QueueingListComponent} from "./queueingList.component";
import {QueueEditDialog} from "./queueingEditor.dialog";
import {RabbitService} from "./rabbit.service";
import {RoutingOverrideDialog} from "./routingOverride.dialog";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		NgbModule,
	],
	declarations : [
		QueueingListComponent,
		QueueEditDialog,
		RoutingOverrideDialog
	],
	entryComponents : [
		QueueEditDialog,
		RoutingOverrideDialog
	],
	providers : [
		RabbitService
	]
})
export class QueueingModule {}

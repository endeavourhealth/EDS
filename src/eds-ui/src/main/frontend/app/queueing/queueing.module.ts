import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {QueueingListComponent} from "./queueingList.component";
import {QueueEditDialog} from "./queueingEditor.dialog";
import {ConfigModule} from "../config/config.module";
import {RabbitService} from "./rabbit.service";

@NgModule({
	imports : [
		BrowserModule,
		FormsModule,
		NgbModule,
		ConfigModule,
	],
	declarations : [
		QueueingListComponent,
		QueueEditDialog,
	],
	entryComponents : [
		QueueEditDialog
	],
	providers : [
		RabbitService
	]
})
export class QueueingModule {}

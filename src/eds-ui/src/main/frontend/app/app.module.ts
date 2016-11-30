// Angular
import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {FormsModule} from '@angular/forms';
import {HttpModule, RequestOptions, XHRBackend, Http} from '@angular/http';
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {ToastModule, ToastOptions} from "ng2-toastr";
import {UIRouterModule, RootModule, UIView} from 'ui-router-ng2';
import {TreeModule} from "angular2-tree-component";

import {AuthHttpService} from "./security/authHttp.service";

// Routing states
import {states} from './app.states';

// Modules
import {MouseCaptureModule} from "./mouseCapture/mouseCapture.module";
import {FlowchartModule} from "./flowchart/flowchart.module";
import {CommonModule} from "./common/common.module";
import {DialogsModule} from "./dialogs/dialogs.module";

import {LayoutModule} from "./layout/layout.module";

import {DashboardModule} from "./dashboard/dashboard.module";
import {LibraryModule} from "./library/library.module";
import {OrganisationsModule} from "./organisations/organisations.module";
import {ServicesModule} from "./services/services.module";
import {QueueingModule} from "./queueing/queueing.module";
import {LoggingModule} from "./logging/logging.module";
import {TransformErrorsModule} from "./transformErrors/transformErrors.module";
import {StatsModule} from "./stats/stats.module";
import {AuditModule} from "./audit/audit.module";
import {AdminModule} from "./administration/admin.module";
import {UserModule} from "./users/user.module";
import {SystemModule} from "./system/system.module";
import {QueryModule} from "./query/query.module";
import {ProtocolModule} from "./protocol/protocol.module";
import {DataSetModule} from "./dataset/dataSet.module";
import {CodeSetModule} from "./codeSet/codeSet.module";


@NgModule({
	imports: [
		BrowserModule,
		FormsModule,
		HttpModule,
		TreeModule,
		NgbModule.forRoot(),
		ToastModule.forRoot(<ToastOptions>{animate: 'flyRight', positionClass: 'toast-bottom-right'}),
		UIRouterModule.forRoot(<RootModule>{ states: states, useHash: true, otherwise: { state: 'app.dashboard', params: {} } }),

		MouseCaptureModule,
		FlowchartModule,
		CommonModule,
		DialogsModule,

		LayoutModule,

		DashboardModule,
		LibraryModule,
		OrganisationsModule,
		ServicesModule,
		QueueingModule,
		LoggingModule,
		TransformErrorsModule,
		StatsModule,
		AuditModule,
		AdminModule,
		UserModule,
		SystemModule,
		QueryModule,
		ProtocolModule,
		DataSetModule,
		CodeSetModule,
	],
	providers: [
		{
			provide: Http,
			useFactory: (backend: XHRBackend, defaultOptions: RequestOptions) => new AuthHttpService(backend, defaultOptions),
			deps: [XHRBackend, RequestOptions]
		}
	],
	bootstrap: [ UIView ]
})

export class AppModule {}
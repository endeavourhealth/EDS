// Angular
import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {FormsModule} from '@angular/forms';
import {HttpModule, RequestOptions, XHRBackend, Http} from '@angular/http';
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {ToastModule, ToastOptions} from "ng2-toastr";
import {UIRouterModule, RootModule} from 'ui-router-ng2';
import {TreeModule} from "angular2-tree-component";

import {AuthHttpService} from "./security/authHttp.service";

// Routing states
import {states} from './app.states';

// Modules
import {CommonModule} from "./common/common.module";
import {DialogsModule} from "./dialogs/dialogs.module";

import {LayoutModule} from "./layout/layout.module";

import {PatientIdentityModule} from "./patientIdentity/patientIdentity.module";
import {RecordViewerModule} from "./recordViewer/recordViewer.module";
import {ResourcesModule} from "./resources/resources.module";

// Components
import {ShellComponent} from "./layout/shell.component";


@NgModule({
	imports: [
		BrowserModule,
		FormsModule,
		HttpModule,
		TreeModule,
		NgbModule.forRoot(),
		ToastModule.forRoot(<ToastOptions>{animate: 'flyRight', positionClass: 'toast-bottom-right'}),
		UIRouterModule.forRoot(<RootModule>{ states: states, useHash: true, otherwise:'/recordViewer' }),

		CommonModule,
		DialogsModule,

		LayoutModule,

		PatientIdentityModule,
		RecordViewerModule,
		ResourcesModule,
	],
	providers: [
		{
			provide: Http,
			useFactory: (backend: XHRBackend, defaultOptions: RequestOptions) => new AuthHttpService(backend, defaultOptions),
			deps: [XHRBackend, RequestOptions]
		}
	],
	bootstrap: [ ShellComponent ]
})

export class AppModule {}
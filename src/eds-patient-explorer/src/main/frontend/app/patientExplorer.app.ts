// Core
import {NgModule} from '@angular/core';
import {Application} from "./application";

// Modules
import {ResourcesModule} from "./resources/resources.module";
import {RecordViewerModule} from "./recordViewer/recordViewer.module";
import {PatientIdentityModule} from "./patientIdentity/patientIdentity.module";
import {CountReportModule} from "./countReport/countReport.module";

// State components
import {RecordViewerComponent} from "./recordViewer/recordViewer.component";
import {PatientIdentityComponent} from "./patientIdentity/patientIdentity.component";
import {ResourcesComponent} from "./resources/resources.component";
import {CountReportComponent} from "./countReport/countReport.component";

@NgModule(
	Application.Define({
		modules: [
			PatientIdentityModule,
			RecordViewerModule,
			ResourcesModule,
			CountReportModule,
		],
		states: [
			{name: 'app.resourceList', url: '/resourceList', component: ResourcesComponent },
			{name: 'app.resourceEdit', url: '/resourceEdit/:itemAction/:itemUuid', component: ResourcesComponent },
			{name: 'app.patientIdentity', url: '/patientIdentity', component : PatientIdentityComponent},
			{name: 'app.recordViewer', url: '/recordViewer', component : RecordViewerComponent },
			{name: 'app.countReports', url: '/countReports', component : CountReportComponent }
		],
		defaultState : { state: 'app.recordViewer', params: {} }
	})
)
export class AppModule {}

Application.Run(AppModule);
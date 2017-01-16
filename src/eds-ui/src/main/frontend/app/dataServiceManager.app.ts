// Core
import {NgModule} from "@angular/core";
import {Application} from "./application";
// Modules
import {FlowchartModule} from "./flowchart/flowchart.module";
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
import {DataSetModule} from "./dataSet/dataSet.module";
import {CodeSetModule} from "./codeSet/codeSet.module";
import {CountReportModule} from "./countReport/countReport.module";
import {ExchangeAuditModule} from "./exchangeAudit/exchangeAudit.module";
// State components
import {DashboardComponent} from "./dashboard/dashboard.component";
import {OrganisationListComponent} from "./organisations/organisationList.component";
import {OrganisationEditComponent} from "./organisations/organisationEditor.component";
import {ServiceListComponent} from "./services/serviceList.component";
import {ServiceEditComponent} from "./services/serviceEditor.component";
import {UserListComponent} from "./users/userList.component";
import {AuditComponent} from "./audit/audit.component";
import {LoggingComponent} from "./logging/logging.component";
import {QueueingListComponent} from "./queueing/queueingList.component";
import {StatsComponent} from "./stats/stats.component";
import {LibraryComponent} from "./library/library.component";
import {SystemEditComponent} from "./system/systemEditor.component";
import {QueryEditComponent} from "./query/queryEditor.component";
import {ProtocolEditComponent} from "./protocol/protocolEditor.component";
import {DataSetEditComponent} from "./dataSet/dataSetEditor.component";
import {CodeSetEditComponent} from "./codeSet/codeSetEditor.component";
import {TransformErrorsComponent} from "./transformErrors/transformErrors.component";
import {CountReportEditComponent} from "./countReport/countReport.component";
import {ExchangeAuditComponent} from "./exchangeAudit/exchangeAudit.component";

@NgModule(
	Application.Define({
		modules: [
			FlowchartModule,

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
			CountReportModule,
			ExchangeAuditModule
		],
		states: [
			{name: 'app.dashboard', url: '/dashboard', component: DashboardComponent},
			{name: 'app.organisationList', url: '/organisationList', component: OrganisationListComponent},
			{name: 'app.organisationEdit',url: '/organisationEdit/:itemAction/:itemUuid',component: OrganisationEditComponent},
			{name: 'app.serviceList', url: '/serviceList', component: ServiceListComponent},
			{name: 'app.serviceEdit', url: '/serviceEdit/:itemAction/:itemUuid', component: ServiceEditComponent},
			{name: 'app.admin', url: '/admin', component: UserListComponent},
			{name: 'app.audit', url: '/audit', component: AuditComponent},
			{name: 'app.monitoring', url: '/monitoring', component: LoggingComponent},
			{name: 'app.queueing', url: '/queueing', component: QueueingListComponent},
			{name: 'app.stats', url: '/stats', component: StatsComponent},
			{name: 'app.library', url: '/library', component: LibraryComponent},
			{name: 'app.systemEdit', url: '/systemEdit/:itemAction/:itemUuid', component: SystemEditComponent},
			{name: 'app.queryEdit', url: '/queryEdit/:itemAction/:itemUuid', component: QueryEditComponent},
			{name: 'app.protocolEdit', url: '/protocolEdit/:itemAction/:itemUuid', component: ProtocolEditComponent},
			{name: 'app.dataSetEdit', url: '/dataSetEdit/:itemAction/:itemUuid', component: DataSetEditComponent},
			{name: 'app.codeSetEdit', url: '/codeSetEdit/:itemAction/:itemUuid', component: CodeSetEditComponent},
			{name: 'app.transformErrors', url: '/transformErrors', component: TransformErrorsComponent},
			{name: 'app.exchangeAudit', url: '/exchangeAudit/:serviceUuid', component: ExchangeAuditComponent},
			{name: 'app.countReportEdit', url: '/countReportEdit/:itemAction/:itemUuid', component: CountReportEditComponent}
		],
		defaultState : { state: 'app.dashboard', params: {} }
	})
)
export class AppModule {}

Application.Run(AppModule);
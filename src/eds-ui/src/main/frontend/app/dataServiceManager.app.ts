// Styling
import "../content/css/index.css";
import "../content/less/index.less";
// Core
import {NgModule} from "@angular/core";
// Modules
import {FlowchartModule} from "./flowchart/flowchart.module";
import {DashboardModule} from "./dashboard/dashboard.module";
import {EdsLibraryModule} from "./edsLibrary/library.module";
import {OrganisationsModule} from "./organisations/organisations.module";
import {ServicesModule} from "./services/services.module";
import {QueueingModule} from "./queueing/queueing.module";
import {LoggingModule} from "./logging/logging.module";
import {TransformErrorsModule} from "./transformErrors/transformErrors.module";
import {StatsModule} from "./stats/stats.module";
import {AuditModule} from "./audit/audit.module";
import {UserModule} from "./users/user.module";
import {SystemModule} from "./system/system.module";
import {QueryModule} from "./query/query.module";
import {ProtocolModule} from "./protocol/protocol.module";
import {DataSetModule} from "./dataSet/dataSet.module";
import {CodeSetModule} from "./codeSet/codeSet.module";
import {CountReportModule} from "./countReport/countReport.module";
import {ExchangeAuditModule} from "./exchangeAudit/exchangeAudit.module";
import {OrganisationManagerModule} from "./organisationManager/organisationManager.module";
import {RegionModule} from "./region/region.module";
import {CohortModule} from "./cohort/cohort.module";
import {DataFlowModule} from "./dataFlow/dataFlow.module";
import {DsaModule} from "./dsa/dsa.module";
import {DpaModule} from "./dpa/dpa.module";
import {DataSharingSummaryModule} from "./dataSharingSummary/dataSharingSummary.module";
import {PaginationModule} from './pagination/pagination.module';
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
import {LibraryComponent} from "./edsLibrary/library.component";
import {SystemEditComponent} from "./system/systemEditor.component";
import {QueryEditComponent} from "./query/queryEditor.component";
import {ProtocolEditComponent} from "./protocol/protocolEditor.component";
import {DataSetEditComponent} from "./dataSet/dataSetEditor.component";
import {CodeSetEditComponent} from "./codeSet/codeSetEditor.component";
import {TransformErrorsComponent} from "./transformErrors/transformErrors.component";
import {CountReportEditComponent} from "./countReport/countReport.component";
import {ExchangeAuditComponent} from "./exchangeAudit/exchangeAudit.component";
import {OrganisationManagerComponent} from "./organisationManager/organisationManager.component";
import {OrganisationManagerEditorComponent} from "./organisationManager/organisationManagerEditor.component";
import {OrganisationManagerOverviewComponent} from "./organisationManager/organisationManagerOverview.component";
import {RegionComponent} from "./region/region.component";
import {RegionEditorComponent} from "./region/regionEditor.component";
import {CohortComponent} from "./cohort/cohort.component";
import {CohortEditorComponent} from "./cohort/cohortEditor.component";
import {DataFlowComponent} from "./dataFlow/dataFlow.component";
import {DataFlowEditorComponent} from "./dataFlow/dataFlowEditor.component";
import {DsaComponent} from "./dsa/dsa.component";
import {DsaEditorComponent} from "./dsa/dsaEditor.component";
import {DpaComponent} from "./dpa/dpa.component";
import {DpaEditorComponent} from "./dpa/dpaEditor.component";
import {DataSharingSummaryOverviewComponent} from "./dataSharingSummary/dataSharingSummaryOverview.component";
import {DataSharingSummaryComponent} from "./dataSharingSummary/dataSharingSummary.component";
import {DataSharingSummaryEditorComponent} from "./dataSharingSummary/dataSharingSummaryEditor.component";
import {Application, AdminModule} from "eds-common-js";
import {DataServiceManagerMenuService} from "./dataServiceManager.menu";

@NgModule(
	Application.Define({
		modules: [
			FlowchartModule,

			DashboardModule,
			EdsLibraryModule,
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
			ExchangeAuditModule,
			OrganisationManagerModule,
			RegionModule,
			CohortModule,
			DataFlowModule,
			DsaModule,
			DpaModule,
			DataSharingSummaryModule,
			PaginationModule
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
			{name: 'app.countReportEdit', url: '/countReportEdit/:itemAction/:itemUuid', component: CountReportEditComponent},
			{name: 'app.organisationManagerOverview', url: '/organisationManager/overview', component: OrganisationManagerOverviewComponent},
			{name: 'app.organisationManager', url: '/organisationManager/:mode', component: OrganisationManagerComponent},
			{name: 'app.organisationManagerEditor', url: '/organisationManager/:itemAction/:itemUuid', component: OrganisationManagerEditorComponent},
			{name: 'app.region', url: '/region', component: RegionComponent},
			{name: 'app.regionEditor', url: '/region/:itemAction/:itemUuid', component: RegionEditorComponent},
			{name: 'app.cohort', url: '/cohort', component: CohortComponent},
			{name: 'app.cohortEditor', url: '/cohort/:itemAction/:itemUuid', component: CohortEditorComponent},
			{name: 'app.dataFlow', url: '/dataFlow', component: DataFlowComponent},
			{name: 'app.dataFlowEditor', url: '/dataFlow/:itemAction/:itemUuid', component: DataFlowEditorComponent},
			{name: 'app.dsa', url: '/dsa', component: DsaComponent},
			{name: 'app.dsaEditor', url: '/dsa/:itemAction/:itemUuid', component: DsaEditorComponent},
			{name: 'app.dpa', url: '/dpa', component: DpaComponent},
			{name: 'app.dpaEditor', url: '/dpa/:itemAction/:itemUuid', component: DpaEditorComponent},
			{name: 'app.dataSharingSummaryOverview', url: '/dataSharingSummary/overview', component: DataSharingSummaryOverviewComponent},
			{name: 'app.dataSharingSummary', url: '/dataSharingSummary', component: DataSharingSummaryComponent},
			{name: 'app.dataSharingSummaryEditor', url: '/dataSharingSummary/:itemAction/:itemUuid', component: DataSharingSummaryEditorComponent}
		],
		defaultState : { state: 'app.dashboard', params: {} },
		menuManager : DataServiceManagerMenuService
	})
)
export class AppModule {}

Application.Run(AppModule);
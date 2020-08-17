// Styling
import "../content/css/index.css";
import "../content/less/index.less";
// Core
import {NgModule} from "@angular/core";
// Modules
import {EdsLibraryModule} from "./edsLibrary/library.module";
import {ServicesModule} from "./services/services.module";
import {QueueingModule} from "./queueing/queueing.module";
import {LoggingModule} from "./logging/logging.module";
import {TransformErrorsModule} from "./transformErrors/transformErrors.module";
import {StatsModule} from "./stats/stats.module";
import {AuditModule} from "./audit/audit.module";
import {UserModule} from "./users/user.module";
import {SystemModule} from "./system/system.module";
import {QueryModule} from "./query/query.module";
import {CodeSetModule} from "./codeSet/codeSet.module";
import {CountReportModule} from "./countReport/countReport.module";
import {ExchangeAuditModule} from "./exchangeAudit/exchangeAudit.module";
import {DataSetModule} from "./dataSet/dataSet.module";
import {RemoteFilingModule} from "./remoteFiling/remoteFiling.module";
// State components
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
import {CodeSetEditComponent} from "./codeSet/codeSetEditor.component";
import {TransformErrorsComponent} from "./transformErrors/transformErrors.component";
import {CountReportEditComponent} from "./countReport/countReport.component";
import {ExchangeAuditComponent} from "./exchangeAudit/exchangeAudit.component";
import {Application, AdminModule, LoggerModule} from "eds-common-js";
import {DataServiceManagerMenuService} from "./dataServiceManager.menu";
import {DataSetEditComponent} from "./dataSet/dataSetEditor.component";
import {RemoteFilingComponent} from "./remoteFiling/remoteFiling.component";
import {Hl7ReceiverComponent} from "./hl7Receiver/hl7Receiver.component";
import {Hl7ReceiverModule} from "./hl7Receiver/hl7Receiver.module";
import {FrailtyApiComponent} from "./frailtyApi/frailtyApi.component";
import {FrailtyApiModule} from "./frailtyApi/frailtyApi.module";
import {SftpReaderComponent} from "./sftpReader/sftpReader.component";
import {SftpReaderModule} from "./sftpReader/sftpReader.module";
import {DatabaseStatsModule} from "./databaseStats/databaseStats.module";
import {DatabaseStatsComponent} from "./databaseStats/databaseStats.component";
import {QueueReaderStatusModule} from "./queueReaderStatus/queueReaderStatus.module";
import {QueueReaderStatusComponent} from "./queueReaderStatus/queueReaderStatus.component";
import {ConfigManagerModule} from "./configManager/configManager.module";
import {ConfigManagerComponent} from "./configManager/configManager.component";
import {SystemListComponent} from "./system/systemList.component";

@NgModule(
	Application.Define({
		modules: [
			AdminModule,
			LoggerModule,
			EdsLibraryModule,
			ServicesModule,
			QueueingModule,
			LoggingModule,
			TransformErrorsModule,
			StatsModule,
			AuditModule,
			DataSetModule,
			UserModule,
			SystemModule,
			QueryModule,
			//ProtocolModule,
			CodeSetModule,
			CountReportModule,
			ExchangeAuditModule,
			RemoteFilingModule,
			Hl7ReceiverModule,
			FrailtyApiModule,
			SftpReaderModule,
			DatabaseStatsModule,
			QueueReaderStatusModule,
			ConfigManagerModule
		],
		states: [
			{name: 'app.systemList', url: '/systemList', component: SystemListComponent},
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
			/*{name: 'app.protocolEdit', url: '/protocolEdit/:itemAction/:itemUuid', component: ProtocolEditComponent},*/
			{name: 'app.dataSetEdit', url: '/dataSetEdit/:itemAction/:itemUuid', component: DataSetEditComponent},
			{name: 'app.codeSetEdit', url: '/codeSetEdit/:itemAction/:itemUuid', component: CodeSetEditComponent},
			{name: 'app.transformErrors', url: '/transformErrors', component: TransformErrorsComponent},
			{name: 'app.exchangeAudit', url: '/exchangeAudit/:serviceId/:systemId', component: ExchangeAuditComponent},
			{name: 'app.countReportEdit', url: '/countReportEdit/:itemAction/:itemUuid', component: CountReportEditComponent},
            {name: 'app.remoteFiling', url: '/remoteFiling', component: RemoteFilingComponent},
			{name: 'app.hl7Receiver', url: '/hl7Receiver', component: Hl7ReceiverComponent},
			{name: 'app.frailtyApi', url: '/frailtyApi', component: FrailtyApiComponent},
			{name: 'app.sftpReader', url: '/sftpReader', component: SftpReaderComponent},
			{name: 'app.databaseStats', url: '/databaseStats', component: DatabaseStatsComponent},
			{name: 'app.queueReaderStatus', url: '/queueReaderStatus', component: QueueReaderStatusComponent},
			{name: 'app.configManager', url: '/configManager', component: ConfigManagerComponent}
		],
		defaultState : { state: 'app.queueReaderStatus', params: {} },
		menuManager : DataServiceManagerMenuService
	})
)
export class AppModule {}

Application.Run(AppModule);
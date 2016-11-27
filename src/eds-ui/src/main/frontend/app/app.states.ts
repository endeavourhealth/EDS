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
import {DataSetEditComponent} from "./dataset/dataSetEditor.component";
import {CodeSetEditComponent} from "./codeSet/codeSetEditor.component";
import {TransformErrorsComponent} from "./transformErrors/transformErrors.component";
import {ShellComponent} from "./layout/shell.component";

let shellState = { name: 'app', url: '/app', component: ShellComponent };
let dashboardState = {name: 'app.dashboard', url: '/app/dashboard', component: DashboardComponent };
let organisationListState = {name: 'app.organisationList', url: '/app/organisationList', component: OrganisationListComponent };
let organisationEditState = {name: 'app.organisationEdit', url: '/app/organisationEdit/:itemAction/:itemUuid', component: OrganisationEditComponent };
let serviceListState = {name: 'app.serviceList', url: '/app/serviceList', component: ServiceListComponent };
let serviceEditState = {name: 'app.serviceEdit', url: '/app/serviceEdit/:itemAction/:itemUuid', component: ServiceEditComponent };
let adminState = {name: 'app.admin', url: '/app/admin', component: UserListComponent };
let auditState = {name: 'app.audit', url: '/app/audit', component: AuditComponent };
let loggingState = {name: 'app.monitoring', url: '/app/monitoring', component: LoggingComponent };
let queueingState = {name: 'app.queueing', url: '/app/queueing', component: QueueingListComponent };
let statsState = {name: 'app.stats', url: '/app/stats', component: StatsComponent };
let libraryState = {name: 'app.library', url: '/app/library', component: LibraryComponent };
let systemEditState = {name: 'app.systemEdit', url: '/app/systemEdit/:itemAction/:itemUuid', component: SystemEditComponent };
let queryEditState = {name: 'app.queryEdit', url: '/app/queryEdit/:itemAction/:itemUuid', component: QueryEditComponent };
let protocolEditState = {name: 'app.protocolEdit', url: '/app/protocolEdit/:itemAction/:itemUuid', component: ProtocolEditComponent };
let dataSetEditState = {name: 'app.dataSetEdit', url: '/app/dataSetEdit/:itemAction/:itemUuid', component: DataSetEditComponent };
let codeSetEditState = {name: 'app.codeSetEdit', url: '/app/codeSetEdit/:itemAction/:itemUuid', component: CodeSetEditComponent };
let transformErrorsState = {name: 'app.transformErrors', url: '/app/transformErrors', component: TransformErrorsComponent };

export const states = [
	shellState,
	dashboardState,
	organisationListState,
	organisationEditState,
	serviceListState,
	serviceEditState,
	adminState,
	auditState,
	loggingState,
	queueingState,
	statsState,
	libraryState,
	systemEditState,
	queryEditState,
	protocolEditState,
	dataSetEditState,
	codeSetEditState,
	transformErrorsState,
];

import {DashboardComponent} from "./dashboard/dashboard.component";
import {OrganisationListComponent} from "./organisations/organisationList.component";
import {OrganisationEditComponent} from "./organisations/organisationEditor.component";
import {ServiceListComponent} from "./services/serviceList.component";
import {ServiceEditComponent} from "./services/serviceEditor.component";
import {AdminComponent} from "./administration/admin.component";
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
import {ResourcesComponent} from "./resources/resources.component";
import {PatientIdentityComponent} from "./patientIdentity/patientIdentity.component";
import {RecordViewerComponent} from "./recordViewer/recordViewer.component";


let dashboardState = {name: 'dashboard', url: '/dashboard', component: DashboardComponent };
let organisationListState = {name: 'organisationList', url: '/organisationList', component: OrganisationListComponent };
let organisationEditState = {name: 'organisationEdit', url: '/organisationEdit/:itemAction/:itemUuid', component: OrganisationEditComponent };
let serviceListState = {name: 'serviceList', url: '/serviceList', component: ServiceListComponent };
let serviceEditState = {name: 'serviceEdit', url: '/serviceEdit/:itemAction/:itemUuid', component: ServiceEditComponent };
let adminState = {name: 'admin', url: '/admin', component: AdminComponent };
let auditState = {name: 'audit', url: '/audit', component: AuditComponent };
let loggingState = {name: 'monitoring', url: '/monitoring', component: LoggingComponent };
let queueingState = {name: 'queueing', url: '/queueing', component: QueueingListComponent };
let statsState = {name: 'stats', url: '/stats', component: StatsComponent };
let libraryState = {name: 'library', url: '/library', component: LibraryComponent };
let systemEditState = {name: 'systemEdit', url: '/systemEdit/:itemAction/:itemUuid', component: SystemEditComponent };
let queryEditState = {name: 'queryEdit', url: '/queryEdit/:itemAction/:itemUuid', component: QueryEditComponent };
let protocolEditState = {name: 'protocolEdit', url: '/protocolEdit/:itemAction/:itemUuid', component: ProtocolEditComponent };
let dataSetEditState = {name: 'dataSetEdit', url: 'dataSetEdit/:itemAction/:itemUuid', component: DataSetEditComponent };
let codeSetEditState = {name: 'codeSetEdit', url: 'codeSetEdit/:itemAction/:itemUuid', component: CodeSetEditComponent };
let transformErrorsState = {name: 'transformErrors', url: 'transformErrors', component: TransformErrorsComponent };
let resourceListState = {name: 'resourceList', url: 'resourceList', component: ResourcesComponent };
let resourceEditState = {name: 'resourceEdit', url: 'resourceEdit/:itemAction/:itemUuid', component: ResourcesComponent };
let patientIdentityState = { name: 'patientIdentity', url: 'patientIdentity', component : PatientIdentityComponent };
let recordViewerState = {name : 'recordViewer', url: 'recordViewer', component : RecordViewerComponent };

export const states = [
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
	resourceListState,
	resourceEditState,
	patientIdentityState,
	recordViewerState,
];

import {ResourcesComponent} from "./resources/resources.component";
import {PatientIdentityComponent} from "./patientIdentity/patientIdentity.component";
import {RecordViewerComponent} from "./recordViewer/recordViewer.component";
import {ShellComponent} from "./layout/shell.component";

let shellState = {name : 'app', url: '/app', component: ShellComponent };
let resourceListState = {name: 'app.resourceList', url: '/resourceList', component: ResourcesComponent };
let resourceEditState = {name: 'app.resourceEdit', url: '/resourceEdit/:itemAction/:itemUuid', component: ResourcesComponent };
let patientIdentityState = { name: 'app.patientIdentity', url: '/patientIdentity', component : PatientIdentityComponent };
let recordViewerState = {name : 'app.recordViewer', url: '/recordViewer', component : RecordViewerComponent };

export const states = [
	shellState,
	resourceListState,
	resourceEditState,
	patientIdentityState,
	recordViewerState,
];

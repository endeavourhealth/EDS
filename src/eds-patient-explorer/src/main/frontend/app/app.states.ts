import {ResourcesComponent} from "./resources/resources.component";
import {PatientIdentityComponent} from "./patientIdentity/patientIdentity.component";
import {RecordViewerComponent} from "./recordViewer/recordViewer.component";

let resourceListState = {name: 'resourceList', url: 'resourceList', component: ResourcesComponent };
let resourceEditState = {name: 'resourceEdit', url: 'resourceEdit/:itemAction/:itemUuid', component: ResourcesComponent };
let patientIdentityState = { name: 'patientIdentity', url: 'patientIdentity', component : PatientIdentityComponent };
let recordViewerState = {name : 'recordViewer', url: 'recordViewer', component : RecordViewerComponent };

export const states = [
	resourceListState,
	resourceEditState,
	patientIdentityState,
	recordViewerState,
];

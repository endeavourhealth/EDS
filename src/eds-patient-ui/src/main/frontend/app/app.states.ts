import {AuditComponent} from "./audit/audit.component";
import {ConsentComponent} from "./consent/consent.component";
import {MedicalRecordComponent} from "./medicalRecord/medicalRecord.component";

let medicalRecordState = { name: 'medicalRecord', url: '/medicalRecord',  component: MedicalRecordComponent };
let consentState = { name: 'consent', url: '/consent',  component: ConsentComponent };
let auditState = { name: 'audit', url: '/audit',  component: AuditComponent };

export const states = [medicalRecordState, consentState, auditState];

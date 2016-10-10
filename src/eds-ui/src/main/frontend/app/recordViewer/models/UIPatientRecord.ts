import {UIPatient} from "./resources/admin/UIPatient";
import {UIProblem} from "./resources/clinical/UIProblem";
import {UIEncounter} from "./resources/clinical/UIEncounter";
import {UICondition} from "./resources/clinical/UICondition";

export class UIPatientRecord {
    patient: UIPatient;
    conditions: UICondition[];
    problems: UIProblem[];
    encounters: UIEncounter[];

    constructor()
    constructor(patient: UIPatient)
    constructor(patient?: UIPatient) {
        this.patient = patient;
    }
}
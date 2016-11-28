import {UIPatient} from "./resources/admin/UIPatient";
import {UIProblem} from "./resources/clinical/UIProblem";
import {UIEncounter} from "./resources/clinical/UIEncounter";
import {UICondition} from "./resources/clinical/UICondition";
import {linq} from "../../common/linq";
import {UIDiary} from "./resources/clinical/UIDiary";
import {UIObservation} from "./resources/clinical/UIObservation";
import {UIMedicationOrder} from "./resources/clinical/UIMedicationOrder";
import {UIAllergy} from "./resources/clinical/UIAllergy";
import {UIImmunization} from "./resources/clinical/UIImmunization";

export class UIPatientRecord {
    patient: UIPatient;
    conditions: UICondition[];
    problems: UIProblem[];
    encounters: UIEncounter[];
    observations: UIObservation[];
    diary: UIDiary[];
    medicationOrders : UIMedicationOrder[];
    allergies : UIAllergy[];
    immunizations : UIImmunization[];

    constructor(patient?: UIPatient) {
        this.patient = patient;
    }

    public getActiveProblems(): UIProblem[] {
        return linq(this.problems)
            .Where(t => (!t.hasAbated))
            .ToArray();
    }

    public hasActiveProblems(): boolean {
        if (this.getActiveProblems() == null)
            return false;

        return (this.getActiveProblems().length > 0);
    }

    public getPastProblems(): UIProblem[] {
        return linq(this.problems)
            .Where(t => t.hasAbated)
            .ToArray();
    }

    public hasPastProblems(): boolean {
        if (this.getPastProblems() == null)
            return false;

        return (this.getPastProblems().length > 0);
    }

    public hasObservations(): boolean {
        if (this.observations == null)
            return false;

        return (this.observations.length > 0);
    }

    public hasDiaryEntries(): boolean {
        if (this.diary == null)
            return false;

        return (this.diary.length > 0);
    }

    public getCurrentMedicationOrders(): UIMedicationOrder[] {
        return linq(this.medicationOrders)
          .Where(t=> t.dateEnded.date == null)
          .ToArray();
    }

    public getPastMedicationOrders(): UIMedicationOrder[] {
        return linq(this.medicationOrders)
					.Where(t=> t.dateEnded.date != null)
					.ToArray();
    }
}
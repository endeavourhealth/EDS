import {UIPatient} from "./resources/admin/UIPatient";
import {UIProblem} from "./resources/clinical/UIProblem";
import {UIEncounter} from "./resources/clinical/UIEncounter";
import {UICondition} from "./resources/clinical/UICondition";
import {linq} from "../../common/linq";
import {UIDiary} from "./resources/clinical/UIDiary";
import {UIObservation} from "./resources/clinical/UIObservation";
import {UIAllergy} from "./resources/clinical/UIAllergy";
import {UIImmunisation} from "./resources/clinical/UIImmunisation";
import {UIFamilyHistory} from "./resources/clinical/UIFamilyHistory";
import {UIMedicationStatement} from "./resources/clinical/UIMedicationStatement";

export class UIPersonRecord {
    patients: UIPatient[];
    conditions: UICondition[];
    problems: UIProblem[];
    encounters: UIEncounter[];
    observations: UIObservation[];
    diary: UIDiary[];
    medication : UIMedicationStatement[];
    allergies : UIAllergy[];
    immunisations : UIImmunisation[];
    familyHistory : UIFamilyHistory[];

    constructor(patients: UIPatient[]) {
        this.patients = patients;
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

    public getAcuteMedication(): UIMedicationStatement[] {
        return linq(this.medication)
          .Where(t=> t.status != 'Completed' && t.authorisationType.code != 'repeat')
          .ToArray();
    }

    public getRepeatMedication(): UIMedicationStatement[] {
        return linq(this.medication)
					.Where(t=> t.status != 'Completed' && t.authorisationType.code == 'repeat')
					.ToArray();
    }

    public getPastMedication(): UIMedicationStatement[] {
        return linq(this.medication)
					.Where(t=> t.status == 'Completed')
					.ToArray();
    }

    public getInvestigations() : UIObservation[] {
        return linq(this.observations)
          .Where(t => t.related && t.related.filter((r) => r.type === 'has-member').length > 0)
          .ToArray();
    }
}
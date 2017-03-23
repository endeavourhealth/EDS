import {UIPractitioner} from "../admin/UIPractitioner";
import {UIPeriod} from "../../types/UIPeriod";
import {UIResource} from "../UIResource";
import {UIOrganisation} from "../admin/UIOrganisation";
import {UIPatient} from "../admin/UIPatient";

export class UIEpisodeOfCare extends UIResource {
    status: string;
    patient: UIPatient;
    managingOrganisation: UIOrganisation;
    period: UIPeriod;
    careManager : UIPractitioner;
}

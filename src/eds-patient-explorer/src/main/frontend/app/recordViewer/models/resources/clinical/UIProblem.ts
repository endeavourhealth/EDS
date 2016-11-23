import {UIPractitioner} from "../admin/UIPractitioner";
import {UICode} from "../../types/UICode";
import {UICondition} from "./UICondition";
import {UIDate} from "../../types/UIDate";

export class UIProblem extends UICondition {
    expectedDuration: number;
    lastReviewDate: UIDate;
    lastReviewer: UIPractitioner;
    significance: UICode;
    relatedProblem: UIProblem;
    relationshipType: string;
}

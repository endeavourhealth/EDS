import {UIResource} from "../UIResource";
import {UICodeableConcept} from "../../types/UICodeableConcept";

export class UIDosageInstruction extends UIResource {
    instructions : string;
    additionalInstructions: UICodeableConcept;
}

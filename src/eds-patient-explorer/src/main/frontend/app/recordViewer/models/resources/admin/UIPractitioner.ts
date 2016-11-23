import {UIHumanName} from "../../types/UIHumanName";
import {UIResource} from "../UIResource";

export class UIPractitioner extends UIResource {
    name: UIHumanName;
    active: boolean;
}

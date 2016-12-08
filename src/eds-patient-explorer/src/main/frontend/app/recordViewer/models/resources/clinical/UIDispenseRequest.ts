import {UIResource} from "../UIResource";
import {UIQuantity} from "../../types/UIQuantity";

export class UIDispenseRequest extends UIResource {
    numberOfRepeatsAllowed: number;
    expectedDuration: UIQuantity;
    quantity: UIQuantity;
}

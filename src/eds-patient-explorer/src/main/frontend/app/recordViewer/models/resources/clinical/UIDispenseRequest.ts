import {UIResource} from "../UIResource";

export class UIDispenseRequest extends UIResource {
    numberOfRepeatsAllowed: number;
    expectedDuration: string;
    quantity: string;
}

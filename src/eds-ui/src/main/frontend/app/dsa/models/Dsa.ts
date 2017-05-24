import {Purpose} from "./Purpose";

export class Dsa {
    uuid : string;
    name : string;
    description : string;
    derivation : string;
    dsaStatusId : number;
    consentModelId : number;
    startDate : string;
    endDate : string;
    dataFlows : { [key:string]:string; };
    regions : { [key:string]:string; };
    publishers : { [key:string]:string; };
    subscribers : { [key:string]:string; };
    purposes : Purpose[];
    benefits : Purpose[];

    getDisplayItems() :any[] {
        return [
            {label: 'Description', property: 'description'},
            {label: 'Derivation', property: 'derivation'}
        ];
    }
}

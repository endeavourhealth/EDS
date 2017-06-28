import {Address} from './Address';

export class Organisation {
    uuid : string;
    name:string;
    alternativeName: string;
    odsCode: string;
    icoCode: string;
    igToolkitStatus: string;
    dateOfRegistration: string;
    registrationPerson: string;
    evidenceOfRegistration: string;
    isService : number;
    bulkImported : number;
    bulkItemUpdated : number;
    bulkConflictedWith : string;
    type : string;
    regions : { [key:string]:string; };
    parentOrganisations : { [key:string]:string; };
    childOrganisations : { [key:string]:string; };
    services : { [key:string]:string; };
    addresses : Address[];

    getDisplayItems() :any[] {
        return [
            {label: 'ODS Code', property: 'odsCode'},
            {label: 'Alternative Name', property: 'alternativeName'},
            {label: 'ICO Code', property: 'icoCode'},
            {label: 'IG Toolkit Status', property: 'igToolkitStatus'},
            {label: 'Date of Registration', property: 'dateOfRegistration'},
            {label: 'Registration Person', property: 'registrationPerson'},
            {label: 'Evidence Of Registration', property: 'evidenceOfRegistration'},
            {label: 'Type', property: 'type'}
        ];
    }
}

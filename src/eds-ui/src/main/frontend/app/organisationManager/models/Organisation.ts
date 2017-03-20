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
    isService : string;
    regions : { [key:string]:string; };
    parentOrganisations : { [key:string]:string; };
    childOrganisations : { [key:string]:string; };
    services : { [key:string]:string; };
    addresses : Address[];
}

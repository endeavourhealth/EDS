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
    postcode: string;
    geolocation: string;
    regions : { [key:string]:string; };
}

export class Region {
    uuid:string;
    name:string;
    description:string;
    organisationCount:number;
    organisations : { [key:string]:string; };
    parentRegions : { [key:string]:string; };
    childRegions : { [key:string]:string; };
    sharingAgreements : { [key:string]:string; };
}

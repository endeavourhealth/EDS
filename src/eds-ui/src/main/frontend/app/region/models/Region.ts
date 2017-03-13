export class Region {
    uuid:string;
    name:string;
    description:string;
    organisationCount:number;
    organisations : { [key:string]:string; };
}

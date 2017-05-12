export class Cohort {
    uuid : string;
    name:string;
    nature: string;
    patientCohortInclusionConsentModel: string;
    QueryDefinition: string;
    RemovalPolicy: string;
    dpas : { [key:string]:string; };

    getDisplayItems() :any[] {
        return [
            {label: 'Nature', property: 'nature'}
        ];
    }
}

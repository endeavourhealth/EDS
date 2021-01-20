
export class SubscriberConfiguration {

    name: string;
    description: string;
    schema: string;
    deidentified: boolean;
    excludeTestPatients: boolean; //todo
    excludeNhsNumberRegex: string; //todo
    excludePatientsWithoutNhsNumber: boolean; //todo
    remoteSubscriberId: number;
    subscriberLocation: string;
    cohortType: string; //todo
    cohort: string[]; //todo
    subscriberDatabase: string; //todo
    subscriberDatabaseName: string; //todo
    subscriberTransformDatabase: string; //todo
    subscriberTransformDatabaseName: string; //todo
    numPublishers: number;
    inboundUpToDate: number;
    inboundOneDay: number;
    inboundMoreDays: number;
    outboundUpToDate: number;
    outboundOneDay: number;
    outboundMoreDays: number;    
}
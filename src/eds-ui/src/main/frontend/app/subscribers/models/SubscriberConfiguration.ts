
export class SubscriberConfiguration {

    name: string;
    description: string;
    schema: string;
    deidentified: boolean;
    excludeTestPatients: boolean; //todo
    excludeNhsNumberRegex: string; //todo
    remoteSubscriberId: number;
    subscriberLocation: string;
    cohortType: string; //todo
    cohort: string[]; //todo
    subscriberDatabase: string; //todo
    subscriberDatabaseName: string; //todo
    subscriberTransformDatabase: string; //todo
    subscriberTransformDatabaseName: string; //todo
    numPublishers: number;
}
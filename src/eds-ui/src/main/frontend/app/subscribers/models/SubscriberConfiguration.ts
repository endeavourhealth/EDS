
import {PublisherService} from "./PublisherService";
export class SubscriberConfiguration {

    name: string;
    description: string;
    schema: string;
    deidentified: boolean;
    excludeTestPatients: boolean; 
    excludeNhsNumberRegex: string; 
    excludePatientsWithoutNhsNumber: boolean; 
    remoteSubscriberId: number;
    subscriberLocation: string;
    cohortType: string; 
    cohort: string[]; 
    subscriberDatabase: string; 
    subscriberDatabaseName: string; 
    subscriberTransformDatabase: string; 
    subscriberTransformDatabaseName: string; 
    numPublishers: number;
    inboundUpToDate: number;
    inboundOneDay: number;
    inboundMoreDays: number;
    outboundUpToDate: number;
    outboundOneDay: number;
    outboundMoreDays: number;
    publisherServices: PublisherService[];
}
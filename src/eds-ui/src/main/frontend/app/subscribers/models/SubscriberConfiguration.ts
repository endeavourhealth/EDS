
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
    cohortDesc: string; //textual version of the above two
    subscriberDatabase: string; 
    subscriberDatabaseName: string; 
    subscriberTransformDatabase: string; 
    subscriberTransformDatabaseName: string; 
    numPublishers: number;
    inboundError: number;
    inboundUpToDate: number;
    inboundOneDay: number;
    inboundMoreDays: number;
    outboundUpToDate: number;
    outboundOneDay: number;
    outboundMoreDays: number;
    publisherServices: PublisherService[];
}
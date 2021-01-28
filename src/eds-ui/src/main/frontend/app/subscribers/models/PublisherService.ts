import {PublisherSystem} from "./PublisherSystem";
export class PublisherService {

    //variable returned from server
    uuid: string; //service UUID
    name: string; //org name
    alias: string; //other org name
    odsCode: string;
    tags: {}; //tags set in DDS-UI
    systemStatus: PublisherSystem[];

    //cached things from client
    inboundBehind: boolean; //if behind in inbound processing at all
    inboundWarning: string; //if inbound behind TOO MUCH (or in error)
    outboundBehind: boolean; //if behind in outbound processing at all
    outboundWarning: string; //if outbound behind TOO MUCH

}
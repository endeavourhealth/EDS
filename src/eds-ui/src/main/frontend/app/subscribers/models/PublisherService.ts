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
    inboundWarning: string;
    outboundWarning: string;
}
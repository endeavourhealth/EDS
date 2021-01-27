import {PublisherSystem} from "./PublisherSystem";
export class PublisherService {

    uuid: string; //service UUID
    name: string; //org name
    odsCode: string;
    tags: {}; //tags set in DDS-UI
    systemStatus: PublisherSystem[];

}
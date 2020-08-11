import {DsmPublisherDsa} from "./DsmPublisherDsa";
import {DsmDistributionProject} from "./DsmDistributionProject";

export class DsmServiceData {
    hasDPA: boolean;
    publisherDSAs: DsmPublisherDsa[];
    distributionProjects: DsmDistributionProject[];
}

import {RemoteFilingStatistics} from "./RemoteFilingStatistics";

export class RemoteFilingSubscribers {
    id: number;
    jsonDefinition: string;
    isLive: boolean;

    statistics: RemoteFilingStatistics[];
}
import {SftpReaderBatchContents} from "./SftpReaderBatchContents";

export class SftpReaderChannelStatus {

    id: string;
    name: string;
    pollFrequency: number;
    instanceName: string;
    latestBatchId: number;
    latestBatchIdentifier: string;
    latestBatchReceived: number;
    latestBatchSequenceNumber: number;
    latestBatchComplete: boolean;
    latestBatchFileCount: number;
    latestBatchSizeBytes: string;
    completeBatchId: number;
    completeBatchIdentifier: string;
    completeBatchReceived: number;
    completeBatchSequenceNumber: number;
    completeBatchContents: SftpReaderBatchContents[];
}
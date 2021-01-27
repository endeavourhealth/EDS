import {SftpReaderBatchContents} from "./SftpReaderBatchContents";

export class SftpReaderChannelStatus {

    //sent from server
    id: string;
    pollFrequencySeconds: number;
    dataFrequencyDays: number;
    latestPollingStart: number;
    latestPollingEnd: number;
    latestPollingException: string;
    latestPollingFilesDownloaded: number;
    latestPollingBatchesCompleted: number;
    latestPollingBatchSplitsNotifiedOk: number;
    latestPollingBatchSplitsNotifiedFailure: number;
    latestBatchId: number;
    latestBatchIdentifier: string;
    latestBatchExtractDate: number;
    latestBatchExtractCutoff: number;
    latestBatchReceived: number;
    latestBatchSequenceNumber: number;
    latestBatchComplete: boolean;
    latestBatchFileCount: number;
    latestBatchSizeBytes: string;
    completeBatchCompletionDate: number;
    completeBatchId: number;
    completeBatchIdentifier: string;
    completeBatchExtractDate: number;
    completeBatchExtractCutoff: number;
    completeBatchReceived: number;
    completeBatchSequenceNumber: number;
    completeBatchContents: SftpReaderBatchContents[];

    //locally set
    warning: boolean;
    okBatches: SftpReaderBatchContents[];
    dpaErrorBatches: SftpReaderBatchContents[];
    errorBatches: SftpReaderBatchContents[];
}
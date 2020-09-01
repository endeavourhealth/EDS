import {SftpReaderBatchContents} from "./SftpReaderBatchContents";

export class SftpReaderChannelBatch {

    id: number;
    received: number;
    identifier: string;
    sequenceNumber: number;
    complete: boolean;
    fileCount: number;
    sizeBytes: number;
    sizeDesc: string;
    batchContents: SftpReaderBatchContents[];

}
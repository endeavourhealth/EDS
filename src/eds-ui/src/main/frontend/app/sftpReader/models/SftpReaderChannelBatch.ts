import {SftpReaderBatchContents} from "./SftpReaderBatchContents";

export class SftpReaderChannelBatch {

    id: number;
    received: number;
    identifier: string;
    extractDate: number;
    extractCutoff: number
    sequenceNumber: number;
    complete: boolean;
    fileCount: number;
    sizeBytes: number;
    sizeDesc: string;
    batchContents: SftpReaderBatchContents[];

}
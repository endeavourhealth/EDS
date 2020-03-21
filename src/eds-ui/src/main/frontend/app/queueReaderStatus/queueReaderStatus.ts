export class QueueReaderStatus {

    applicationName: string;
    applicationInstanceName: string;
    timestmp: number;
    hostName: string;
    isBusy: boolean;
    maxHeapMb: number;
    maxHeapDesc: string;
    currentHeapMb: number;
    physicalMemoryMb: number;
    physicalMemoryDesc: string;
    cpuLoad: number;
    isBusyDetail: string;
    isBusySince: number;
    queueName: string;
}
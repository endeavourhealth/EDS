export class QueueReaderStatus {

    applicationName: string;
    applicationInstanceName: string;
    applicationInstanceNumber: number;
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
    isBusyOdsCode: string;
    isBusyDataDate: string;
    isBusyPublisherConfigName: string;
    dtStarted: number;
    dtJar: number;
    queueName: string;
}
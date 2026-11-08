import {RemoteFilerError} from "./RemoteFilerError";

export class RemoteFilerStatus {

    //sent back from the server
    subscriberId: number;
    countQueuedMessages: number;
    oldestQueuedMessage: number;
    newestQueuedMessage: number;
    countMessagesWrittenToSftp: number;
    oldestMessageWrittenToSftp: number;
    newestMessageWrittenToSftp: number;
    countMessagesInError: number;
    oldestMessageInError: number;
    newestMessageInError: number;
    errors: RemoteFilerError[];

    //cached variables for display
    queuedMessageWarning: string;
    writtenToSftpWarning: string;
    messagesInErrorWarning: string;
}
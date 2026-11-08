import {RemoteFilerFileDetails} from "./RemoteFilerFileDetails";
import {RemoteFilerSftpDetails} from "./RemoteFilerSftpDetails";
export class RemoteFilerTechnicalDetails {
    subscriberFileLocationDetails: RemoteFilerFileDetails;
    sftpConnectionDetails: RemoteFilerSftpDetails;
}
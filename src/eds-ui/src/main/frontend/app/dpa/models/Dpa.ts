export class Dpa {
    uuid : string;
    name : string;
    description : string;
    derivation : string;
    publisherInformation : string;
    publisherContractInformation : string;
    publisherDataSet : string;
    dsaStatusId : number;
    storageProtocolId : number;
    dataFlow : string;
    returnToSenderPolicy : string;
    dataFlows : { [key:string]:string; };
    cohorts : { [key:string]:string; };
}

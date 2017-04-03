export class Dsa {
    uuid : string;
    name : string;
    description : string;
    derivation : string;
    publisherInformation : string;
    publisherContractInformation : string;
    subscriberInformation : string;
    subscriberContractInformation : string;
    dsaStatusId : number;
    consentModel : string;
    dataFlows : { [key:string]:string; };
}

export class OrganisationNode {
    id : string;
    name : string;
    hasChildren : boolean;
    children : OrganisationNode[];
    type : number;
    itemUuid : string;
}
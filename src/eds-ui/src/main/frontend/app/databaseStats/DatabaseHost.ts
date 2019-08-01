import {Database} from "./Database";
export class DatabaseHost {
    type: string;
    host: string;
    expanded: boolean;
    refreshingDatabases: boolean;
    databases: Database[];
    error: string;
}
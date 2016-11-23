import {List} from "linqts/dist/linq";

export function linq<T>(array: T[]): List<T> {
    return new List<T>(array);
}
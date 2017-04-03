import {Pipe} from "@angular/core";
import {PipeTransform} from "@angular/core";

@Pipe({name: 'formatBoolean'})
export class FormatBoolean implements PipeTransform {

    transform(value: string, args: string[]): any {
        if (value) {
            return 'Y';
        } else {
            return 'N';
        }
    }
}

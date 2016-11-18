import {Pipe, PipeTransform} from "@angular/core";

@Pipe({name : 'parentheses'})
export class Parentheses implements PipeTransform {
    transform(text: string): string {
        if (text)
            return "(" + text.trim() + ")";

        return "";
    }
}


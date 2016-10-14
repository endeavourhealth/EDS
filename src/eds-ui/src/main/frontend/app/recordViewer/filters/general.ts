import S = require("string");

export function parentheses() {
    return getParentheses;
}

function getParentheses(text: string): string {
    if (S(text).isEmpty())
        return "";

    return "(" + text.trim() + ")";
}

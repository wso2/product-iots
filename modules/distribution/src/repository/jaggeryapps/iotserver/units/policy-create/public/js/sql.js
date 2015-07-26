/*Copyright (C) 2015 by Marijn Haverbeke <marijnh@gmail.com> and others

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.*/

/*Annotations, Annotation Names and relevant tokens*/
var ANNOTATION_IMPORT = "Import";
var ANNOTATION_EXPORT = "Export";

var ANNOTATION_TOKEN_AT = "@";
var ANNOTATION_TOKEN_OPENING_BRACKET = "(";
var ANNOTATION_TOKEN_CLOSING_BRACKET = ")";

var REGEX_LINE_STARTING_WITH_PLAN = /^@Plan.*/g;
var REGEX_LINE_STARTING_WITH_SINGLE_LINE_COMMENT = /^--.*/g;
var REGEX_LINE_STARTING_WITH_MULTI_LINE_COMMENT = /^\/\*.*\*\//g;
var REGEX_LINE_STARTING_WITH_IMPORT_STATEMENT = /^@Import.*/g;

var SIDDHI_STATEMENT_DELIMETER = ";";
var SIDDHI_LINE_BREAK = "\n";
var SIDDHI_LINE_BREAK_CHARACTER = '\n';
var SIDDHI_SINGLE_QUOTE = "'";
var SIDDHI_SPACE_LITERAL = " ";

var SIDDHI_LITERAL_DEFINE_STREAM = "define stream";

var MIME_TYPE_SIDDHI_QL = "text/siddhi-ql";


CodeMirror.defineMode("sql", function (config, parserConfig) {
    "use strict";

    var client = parserConfig.client || {},
        atoms = parserConfig.atoms || {"false":true, "true":true, "null":true},
        builtin = parserConfig.builtin || {},
        keywords = parserConfig.keywords || {},
        operatorChars = parserConfig.operatorChars || /^[*+\-%<>!=&|~^]/,
        support = parserConfig.support || {},
        hooks = parserConfig.hooks || {},
        dateSQL = parserConfig.dateSQL || {"date":true, "time":true, "timestamp":true};

    function tokenBase(stream, state) {
        var ch = stream.next();

        // call hooks from the mime type
        if (hooks[ch]) {
            var result = hooks[ch](stream, state);
            if (result !== false) return result;
        }

        if (ch.charCodeAt(0) > 47 && ch.charCodeAt(0) < 58) {
            // numbers
            // ref: http://dev.mysql.com/doc/refman/5.5/en/number-literals.html
            stream.match(/^[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?/);
            support.decimallessFloat == true && stream.eat('.');
            return "number";
        } else if (ch == "'" || (ch == '"' && support.doubleQuote)) {
            // strings
            // ref: http://dev.mysql.com/doc/refman/5.5/en/string-literals.html
            state.tokenize = tokenLiteral(ch);
            return state.tokenize(stream, state);
        } else if (/^[\(\),\;\[\]]/.test(ch)) {
            // no highlightning
            return null;
        } else if ((ch == "-" && stream.eat("-") && (!support.commentSpaceRequired || stream.eat(" ")))) {
            // 1-line comments
            // ref: https://kb.askmonty.org/en/comment-syntax/
            stream.skipToEnd();
            return "comment";
        } else if (ch == "/" && stream.eat("*")) {
            // multi-line comments
            // ref: https://kb.askmonty.org/en/comment-syntax/
            state.tokenize = tokenComment;
            return state.tokenize(stream, state);
        } else if (ch == ".") {
            // .1 for 0.1
            if (support.zerolessFloat == true && stream.match(/^(?:\d+(?:e[+-]?\d+)?)/i)) {
                return "number";
            }
        } else {
            stream.eatWhile(/^[_\-\w\d]/);    /* Character '-' will also be eaten, to prevent the highlight happening in keywords being embedded in non-keyword strings. For example, 'all' in 'all-nonkeyword' */
            var word = stream.current().toLowerCase();         // Added toLowerCase() to highlight keywords in a case insensitive manner.
            // dates (standard SQL syntax)
            // ref: http://dev.mysql.com/doc/refman/5.5/en/date-and-time-literals.html
            if (dateSQL.hasOwnProperty(word) && (stream.match(/^( )+'[^']*'/) || stream.match(/^( )+"[^"]*"/)))
                return "number";
            if (atoms.hasOwnProperty(word)) return "atom";
            if (builtin.hasOwnProperty(word)) return "builtin";
            if (keywords.hasOwnProperty(word)) return "keyword";
            if (client.hasOwnProperty(word)) return "string-2";
            return null;
        }
    }

    // 'string', with char specified in quote escaped by '\'
    function tokenLiteral(quote) {
        return function (stream, state) {
            var escaped = false, ch;
            while ((ch = stream.next()) != null) {
                if (ch == quote && !escaped) {
                    state.tokenize = tokenBase;
                    break;
                }
                escaped = !escaped && ch == "\\";
            }
            return "string";
        };
    }

    function tokenComment(stream, state) {
        while (true) {
            if (stream.skipTo("*")) {
                stream.next();
                if (stream.eat("/")) {
                    state.tokenize = tokenBase;
                    break;
                }
            } else {
                stream.skipToEnd();
                break;
            }
        }
        return "comment";
    }

    function pushContext(stream, state, type) {
        state.context = {
            prev:state.context,
            indent:stream.indentation(),
            col:stream.column(),
            type:type
        };
    }

    function popContext(state) {
        state.indent = state.context.indent;
        state.context = state.context.prev;
    }

    return {
        startState:function () {
            return {tokenize:tokenBase, context:null};
        },

        token:function (stream, state) {
            if (stream.sol()) {
                if (state.context && state.context.align == null)
                    state.context.align = false;
            }
            if (stream.eatSpace()) return null;

            var style = state.tokenize(stream, state);
            if (style == "comment") return style;

            if (state.context && state.context.align == null)
                state.context.align = true;

            var tok = stream.current();
            if (tok == "(")
                pushContext(stream, state, ")");
            else if (tok == "[")
                pushContext(stream, state, "]");
            else if (state.context && state.context.type == tok)
                popContext(state);
            return style;
        },

        indent:function (state, textAfter) {
            var cx = state.context;
            if (!cx) return CodeMirror.Pass;
            var closing = textAfter.charAt(0) == cx.type;
            if (cx.align) return cx.col + (closing ? 0 : 1);
            else return cx.indent + (closing ? 0 : config.indentUnit);
        },

        blockCommentStart: "/*",
        blockCommentEnd: "*/",
        lineComment: "--"
    };
});

(function () {
    "use strict";

    // `identifier`
    function hookIdentifier(stream) {
        // MySQL/MariaDB identifiers
        // ref: http://dev.mysql.com/doc/refman/5.6/en/identifier-qualifiers.html
        var ch;
        while ((ch = stream.next()) != null) {
            if (ch == "`" && !stream.eat("`")) return "variable-2";
        }
        stream.backUp(stream.current().length - 1);
        return stream.eatWhile(/\w/) ? "variable-2" : null;
    }

    // variable token
    function hookVar(stream) {
        // variables
        // @@prefix.varName @varName
        // varName can be quoted with ` or ' or "
        // ref: http://dev.mysql.com/doc/refman/5.5/en/user-variables.html
        if (stream.eat("@")) {
            stream.match(/^session\./);
            stream.match(/^local\./);
            stream.match(/^global\./);
        }

        if (stream.eat("'")) {
            stream.match(/^.*'/);
            return "variable-2";
        } else if (stream.eat('"')) {
            stream.match(/^.*"/);
            return "variable-2";
        } else if (stream.eat("`")) {
            stream.match(/^.*`/);
            return "variable-2";
        } else if (stream.match(/^[0-9a-zA-Z$\.\_]+/)) {
            return "variable-2";
        }
        return null;
    }

    ;

    // short client keyword token
    function hookClient(stream) {
        // \N means NULL
        // ref: http://dev.mysql.com/doc/refman/5.5/en/null-values.html
        if (stream.eat("N")) {
            return "atom";
        }
        // \g, etc
        // ref: http://dev.mysql.com/doc/refman/5.5/en/mysql-commands.html
        return stream.match(/^[a-zA-Z.#!?]/) ? "variable-2" : null;
    }

    // these keywords are used by all SQL dialects (however, a mode can still overwrite it)
    var sqlKeywordsWithoutSymbols = "all and as begin by contains define delete end events " +
        "every first for from full group having inner insert into join last " +
        "left not of on or outer output partition raw return right select snapshot stream table ";
    var sqlKeywords = ", : ? # ( ) " + sqlKeywordsWithoutSymbols;
    var builtIn = "bool double float int long object string ";
    var atoms = "false true null ";
    var dateSQL = "days hours milliseconds minutes months seconds ";
    var allSqlSuggestions = sqlKeywordsWithoutSymbols + builtIn + atoms + dateSQL;

    // turn a space-separated list into an array
    function set(str) {
        var obj = {}, words = str.split(" ");
        for (var i = 0; i < words.length; ++i) obj[words[i]] = true;
        return obj;
    }

    // A generic SQL Mode. It's not a standard, it just try to support what is generally supported
    CodeMirror.defineMIME(MIME_TYPE_SIDDHI_QL, {
        name:"sql",
        keywords:set(sqlKeywords),
        builtin:set(builtIn),
        atoms:set(atoms),
        operatorChars:/^[*+%<>!=/]/,
        dateSQL:set(dateSQL),
        support:set("doubleQuote "),
        allSqlSuggestions:set(allSqlSuggestions)
    });
}());

/*
 How Properties of Mime Types are used by SQL Mode
 =================================================

 keywords:
 A list of keywords you want to be highlighted.
 functions:
 A list of function names you want to be highlighted.
 builtin:
 A list of builtin types you want to be highlighted (if you want types to be of class "builtin" instead of "keyword").
 operatorChars:
 All characters that must be handled as operators.
 client:
 Commands parsed and executed by the client (not the server).
 support:
 A list of supported syntaxes which are not common, but are supported by more than 1 DBMS.
 * ODBCdotTable: .tableName
 * zerolessFloat: .1
 * doubleQuote
 * nCharCast: N'string'
 * charsetCast: _utf8'string'
 * commentHash: use # char for comments
 * commentSlashSlash: use // for comments
 * commentSpaceRequired: require a space after -- for comments
 atoms:
 Keywords that must be highlighted as atoms,. Some DBMS's support more atoms than others:
 UNKNOWN, INFINITY, UNDERFLOW, NaN...
 dateSQL:
 Used for date/time SQL standard syntax, because not all DBMS's support same temporal types.
 */
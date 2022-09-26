//
// Copyright 2022 Inrupt Inc.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal in
// the Software without restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
// Software, and to permit persons to whom the Software is furnished to do so,
// subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
// INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
// PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
// SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//

grammar Link ;

// ------------------------------------
//  Grammar adapted from IETF RFC 9110
// ------------------------------------


// --------------
//  Parser Rules
// --------------
linkHeader : link (',' link)* ;

// link-value = "<" URI-Reference ">" *( OWS ";" OWS link-param )
link : UriReference (WS? ';' WS? LinkParam)* ;


// -------------
//  Lexer Rules
// -------------
UriReference : '<' UriChar+ '>' ;

// URI characters -- this does not perform syntactic parsing of URIs; that is left to the Java URI parser
fragment UriChar : ('\u0021'..'\u003b' | '=' | '\u003f'..'\u007e'| ObsText ); //3c 3e - exclude

// link-param = token BWS [ "=" BWS ( token / quoted-string ) ]
LinkParam : Token '=' ( Token | QuotedString ) ;

// obs-text = %x80-FF
fragment ObsText : '\u0080'..'\uFFFF' ;

// qdtext = HTAB / SP / "!" / %x23-5B ; '#'-'[' / %x5D-7E ; ']'-'~' / obs-text
fragment Qdtext : ( HTAB | SP | '!' | '\u0023'..'\u005B' | '\u005D'..'\u007E' | ObsText ) ;

// quoted-pair = "\" ( HTAB / SP / VCHAR / obs-text )
fragment QuotedPair : '\\' ( HTAB | SP | VCHAR | ObsText ) ;

// quoted-string = DQUOTE *( qdtext / quoted-pair ) DQUOTE
fragment QuotedString : DQUOTE ( Qdtext | QuotedPair )* DQUOTE ;

// tchar = "!" / "#" / "$" / "%" / "&" / "'" / "*" / "+" / "-" / "." / "^" / "_" / "`" / "|" / "~" / DIGIT / ALPHA
fragment Tchar : ( '!' | '#' | '$' | '%' | '&' | '\'' | '*' | '+' | '-' | '.' | '^' | '_' | '`' | '|' | '~' | DIGIT | ALPHA ) ;

// token = 1*tchar
fragment Token : ( Tchar )+ ;

// Other constants (lexical entities)
WS : ( SP | HTAB )+ ;

// Other constants (lexical fragments)
fragment SP : ' ' ;
fragment DQUOTE : '\u0022' ;
fragment HTAB : '\t' ;
fragment DIGIT : '0'..'9' ;
fragment ALPHA : ( 'a'..'z' | 'A'..'Z' ) ;
fragment VCHAR : '\u0021'..'\u007E' ;

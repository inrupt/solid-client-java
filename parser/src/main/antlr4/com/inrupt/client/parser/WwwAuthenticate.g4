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

grammar WwwAuthenticate ;

// ------------------------------------
//  Grammar adapted from IETF RFC 9110
// ------------------------------------


// --------------
//  Parser Rules
// --------------
// WWW-Authenticate = [ challenge *( OWS "," OWS challenge ) ]
wwwAuthenticate : challenge ( ',' WS? challenge )* ;

// challenge = auth-scheme [ 1*SP ( token68 / [ auth-param *( OWS "," OWS auth-param ) ] ) ]
challenge : AuthScheme ( WS ( Token68 | AuthParam ( WS? ',' WS? AuthParam )* ) )* ;


// -------------
//  Lexer Rules
// -------------
// auth-param = token BWS "=" BWS ( token / quoted-string )
AuthParam : Token '=' ( Token | QuotedString ) ;

// auth-scheme = token
AuthScheme : Token ;

// obs-text = %x80-FF
fragment ObsText : '\u0080'..'\uFFFF' ;

// qdtext = HTAB / SP / "!" / %x23-5B ; '#'-'[' / %x5D-7E ; ']'-'~' / obs-text
fragment Qdtext : ( HTAB | SP | '!' | '\u0023'..'\u005B' | '\u005D'..'\u007E' | ObsText ) ;

// quoted-pair = "\" ( HTAB / SP / VCHAR / obs-text )
fragment QuotedPair : '\\' ( HTAB | SP | VCHAR | ObsText ) ;

// quoted-string = DQUOTE *( qdtext / quoted-pair ) DQUOTE
QuotedString : DQUOTE ( Qdtext | QuotedPair )* DQUOTE ;

// tchar = "!" / "#" / "$" / "%" / "&" / "'" / "*" / "+" / "-" / "." / "^" / "_" / "`" / "|" / "~" / DIGIT / ALPHA
fragment Tchar : ( '!' | '#' | '$' | '%' | '&' | '\'' | '*' | '+' | '-' | '.' | '^' | '_' | '`' | '|' | '~' | DIGIT | ALPHA ) ;

// token = 1*tchar
Token : ( Tchar )+ ;

// token68 = 1*( ALPHA / DIGIT / "-" / "." / "_" / "~" / "+" / "/" ) *"="
Token68 : ( ALPHA | DIGIT | '-' | '.' | '_' | '~' | '+' | '/' )+ '='* ;

// Other constants (lexical entities)
WS : ( SP | HTAB )+ ;

// Other constants (lexical fragments)
fragment SP : ' ' ;
fragment DQUOTE : '"' ;
fragment HTAB : '\t' ;
fragment DIGIT : '0'..'9' ;
fragment ALPHA : ( 'a'..'z' | 'A'..'Z' ) ;
fragment VCHAR : '\u0021'..'\u007E' ;


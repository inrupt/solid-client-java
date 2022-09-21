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
// Link  = #link-value
link : linkValue (',' linkValue)* ;

// link-value = "<" URI-Reference ">" *( OWS ";" OWS link-param )
linkValue : '<' UriReference '>' (';' LinkParam)* ;


// -------------
//  Lexer Rules
// -------------
// link-param = token BWS [ "=" BWS ( token / quoted-string ) ]
LinkParam : Token ('=' ( Token | QuotedString ))? ;

//URI-reference = URI / relative-ref
UriReference : (Uri | RelativeRef) ;

//URI = scheme ":" hier-part [ "?" query ] [ "#" fragment ]
fragment Uri : Scheme ':' HeirPart ('?' Query?)? ('#' Fragment?)? ;

//scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
fragment Scheme : ALPHA (ALPHA | DIGIT | '+' | '-' | '.')* ;

//hier-part = "//" authority path-abempty / path-absolute / path-rootless / path-empty
fragment HeirPart : '//' Authority (PathAbempty | PathAbsolute | PathRootless| PathEmpty) ;

//authority = [ userinfo "@" ] host [ ":" port ]
fragment Authority : (UserInfo? '@')? Host ( ':' Port)? ;

//userinfo = *( unreserved / pct-encoded / sub-delims / ":" )
fragment UserInfo : (Unreserved | PctEncoded | SubDelims | ':')+ ;
   
//port = *DIGIT
fragment Port : DIGIT+ ;

//host = IP-literal / IPv4address / reg-name
fragment Host : (IPLiteral | IPv4address | RegName) ;

//IP-literal    = "[" ( IPv6address / IPvFuture  ) "]"
fragment IPLiteral : '[' (IPv6address | IPvFuture) ']' ;

//IPvFuture     = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )
fragment IPvFuture : 'v' HEXDIG+ '.' (Unreserved | SubDelims | ':')+ ;

/*IPv6address   =                             6( H16 ":" ) LS32
                 /                       "::" 5( H16 ":" ) LS32
                 / [               H16 ] "::" 4( H16 ":" ) LS32
                 / [ *1( H16 ":" ) H16 ] "::" 3( H16 ":" ) LS32
                 / [ *2( H16 ":" ) H16 ] "::" 2( H16 ":" ) LS32
                 / [ *3( H16 ":" ) H16 ] "::"    H16 ":"   LS32
                 / [ *4( H16 ":" ) H16 ] "::"              LS32
                 / [ *5( H16 ":" ) H16 ] "::"              H16
                 / [ *6( H16 ":" ) H16 ] "::"
*/
fragment IPv6address
:                                                                            '::' (H16 ':') (H16 ':') (H16 ':') (H16 ':') (H16 ':') (H16 ':') LS32
|                                                                            '::'           (H16 ':') (H16 ':') (H16 ':') (H16 ':') (H16 ':') LS32
  |                                                                    H16?  '::'                     (H16 ':') (H16 ':') (H16 ':') (H16 ':') LS32
  |                                                        ((H16 ':')? H16)? '::'                               (H16 ':') (H16 ':') (H16 ':') LS32
  |                                             ((H16 ':')? (H16 ':')? H16)? '::'                                         (H16 ':') (H16 ':') LS32
  |                                  ((H16 ':')? (H16 ':')? (H16 ':')? H16)? '::'                                                    H16 ':'  LS32
  |                       ((H16 ':')? (H16 ':')? (H16 ':')? (H16 ':')? H16)? '::'                                                             LS32
  |            ((H16 ':')? (H16 ':')? (H16 ':')? (H16 ':')? (H16 ':')? H16)? '::'                                                             H16
  | ((H16 ':')? (H16 ':')? (H16 ':')? (H16 ':')? (H16 ':')? (H16 ':')? H16)? '::'
  ;

//H16           = 1*4HEXDIG
fragment H16 : HEXDIG (HEXDIG)? (HEXDIG)? (HEXDIG)?; //HELP

//LS32          = ( H16 ":" H16 ) / IPv4address
fragment LS32 : ((H16 ':' H16) | IPv4address) ;

//IPv4address   = dec-octet "." dec-octet "." dec-octet "." dec-octet
fragment IPv4address : DecOctet '.' DecOctet '.' DecOctet '.' DecOctet ;

//relative-ref  = relative-part [ "?" query ] [ "#" fragment ]
fragment RelativeRef : RelativePart ('?' Query?)? ('#' Fragment?)? ; 

//relative-part = "//" authority path-abempty / path-absolute / path-noscheme / path-empty
fragment RelativePart : '//' Authority (PathAbempty | PathAbsolute | PathNoscheme | PathEmpty) ;

//alt //RelativePart : '//' Authority (PathAbempty | PathAbsolute | PathNoscheme)? ;

//absolute-URI  = scheme ":" hier-part [ "?" query ]
fragment AbsoluteURI : Scheme ':' HeirPart ('?' Query?) ;

/*dec-octet     = DIGIT                  ; 0-9
                 / %x31-39 DIGIT         ; 10-99
                 / "1" 2DIGIT            ; 100-199
                 / "2" %x30-34 DIGIT     ; 200-249
                 / "25" %x30-35          ; 250-255
*/
fragment DecOctet : (DIGIT | '\u0031'..'\u0039' DIGIT | '1' /* 2DIGIT */ | '2' '\u0030'..'\u0034'| '25' '\u0030'..'\u0035'); // HELP


//reg-name      = *( unreserved / pct-encoded / sub-delims )
fragment RegName : (Unreserved | PctEncoded | SubDelims)+ ;

/*path           = path-abempty    ; begins with "/" or is empty
                 / path-absolute   ; begins with "/" but not "//"
                 / path-noscheme   ; begins with a non-colon segment
                 / path-rootless   ; begins with a segment
                 / path-empty      ; zero characters
*/
fragment Path : (PathAbempty | PathAbsolute | PathNoscheme | PathRootless | PathEmpty) ; 
// (PathAbempty | PathAbsolute | PathNoscheme | PathRootless)?

//path-abempty  = *( "/" segment )
fragment PathAbempty : ('/' Segment)+ ;

//path-absolute = "/" [ segment-nz *( "/" segment ) ]
fragment PathAbsolute : '/' (SegmentNz ('/' Segment)*)? ;

//path-noscheme = segment-nz-nc *( "/" segment )
fragment PathNoscheme : SegmentNzNc ('/' Segment)* ;

//path-rootless = segment-nz *( "/" segment )
fragment PathRootless : SegmentNz ('/' Segment)* ;

//path-empty    = 0<pchar>
fragment PathEmpty : '/**/';

//segment       = *pchar
fragment Segment : Pchar+ ;

//segment-nz    = 1*pchar
fragment SegmentNz : Pchar+ ;

//segment-nz-nc = 1*( unreserved / pct-encoded / sub-delims / "@" ) ; non-zero-length segment without any colon ":"
fragment SegmentNzNc : (Unreserved | PctEncoded | SubDelims | '@')+ ;

//pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
fragment Pchar :(Unreserved | PctEncoded | SubDelims | ':' | '@') ;

//query         = *( pchar / "/" / "?" )
fragment Query : (Pchar | '/' | '?')+ ;

//fragment      = *( pchar / "/" / "?" )
fragment Fragment : (Pchar | '/' | '?')+ ;

//pct-encoded   = "%" HEXDIG HEXDIG
fragment PctEncoded : '%' HEXDIG HEXDIG ;

//unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
fragment Unreserved : (ALPHA | DIGIT | '-' | '.' | '_' | '~') ;

//reserved      = gen-delims / sub-delims
fragment Reserved : (GenDelims | SubDelims) ;

//gen-delims    = ":" / "/" / "?" / "#" / "[" / "]" / "@"
fragment GenDelims : (':' | '/' | '?' | '#' | '[' | ']' | '@') ;

//sub-delims    = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
fragment SubDelims : ('!' | '$' | '&' | '\'' | '(' | ')' | '*' | '+' | ',' | ';' | '=') ; // check escape character works

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

// token68 = 1*( ALPHA / DIGIT / "-" / "." / "_" / "~" / "+" / "/" ) *"="
fragment Token68 : ( ALPHA | DIGIT | '-' | '.' | '_' | '~' | '+' | '/' )+ '='* ;

// Other constants (lexical entities)
fragment WS : ( SP | HTAB )+ ;

// Other constants (lexical fragments)
fragment HEXDIG: ('0' ..'9' | ( 'a'..'f' | 'A'..'F' )); //check antlr spec
fragment SP : ' ' ;
fragment DQUOTE : '"' ;
fragment HTAB : '\t' ;
fragment DIGIT : '0'..'9' ;
fragment ALPHA : ( 'a'..'z' | 'A'..'Z' ) ;
fragment VCHAR : '\u0021'..'\u007E' ;
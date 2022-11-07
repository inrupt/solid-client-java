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

grammar WacAllow ;

// ------------------------------------
//  Grammar adapted from Solid TR Web Access Control: Section 6.1
// ------------------------------------


// --------------
//  Parser Rules
// --------------

//wacAllow : 'WAC-Allow' ':' WS? ((','| accessParam) (WS? ',' (WS? accessParam))*)? WS?;
    //wac-allow = "WAC-Allow" ":" OWS #access-param OWS
    //#element => [ ( "," / element ) *( OWS "," [ OWS element ] ) ]
wacAllow : AccessParam ( WS? ',' WS? AccessParam)* ; 


// -------------
//  Lexer Rules
// -------------

//access-param = permission-group OWS "=" OWS DQUOTE OWS *1(access-mode *(RWS access-mode)) OWS DQUOTE
    //access-param = permission-group OWS "=" OWS access-modes
    //access-modes = DQUOTE OWS *1(access-mode *(RWS access-mode)) OWS DQUOTE
AccessParam : PermissionGroup WS? '=' WS? DQUOTE WS? (AccessMode (WS AccessMode)*)? WS? DQUOTE ;

// access-mode = "read" / "write" / "append" / "control"
fragment AccessMode : ALPHA+ ;

//permission-group = 1*ALPHA
fragment PermissionGroup : ALPHA+ ; 

// Other constants (lexical entities)
WS : ( SP | HTAB )+ ;
DQUOTE : '\u0022' ;

// Other constants (lexical fragments)
fragment SP : ' ' ;
fragment HTAB : '\t' ;
fragment ALPHA : ( 'a'..'z' | 'A'..'Z' ) ;

grammar miGramatica;

// Manejo de errores con el formato adecuado
@lexer::members {
    @Override
    public void notifyListeners(LexerNoViableAltException e) {
        String text = _input.getText(Interval.of(_tokenStartCharIndex, _input.index()));
        int line = getLine();
        String msg = "Error [Fase Léxica]: La línea " + line + " contiene un error, lexema no reconocido: " + text;
        System.err.println(msg);
        // No llamamos a getErrorListenerDispatch().syntaxError(...)
    }
}


// Reglas del parser (sintácticas)
program     : code ;

code        : (declaration END)* ;

declaration : exp
            | flux
            | print_stmt ;

flux        : if_stmt
            | while_stmt
            | for_stmt ;

if_stmt     : IF PARL exp PARR KEYL code KEYR if_prime ;

if_prime    : ELSE KEYL code KEYR
            | /* vacío */ ;

while_stmt  : WHILE PARL exp PARR KEYL code KEYR ;

for_stmt    : FOR PARL optionalExp END optionalExp END optionalExp PARR KEYL code KEYR ;

print_stmt  : PRINT PARL ID PARR ;

optionalExp : exp
            | /* vacío */ ;

exp         : assignmentExp
            | /* vacío */ ;

assignmentExp
            : relationalExp (ASSIGN assignmentExp)?
            ;

relationalExp
            : additiveExp (OREL additiveExp)*
            ;

additiveExp : multiplicativeExp ((OSUM | ORES) multiplicativeExp)*
            ;

multiplicativeExp
            : unaryExp ((OMUL | ODIV) unaryExp)*
            ;

unaryExp    : ORES unaryExp
            | primaryExp ;

primaryExp  : ID
            | NUM
            | PARL exp PARR ;

// Tokens (léxicos)
IF          : 'if' ;
ELSE        : 'else' ;
FOR         : 'for' ;
WHILE       : 'while' ;
PRINT       : 'print' ;

OSUM        : '+' ;
ORES        : '-' ;
OMUL        : '*' ;
ODIV        : '/' ;

OREL        : '<='
            | '>='
            | '=='
            | '<>'
            | '<'
            | '>' ;

KEYL        : '{' ;
KEYR        : '}' ;
PARL        : '(' ;
PARR        : ')' ;
ASSIGN      : '=' ;
END         : ';' ;

fragment LETTER        : [a-z];
ID                     : LETTER (LETTER)* {getText().length() <= 12}?;
NUM                    : [0-9]+ ;

// Ignorar espacios en blanco y saltos de línea
WS          : [ \t\r\n]+ -> skip ;

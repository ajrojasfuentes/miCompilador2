package com.miCompilador;

import org.antlr.v4.runtime.*;

import java.lang.reflect.Array;

public class CustomErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        String[] error = msg.split(" ");
        String message = "Error [Fase Sintáctica]: La línea " + line + " contiene un error en su gramática, falta token " + error[1];
        System.err.println(message);
    }
}

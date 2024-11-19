package com.miCompilador;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Listener personalizado para capturar errores léxicos durante el análisis con ANTLR.
 */
public class LexerErrorListener extends BaseErrorListener {

    // Indicador de si se han encontrado errores léxicos
    private boolean hayErrores = false;

    /**
     * Verifica si se han detectado errores léxicos.
     *
     * @return true si hay errores, false en caso contrario.
     */
    public boolean hayErrores() {
        return hayErrores;
    }

    /**
     * Método invocado por ANTLR cuando se detecta un error sintáctico.
     *
     * @param recognizer          El reconocedor que detectó el error.
     * @param offendingSymbol     El símbolo que causó el error.
     * @param line                La línea donde ocurrió el error.
     * @param charPositionInLine  La posición de carácter en la línea donde ocurrió el error.
     * @param msg                 El mensaje de error generado.
     * @param e                   La excepción que causó el error, si existe.
     */
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        hayErrores = true;
        String message = String.format(
                "Error [Fase Léxica]: Línea %d, posición %d: %s",
                line, charPositionInLine, msg
        );
        System.err.println(message);
    }
}

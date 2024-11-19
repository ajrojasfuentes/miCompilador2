package com.miCompilador;

import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * AnalizadorSemantico se encarga de la fase semántica de un compilador, utilizando ANTLR para manejar la gramática.
 * Este analizador verifica la correcta declaración y uso de las variables dentro del código fuente.
 */
public class AnalizadorSemantico extends miGramaticaBaseVisitor<Void> {

    /**
     * Tabla de símbolos que almacena información sobre las variables declaradas en el código.
     * La clave es el nombre de la variable y el valor es una instancia de la clase {@link Simbolo}.
     */
    private Map<String, Simbolo> tablaSimbolos = new HashMap<>();

    /**
     * Indicador de si se han encontrado errores semánticos durante el análisis.
     */
    private boolean hayErrores = false;

    /**
     * Verifica si se han encontrado errores semánticos.
     *
     * @return {@code true} si hay errores semánticos, {@code false} en caso contrario.
     */
    public boolean hayErroresSemanticos() {
        return hayErrores;
    }

    /**
     * Clase interna que representa un símbolo en la tabla de símbolos.
     */
    public class Simbolo {
        String nombre;
        String tipo; // Tipo de dato de la variable
        int lineaDeclaracion;

        /**
         * Constructor para crear un símbolo con el nombre, tipo y línea de declaración especificados.
         *
         * @param nombre el nombre del símbolo.
         * @param tipo el tipo de dato del símbolo.
         * @param lineaDeclaracion la línea donde se declaró el símbolo.
         */
        Simbolo(String nombre, String tipo, int lineaDeclaracion) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.lineaDeclaracion = lineaDeclaracion;
        }
    }

    /**
     * Obtiene la tabla de símbolos que contiene información sobre todas las variables declaradas.
     *
     * @return un mapa que asocia el nombre de cada variable con su respectivo símbolo.
     */
    public Map<String, Simbolo> getTablaSimbolos() {
        return tablaSimbolos;
    }

    /**
     * Maneja las expresiones de asignación en el código, verificando la declaración de variables.
     *
     * @param ctx el contexto de la expresión de asignación.
     * @return {@code null} después de visitar los hijos del contexto.
     */
    @Override
    public Void visitAssignmentExp(miGramaticaParser.AssignmentExpContext ctx) {
        if (ctx.relationalExp() != null && ctx.ASSIGN() != null && ctx.assignmentExp() != null) {
            String nombreVariable = ctx.relationalExp().getText();
            Token tokenVariable = ctx.relationalExp().start;
            int linea = tokenVariable.getLine();

            // Verificar si la variable ya fue declarada
            if (!tablaSimbolos.containsKey(nombreVariable)) {
                // Si no, agregarla a la tabla de símbolos
                tablaSimbolos.put(nombreVariable, new Simbolo(nombreVariable, "variable", linea));
            }
        }
        return visitChildren(ctx);
    }

    /**
     * Maneja las expresiones primarias en el código, verificando el uso correcto de identificadores.
     *
     * @param ctx el contexto de la expresión primaria.
     * @return {@code null} después de visitar los hijos del contexto.
     */
    @Override
    public Void visitPrimaryExp(miGramaticaParser.PrimaryExpContext ctx) {
        if (ctx.ID() != null) {
            String nombreVariable = ctx.ID().getText();
            Token tokenVariable = ctx.ID().getSymbol();
            int linea = tokenVariable.getLine();

            // Verificar si la variable ha sido declarada
            if (!tablaSimbolos.containsKey(nombreVariable)) {
                hayErrores = true;
                System.err.println("Error [Fase Semántica]: La línea " + linea + " contiene un error, no declarado identificador " + nombreVariable);
            }
        }
        return visitChildren(ctx);
    }

    // Puedes agregar más métodos y validaciones según sea necesario
}
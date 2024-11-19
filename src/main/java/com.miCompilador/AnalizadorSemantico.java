package com.miCompilador;

import java.util.HashMap;
import java.util.Map;
import org.antlr.v4.runtime.Token;

public class AnalizadorSemantico extends miGramaticaBaseVisitor<Void> {

    // Tabla de símbolos: nombre de variable -> información
    private Map<String, Simbolo> tablaSimbolos = new HashMap<>();

    private boolean hayErrores = false;

    public boolean hayErroresSemanticos() {
        return hayErrores;
    }

    // Clase interna para representar un símbolo
    public class Simbolo {
        String nombre;
        String tipo; // Puedes extender esto para incluir el tipo de dato
        int lineaDeclaracion;

        Simbolo(String nombre, String tipo, int lineaDeclaracion) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.lineaDeclaracion = lineaDeclaracion;
        }
    }

    // Método público para obtener la tabla de símbolos
    public Map<String, Simbolo> getTablaSimbolos() {
        return tablaSimbolos;
    }

    @Override
    public Void visitAssignmentExp(miGramaticaParser.AssignmentExpContext ctx) {
        // Manejar asignaciones
        if (ctx.relationalExp() != null && ctx.ASSIGN() != null && ctx.assignmentExp() != null) {
            String nombreVariable = ctx.relationalExp().getText();
            Token tokenVariable = ctx.relationalExp().start;
            int linea = tokenVariable.getLine();

            // Verificar si la variable ya fue declarada
            if (!tablaSimbolos.containsKey(nombreVariable)) {
                // Si no, agregarla a la tabla de símbolos
                tablaSimbolos.put(nombreVariable, new Simbolo(nombreVariable, "variable", linea));
            }

            // Puedes agregar más lógica para verificar tipos, valores, etc.
        }

        return visitChildren(ctx);
    }

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

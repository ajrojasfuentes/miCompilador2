package com.miCompilador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * La clase GeneradorCodigo extiende miGramaticaBaseVisitor para generar código basado en el árbol
 * de sintaxis analizado por ANTLR.
 */
public class GeneradorCodigo extends miGramaticaBaseVisitor<String> {

    private StringBuilder codigo = new StringBuilder();
    private Map<String, Integer> tablaVariables = new HashMap<>();
    private int contadorEtiquetas = 0;
    private int contadorVariables = 0;

    // Lista de variables globales para la sección .bss
    private List<String> variablesGlobales = new ArrayList<>();

    /**
     * Obtiene el código generado hasta el momento.
     *
     * @return Una cadena que representa el código generado.
     */
    public String getCodigo() {
        return codigo.toString();
    }

    // Métodos auxiliares para generar etiquetas y manejar variables
    /**
     * Genera una nueva etiqueta única.
     *
     * @return El nombre de la nueva etiqueta.
     */
    private String nuevaEtiqueta() {
        return "etiqueta_" + (contadorEtiquetas++);
    }

    /**
     * Genera una nueva variable única.
     *
     * @return El nombre de la nueva variable.
     */
    private String nuevaVariable() {
        return "var_" + (contadorVariables++);
    }

    @Override
    public String visitAssignmentExp(miGramaticaParser.AssignmentExpContext ctx) {
        if (ctx.ASSIGN() != null) {
            // Es una asignación
            String nombreVariable = ctx.relationalExp().getText();
            String valor = visit(ctx.assignmentExp());

            // Si la variable no ha sido declarada, agregarla a la tabla y a variablesGlobales
            if (!tablaVariables.containsKey(nombreVariable)) {
                tablaVariables.put(nombreVariable, contadorVariables++);
                variablesGlobales.add(nombreVariable);
            }

            // Generar código para asignación
            codigo.append("    ; Asignación\n");
            codigo.append("    mov eax, ").append(valor).append("\n");
            codigo.append("    mov [").append(nombreVariable).append("], eax\n");

            return nombreVariable;
        } else {
            // Si no es una asignación, evaluar la expresión
            return visitChildren(ctx);
        }
    }

    @Override
    public String visitPrimaryExp(miGramaticaParser.PrimaryExpContext ctx) {
        if (ctx.NUM() != null) {
            // Retorna el valor numérico
            return ctx.NUM().getText();
        } else if (ctx.ID() != null) {
            // Retorna el nombre de la variable
            return ctx.ID().getText();
        } else if (ctx.PARL() != null && ctx.PARR() != null) {
            // Evaluar la expresión dentro de paréntesis
            return visit(ctx.exp());
        } else {
            return super.visitPrimaryExp(ctx);
        }
    }

    @Override
    public String visitAdditiveExp(miGramaticaParser.AdditiveExpContext ctx) {
        if (ctx.multiplicativeExp().size() == 1) {
            // Solo hay un hijo, retornar el resultado de multiplicativeExp
            return visit(ctx.multiplicativeExp(0));
        } else {
            // Hay más de un multiplicativeExp, manejar las operaciones
            String resultado = visit(ctx.multiplicativeExp(0));
            for (int i = 1; i < ctx.multiplicativeExp().size(); i++) {
                String operando1 = resultado;
                String operando2 = visit(ctx.multiplicativeExp(i));
                String operador = ctx.getChild(2 * i - 1).getText(); // El operador está entre los operandos

                String tempVar = nuevaVariable();

                // Generar código para la operación
                codigo.append("    ; Operación Aditiva\n");
                codigo.append("    mov eax, ").append(operando1).append("\n");
                if (operador.equals("+")) {
                    codigo.append("    add eax, ").append(operando2).append("\n");
                } else {
                    codigo.append("    sub eax, ").append(operando2).append("\n");
                }
                codigo.append("    mov [").append(tempVar).append("], eax\n");

                resultado = tempVar;
            }
            return resultado;
        }
    }

    @Override
    public String visitMultiplicativeExp(miGramaticaParser.MultiplicativeExpContext ctx) {
        if (ctx.unaryExp().size() == 1) {
            return visit(ctx.unaryExp(0));
        } else {
            String resultado = visit(ctx.unaryExp(0));
            for (int i = 1; i < ctx.unaryExp().size(); i++) {
                String operando1 = resultado;
                String operando2 = visit(ctx.unaryExp(i));
                String operador = ctx.getChild(2 * i - 1).getText();

                String tempVar = nuevaVariable();

                // Generar código para la operación
                codigo.append("    ; Operación Multiplicativa\n");
                codigo.append("    mov eax, ").append(operando1).append("\n");
                if (operador.equals("*")) {
                    codigo.append("    imul eax, ").append(operando2).append("\n");
                } else {
                    codigo.append("    ; División\n");
                    codigo.append("    mov ebx, ").append(operando2).append("\n");
                    codigo.append("    cdq\n"); // Extiende eax a edx:eax
                    codigo.append("    idiv ebx\n");
                }
                codigo.append("    mov [").append(tempVar).append("], eax\n");

                resultado = tempVar;
            }
            return resultado;
        }
    }

    @Override
    public String visitIf_stmt(miGramaticaParser.If_stmtContext ctx) {
        String etiquetaElse = nuevaEtiqueta();
        String etiquetaFin = nuevaEtiqueta();

        // Evaluar la condición
        String condicion = visit(ctx.exp());

        // Generar código para la condición
        codigo.append("    ; Condición IF\n");
        codigo.append("    mov eax, ").append(condicion).append("\n");
        codigo.append("    cmp eax, 0\n");
        codigo.append("    je ").append(etiquetaElse).append("\n");

        // Generar código para el bloque verdadero
        visit(ctx.code());

        // Salto al final del IF
        codigo.append("    jmp ").append(etiquetaFin).append("\n");

        // Etiqueta else
        codigo.append(etiquetaElse).append(":\n");

        // Generar código para el bloque else si existe
        if (ctx.if_prime().getChildCount() > 0) {
            visit(ctx.if_prime());
        }

        // Etiqueta fin
        codigo.append(etiquetaFin).append(":\n");

        return null;
    }

    @Override
    public String visitWhile_stmt(miGramaticaParser.While_stmtContext ctx) {
        String etiquetaInicio = nuevaEtiqueta();
        String etiquetaFin = nuevaEtiqueta();

        // Etiqueta de inicio del bucle
        codigo.append(etiquetaInicio).append(":\n");

        // Evaluar la condición
        String condicion = visit(ctx.exp());

        // Generar código para la condición
        codigo.append("    ; Condición WHILE\n");
        codigo.append("    mov eax, ").append(condicion).append("\n");
        codigo.append("    cmp eax, 0\n");
        codigo.append("    je ").append(etiquetaFin).append("\n");

        // Generar código para el bloque del while
        visit(ctx.code());

        // Saltar al inicio del bucle
        codigo.append("    jmp ").append(etiquetaInicio).append("\n");

        // Etiqueta fin del bucle
        codigo.append(etiquetaFin).append(":\n");

        return null;
    }

    @Override
    public String visitFor_stmt(miGramaticaParser.For_stmtContext ctx) {
        String etiquetaInicio = nuevaEtiqueta();
        String etiquetaFin = nuevaEtiqueta();

        // Inicialización
        if (ctx.optionalExp(0).getChildCount() > 0) {
            visit(ctx.optionalExp(0));
        }

        // Etiqueta de inicio del bucle
        codigo.append(etiquetaInicio).append(":\n");

        // Condición
        if (ctx.optionalExp(1).getChildCount() > 0) {
            String condicion = visit(ctx.optionalExp(1));
            codigo.append("    ; Condición FOR\n");
            codigo.append("    mov eax, ").append(condicion).append("\n");
            codigo.append("    cmp eax, 0\n");
            codigo.append("    je ").append(etiquetaFin).append("\n");
        }

        // Bloque del for
        visit(ctx.code());

        // Actualización
        if (ctx.optionalExp(2).getChildCount() > 0) {
            visit(ctx.optionalExp(2));
        }

        // Saltar al inicio del bucle
        codigo.append("    jmp ").append(etiquetaInicio).append("\n");

        // Etiqueta fin del bucle
        codigo.append(etiquetaFin).append(":\n");

        return null;
    }

    @Override
    public String visitPrint_stmt(miGramaticaParser.Print_stmtContext ctx) {
        String id = ctx.ID().getText();

        // Generar código para imprimir la variable
        codigo.append("    ; Imprimir variable\n");
        codigo.append("    mov eax, [").append(id).append("]\n");
        codigo.append("    call print_number\n");

        return null;
    }

    /**
     * Obtiene el código completo generado, incluyendo las secciones de datos, bss y texto.
     *
     * @return Una cadena que representa el código completo generado.
     */
    public String getCodigoCompleto() {
        StringBuilder codigoCompleto = new StringBuilder();

        // Sección de datos
        codigoCompleto.append("section .data\n");
        // Puedes agregar aquí cadenas u otros datos si es necesario

        // Sección .bss para variables sin inicializar
        codigoCompleto.append("section .bss\n");
        for (String var : variablesGlobales) {
            codigoCompleto.append(var).append(" resd 1\n");
        }

        // Sección de código
        codigoCompleto.append("section .text\n");
        codigoCompleto.append("global _start\n");
        codigoCompleto.append("_start:\n");
        codigoCompleto.append(codigo.toString());

        // Salir del programa
        codigoCompleto.append("    ; Salir del programa\n");
        codigoCompleto.append("    mov eax, 1\n");
        codigoCompleto.append("    mov ebx, 0\n");
        codigoCompleto.append("    int 0x80\n");

        // Agregar funciones auxiliares
        codigoCompleto.append("\n; Funciones auxiliares\n");
        codigoCompleto.append("print_number:\n");
        // Implementación de print_number
        codigoCompleto.append("    ; Implementación de print_number\n");
        codigoCompleto.append("    ret\n");

        return codigoCompleto.toString();
    }

}
package com.miCompilador;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name="miCompilador", mixinStandardHelpOptions = true, version = "0.0.1")
public class miCompilador implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {

        // Ruta al archivo de entrada
        String filePath = "/home/ajrf/Workspace/miCompilador2/src/main/resources/prueba.txt";

        // Crear el lexer con el archivo de entrada
        miGramaticaLexer lexer = new miGramaticaLexer(CharStreams.fromFileName(filePath));

        // Crear el buffer de tokens a partir del lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Crear el parser que consumirá los tokens
        miGramaticaParser parser = new miGramaticaParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(new CustomErrorListener());

        // Iniciar el análisis sintáctico
        ParseTree tree = parser.program();

        // Verificar si hubo errores sintácticos
        if (parser.getNumberOfSyntaxErrors() > 0) {
            System.err.println("Se encontraron errores sintácticos. Compilación detenida.");
            return 1; // Código de error
        }

        // Crear una instancia del analizador semántico
        AnalizadorSemantico analizador = new AnalizadorSemantico();

        // Realizar el análisis semántico visitando el árbol de parseo
        analizador.visit(tree);

        // Imprimir la tabla de símbolos
        imprimirTablaDeSimbolos(analizador.getTablaSimbolos());

        // Mensaje de finalización
        System.out.println("Compilación completada con éxito.");

        // Generar el código NASM
        GeneradorCodigo generador = new GeneradorCodigo();
        generador.visit(tree);

        // Obtener el código completo
        String codigoNASM = generador.getCodigoCompleto();

        // Guardar el código en un archivo .asm
        try (PrintWriter out = new PrintWriter("output.txt")) {
            out.println(codigoNASM);
            System.out.println("Generación de código completada. Código guardado en 'output.asm'.");
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo de salida: " + e.getMessage());
            return 1;
        }

// Mensaje de finalización
        System.out.println("Compilación completada exitosamente.");

        return 0;
    }

    // Método para imprimir el árbol AST de forma visual
    private void imprimirArbolAST(ParseTree tree, miGramaticaParser parser) {
        imprimirArbolAST(tree, parser, 0);
    }

    private void imprimirArbolAST(ParseTree tree, miGramaticaParser parser, int nivel) {
        String indentacion = "  ".repeat(nivel);
        String nombreNodo = obtenerNombreNodo(tree, parser);
        System.out.println(indentacion + nombreNodo);

        for (int i = 0; i < tree.getChildCount(); i++) {
            imprimirArbolAST(tree.getChild(i), parser, nivel + 1);
        }
    }

    private String obtenerNombreNodo(ParseTree tree, miGramaticaParser parser) {
        if (tree instanceof TerminalNode) {
            Token simbolo = ((TerminalNode) tree).getSymbol();
            String nombreToken = parser.getVocabulary().getSymbolicName(simbolo.getType());
            if (nombreToken == null) {
                nombreToken = "'" + simbolo.getText() + "'";
            }
            return nombreToken + ": " + simbolo.getText();
        } else {
            String nombreRegla = tree.getClass().getSimpleName().replace("Context", "");
            return nombreRegla;
        }
    }

    // Método para imprimir la tabla de símbolos de forma tabulada
    private void imprimirTablaDeSimbolos(Map<String, AnalizadorSemantico.Simbolo> tablaSimbolos) {
        System.out.println("\nTabla de Símbolos:");
        System.out.println(String.format("%-20s%-15s%-15s", "Nombre", "Tipo", "Línea"));
        System.out.println("-----------------------------------------------------------");
        for (AnalizadorSemantico.Simbolo simbolo : tablaSimbolos.values()) {
            System.out.println(String.format("%-20s%-15s%-15d",
                    simbolo.nombre, simbolo.tipo, simbolo.lineaDeclaracion));
        }
        System.out.println();
    }


    public static void main(String[] args) {
        int exit = new CommandLine(new miCompilador()).execute(args);
        System.exit(exit);
    }
}

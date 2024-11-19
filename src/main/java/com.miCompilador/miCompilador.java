package com.miCompilador;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Clase principal del compilador `miCompilador` que se encarga de convertir código fuente a código ensamblador NASM.
 *
 * <p>Esta clase se hace uso de la biblioteca ANTLR para realizar el análisis lexicográfico,
 * sintáctico y semántico del código fuente. Para la creación de una interfaz de línea de comandos,
 * se emplea la biblioteca `picocli`.</p>
 *
 * <p>El archivo de entrada se especifica como un parámetro, y el archivo de salida generado contiene
 * el código ensamblador NASM.</p>
 *
 * <p>Esta implementación incluye las siguientes fases del compilador:
 * <ul>
 *     <li>Análisis Lexicográfico: Creación de tokens a partir del código fuente.</li>
 *     <li>Análisis Sintáctico: Creación de un árbol sintáctico a partir de los tokens.</li>
 *     <li>Análisis Semántico: Revisión de las reglas semánticas del árbol sintáctico.</li>
 *     <li>Generación de Código: Producción del código ensamblador NASM a partir del árbol sintáctico.</li>
 * </ul>
 * </p>
 */
@CommandLine.Command(name="miCompilador", mixinStandardHelpOptions = true, version = "0.0.1")
public class miCompilador implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Ruta del archivo de entrada")
    private String archivoEntrada;

    @CommandLine.Parameters(index = "1", description = "Ruta del archivo de salida")
    private String archivoSalida;

    /**
     * Método principal que se ejecuta al invocar el compilador desde la línea de comandos.
     *
     * @return Un valor entero que indica el estado de la compilación (0 para éxito, 1 para error).
     * @throws Exception Si ocurre algún error durante la ejecución.
     */
    @Override
    public Integer call() throws Exception {

        // Ruta al archivo de entrada
        String filePath = "/home/ajrf/Workspace/miCompilador2/src/main/resources/prueba.txt";

        // Crear archivo de salida
        BufferedWriter writer = new BufferedWriter(new FileWriter(archivoSalida));

        // Crear el lexer con el archivo de entrada
        miGramaticaLexer lexer = new miGramaticaLexer(CharStreams.fromFileName(archivoEntrada));

        // Remover los errorlisteners por defecto y agregar el personalizado
        lexer.removeErrorListeners();
        LexerErrorListener lexerErrorListener = new LexerErrorListener();
        lexer.addErrorListener(lexerErrorListener);

        // Crear el buffer de tokens a partir del lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Forzar que se complete el tokenizado antes de pasar a la fase sintáctica
        tokens.fill();

        if (lexerErrorListener.hayErrores()) {
            System.err.println("Se encontraron errores léxicos. Compilación detenida.");
            writer.write("Se encontraron errores léxicos. Compilación detenida.");
            writer.newLine();
            return 1; // Código de error
        } else{

            // Crear el parser que consumirá los tokens
            miGramaticaParser parser = new miGramaticaParser(tokens);

            // Eliminar los listeners de errores por defecto y añadir el nuestro
            parser.removeErrorListeners();
            parser.addErrorListener(new CustomErrorListener());

            // Iniciar el análisis sintáctico
            ParseTree tree = parser.program();

            // Verificar si hubo errores sintácticos
            if (parser.getNumberOfSyntaxErrors() > 0) {
                System.err.println("Se encontraron errores sintácticos. Compilación detenida.");
                writer.write("Se encontraron errores sintácticos. Compilación detenida.");
                writer.newLine();
                return 1; // Código de error
            } else {
                // Mensaje de éxito en la fase sintáctica
                System.out.println("Fase sintáctica completada con éxito...");
                writer.write("Fase sintáctica completada con éxito...");
                writer.newLine();
                System.out.println("Iniciando análisis semántico...");
                writer.write("Iniciando análisis semántico...");
                writer.newLine();

                // Crear una instancia del analizador semántico
                AnalizadorSemantico analizador = new AnalizadorSemantico();

                // Realizar el análisis semántico visitando el árbol de parseo
                analizador.visit(tree);

                if (analizador.hayErroresSemanticos()) {
                    System.err.println("Se encontraron errores semánticos. Compilación detenida.");
                    writer.write("Se encontraron errores semánticos. Compilación detenida.");
                    writer.newLine();
                    return 1;
                } else {
                    // Mensaje de éxito en la fase semántica
                    System.out.println("Fase semántica completada con éxito...");
                    writer.write("Fase semántica completada con éxito...");
                    writer.newLine();
                    System.out.println("Iniciando generación de código...");
                    writer.write("Iniciando generación de código...");
                    writer.newLine();

                    // Generar el código NASM
                    GeneradorCodigo generador = new GeneradorCodigo();
                    generador.visit(tree);

                    // Obtener el código completo
                    String codigoNASM = generador.getCodigoCompleto();

                    // Guardar el código en un archivo .asm
                    try (PrintWriter out = new PrintWriter("output.asm")) {
                        out.println(codigoNASM);
                        System.out.println("Generación de código completada. Código guardado en 'output.asm'.");
                        writer.write("Generación de código completada. Código guardado en 'output.asm'.");
                        writer.newLine();
                    } catch (IOException e) {
                        System.err.println("Error al escribir el archivo de salida: " + e.getMessage());
                        writer.write("Error al escribir el archivo de salida: " + e.getMessage());
                        writer.newLine();
                        return 1;
                    }

                    // Imprimir la tabla de símbolos
                    System.out.println(imprimirTablaDeSimbolos(analizador.getTablaSimbolos()));
                    writer.write(imprimirTablaDeSimbolos(analizador.getTablaSimbolos()));
                    writer.newLine();

                    writer.close();

                }

            }

            return 0;

        }

    }

        /**
         * Método para imprimir el árbol AST (Árbol de Sintaxis Abstracta) de forma visual.
         *
         * @param tree El árbol de sintaxis.
         * @param parser El parser utilizado para generar el árbol.
         */
        private void imprimirArbolAST (ParseTree tree, miGramaticaParser parser){
            imprimirArbolAST(tree, parser, 0);
        }

        /**
         * Sobrecarga del método `imprimirArbolAST` para manejar la indentación recursiva.
         *
         * @param tree El árbol de sintaxis.
         * @param parser El parser utilizado para generar el árbol.
         * @param nivel El nivel de indentación actual.
         */
        private void imprimirArbolAST (ParseTree tree, miGramaticaParser parser,int nivel){
            String indentacion = "  ".repeat(nivel);
            String nombreNodo = obtenerNombreNodo(tree, parser);
            System.out.println(indentacion + nombreNodo);

            for (int i = 0; i < tree.getChildCount(); i++) {
                imprimirArbolAST(tree.getChild(i), parser, nivel + 1);
            }
        }

        /**
         * Método para obtener el nombre del nodo actual en el árbol.
         *
         * @param tree El árbol de sintaxis.
         * @param parser El parser utilizado para generar el árbol.
         * @return El nombre del nodo.
         */
        private String obtenerNombreNodo (ParseTree tree, miGramaticaParser parser){
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

        /**
         * Método para imprimir la tabla de símbolos de forma tabulada.
         *
         * @param tablaSimbolos La tabla de símbolos generada durante el análisis semántico.
         */
        private String imprimirTablaDeSimbolos (Map < String, AnalizadorSemantico.Simbolo > tablaSimbolos){
            String tabla = "";
            tabla += "\nTabla de Símbolos:\n";
            tabla += String.format("%-20s%-15s%-15s", "Nombre", "Tipo", "Línea") + "\n";
            tabla +="-----------------------------------------------------------\n";
            for (AnalizadorSemantico.Simbolo simbolo : tablaSimbolos.values()) {
                tabla += String.format("%-20s%-15s%-15d",
                        simbolo.nombre, simbolo.tipo, simbolo.lineaDeclaracion) + "\n";
            }
            return tabla;
        }


    /**
     * Método principal para ejecutar la aplicación de línea de comandos.
     *
     * @param args Los argumentos de línea de comandos.
     */
    public static void main(String[] args) {

        int exit = new CommandLine(new miCompilador()).execute(args);
        System.exit(exit);
    }
}

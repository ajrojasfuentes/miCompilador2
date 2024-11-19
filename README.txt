Cómo ejecutar el programa desde la línea de comandos:

1. Una vez en la línea de comando, navegue entre sus carpetas para ubicarse en la carpeta raíz dentro del proyecto "miCompilador".

2. Como estamos utilizando maven, es necesario correr los siguientes comandos para los paquetes y dependencias, antes de ejecutar el programa:
	mvn clean package

	mvn dependency:copy-dependencies -DoutputDirectory=target/dependency

3. Con esto, ya podemos ejecutar el programa con el siguiente comando (reemplazando la ruta del archivo con la ubicación de su archivo de entrada y su archivo de salida):
	java -cp "target/classes:target/dependency/*" com.miCompilador.miCompilador <ruta al archivo de entrada> <ruta para generar el archivo de salida>

Ejemplo:
	java -cp "target/classes:target/dependency/*" com.miCompilador.miCompilador /home/val/TEC/Compi/Proyecto_2/final/miCompilador2/src/main/resources/prueba.txt /home/val/TEC/Compi/Proyecto_2/final/miCompilador2/src/main/resources/salida.txt

   
Con esto va a recibir la respuesta en consola, además se generará su archivo de salida en la ruta indicada.

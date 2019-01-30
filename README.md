# SistemaCompetitivo
Segunda parte del trabajo de SATD

En el fichero config.txt se define el tamaño del tablero, muros, taxis, personas y el conjunto de máquinas donde ejecutar la prueba

Se ofrece un script para la compilación y un script para la ejecución del sistema en una sola máquina.

* Para compilar: $ ./compila.sh

* Para ejecutar: $ ./ejecuta.sh

Si se desean lanzar los agentes distribuidos en diferentes contenedores de JADE se debe inicializar primero un principal,
luego los secundarios conectándose al principal y finalmente lanzar el agente GestorTablero en uno de ellos.
Detalles sobre el uso de múltiples contenedores: http://jade.tilab.com/doc/administratorsguide.pdf
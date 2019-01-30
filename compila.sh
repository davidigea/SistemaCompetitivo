#!/bin/bash

## Fichero de compilación ##
if [ ! -d bin ]; then
  mkdir bin
fi
javac -cp lib/jade.jar -d bin src/trabajo/parte2/agente/*.java ./src/trabajo/parte2/comportamiento/*.java ./src/trabajo/parte2/dominio/*.java

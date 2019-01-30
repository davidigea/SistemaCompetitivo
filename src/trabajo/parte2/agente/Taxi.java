package trabajo.parte2.agente;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.imtp.leap.JICP.JICPAddress;
import trabajo.parte2.comportamiento.TaxiComportamiento;

public class Taxi extends Agent {
    private int fila, columna;
    private AID gestorTablero;

    @Override
    public void setup() {
        try {
            Object[] parametros = getArguments();
            fila = (int)parametros[0];
            columna = (int)parametros[1];
            gestorTablero = (AID)parametros[2];
            doMove((ContainerID)parametros[3]);
            addBehaviour(new TaxiComportamiento());
        }
        catch(IndexOutOfBoundsException | ClassCastException e) {
            throw new RuntimeException("Error al crear el agente Taxi. Parámetros incorrectos");
        }
    }

    public int getFila() {
        return fila;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public int getColumna() {
        return columna;
    }

    public void setColumna(int columna) {
        this.columna = columna;
    }

    public AID getGestorTablero() {
        return gestorTablero;
    }

    public void setGestorTablero(AID gestorTablero) {
        this.gestorTablero = gestorTablero;
    }
}

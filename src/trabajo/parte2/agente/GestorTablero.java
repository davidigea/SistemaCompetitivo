package trabajo.parte2.agente;

import jade.core.AID;
import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import trabajo.parte2.comportamiento.RecibeConsultaComportamiento;
import trabajo.parte2.comportamiento.RecibeMovimientoComportamiento;
import trabajo.parte2.dominio.Estado;
import trabajo.parte2.dominio.Posicion;
import trabajo.parte2.dominio.Tablero;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GestorTablero extends Agent {
    private Tablero tablero;
    private int numColisiones;

    @Override
    public void setup() {
        tablero = crearTableroAleatorio(32, 32, 110, 110, 110);
        numColisiones = 0;
        System.out.println(tablero + "\n" + "\n");
        addBehaviour(new RecibeConsultaComportamiento(this));
        addBehaviour(new RecibeMovimientoComportamiento(this));
    }

    @Override
    public void takeDown() {
        HashMap<AID, Boolean> h  = tablero.getSigueEnOptimo();
        Iterator it = h.entrySet().iterator();
        int caminosOptimos = 0;
        while(it.hasNext()) {
            if((Boolean)((Map.Entry)it.next()).getValue()) {
                caminosOptimos++;
            }
        }
        System.out.println(numColisiones+"," + ((double) caminosOptimos)/h.size());
    }

    public Tablero getTablero(){return tablero;}

    public void moverTaxi(AID taxi, Posicion posicion){
        tablero.moverTaxi(taxi,posicion);
    }

    // Crea un tablero aleatorio
    // También lanza todos los agentes Taxi
    public Tablero crearTableroAleatorio(int numFilas, int numColumnas,
                                         int numMuros, int numPersonas,
                                         int numTaxis) {
        if((numMuros+numPersonas+numTaxis)>(numFilas*numColumnas)) {
            throw new RuntimeException("No puede crearse un tablero con más elementos que casillas");
        }
        Tablero t = new Tablero(numFilas, numColumnas);
        int f, c;
        ContainerController cc = getContainerController();
        AgentController ac;

        // Añadimos los muros
        while(numMuros>0) {
            f = numeroAleatorio(numFilas);
            c = numeroAleatorio(numColumnas);
            if(t.getCasilla(f,c).getE() == Estado.LIBRE) {
                t.setCasilla(f, c, Estado.MURO, 0);
                numMuros--;
            }
        }

        // Añadimos las personas
        while(numPersonas>0) {
            f = numeroAleatorio(numFilas);
            c = numeroAleatorio(numColumnas);
            if(t.getCasilla(f,c).getE() == Estado.LIBRE) {
                t.setCasilla(f, c, Estado.PERSONA, 0);
                numPersonas--;
            }
        }

        // Añadimos los taxis
        while(numTaxis>0) {
            f = numeroAleatorio(numFilas);
            c = numeroAleatorio(numColumnas);
            if(t.getCasilla(f,c).getE() == Estado.LIBRE) {
                t.setCasilla(f, c, Estado.COCHE, 0);
                Object[] argumentos = new Object[3];
                argumentos[0] = f;
                argumentos[1] = c;
                argumentos[2] = this.getAID();
                try {
                    ac = cc.createNewAgent("Taxi" + numTaxis, "trabajo.parte2.agente.Taxi", argumentos);
                    t.moverTaxi(new AID(ac.getName(), true), new Posicion((int) argumentos[0], (int) argumentos[1]));
                    ac.start();
                }
                catch(StaleProxyException e) {
                    throw new RuntimeException("Error lanzando los taxis");
                }
                numTaxis--;
            }
        }
        return t;
    }

    // Devuelve un número aleatorio tal que 0 <= aleatorio < limite
    private int numeroAleatorio(int limite) {
        SecureRandom s = new SecureRandom();
        byte[] values = new byte[4];
        s.nextBytes(values);
        int aux = ByteBuffer.wrap(values).getInt();
        if (aux<0) { aux = aux*(-1); }
        aux = aux%limite;
        return aux;
    }

    public int getNumColisiones() {
        return numColisiones;
    }

    public void setNumColisiones(int numColisiones) {
        this.numColisiones = numColisiones;
    }
}

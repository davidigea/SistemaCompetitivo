package trabajo.parte2.agente;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.imtp.leap.JICP.JICPAddress;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import trabajo.parte2.comportamiento.RecibeConsultaComportamiento;
import trabajo.parte2.comportamiento.RecibeMovimientoComportamiento;
import trabajo.parte2.dominio.Config;
import trabajo.parte2.dominio.Estado;
import trabajo.parte2.dominio.Posicion;
import trabajo.parte2.dominio.Tablero;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GestorTablero extends Agent {
    private Tablero tablero;
    private int numColisiones;
    private Config config;

    @Override
    public void setup() {
        try {
            config = new Config("config.txt");
        }catch(IOException e){
            System.err.println("Error al leer el fichero, configuración manual");
            config = new Config();
        }
        tablero = crearTableroAleatorio(config.getFilas(), config.getColumnas(), config.getMuros(),
                config.getPersonas(), config.getTaxis());
        numColisiones = 0;
        System.out.println(tablero + "\n" + "\n");
        addBehaviour(new RecibeConsultaComportamiento(this));
        addBehaviour(new RecibeMovimientoComportamiento(this));
    }

    @Override
    public void takeDown() {
        HashMap<AID, Boolean> h  = tablero.getSigueEnOptimo();
        HashMap<AID, Posicion> a  = tablero.getTaxis();
        Iterator it = h.entrySet().iterator();
        int caminosOptimos = 0;
        while(it.hasNext()) {
            Map.Entry m = (Map.Entry)it.next();
            if((Boolean)m.getValue() &!a.containsKey((AID)m.getKey())) {
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
                Object[] argumentos = new Object[4];
                argumentos[0] = f;
                argumentos[1] = c;
                argumentos[2] = this.getAID();

                Object[] keysMaquinas = this.config.getMaquinas().keySet().toArray();
                String keyMaquina = (String)keysMaquinas[numTaxis%keysMaquinas.length];
                argumentos[3] = new ContainerID(keyMaquina,this.config.getMaquinas().get(keyMaquina));

                try {
                    ac = cc.createNewAgent("Taxi" + String.valueOf(numTaxis), "trabajo.parte2.agente.Taxi", argumentos);
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

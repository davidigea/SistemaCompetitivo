package trabajo.parte2.comportamiento;

import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import trabajo.parte2.agente.Taxi;
import trabajo.parte2.dominio.Casilla;
import trabajo.parte2.dominio.Tablero;
import java.util.HashSet;

// Comportamiento de un Taxi cuando quiere pedir una casilla
// Envia una petición de casilla al GestorTablero
public class TaxiComportamiento extends Behaviour {
    // Variables
    private static double PENALIZACION_POR_PASO = 0.02;
    private static double VALOR_PERSONA = 1.0;
    private static double VALOR_MURO_COCHE = -1.0;
    boolean seguirBuscandoPasajero;

    public TaxiComportamiento() {
        super();
        seguirBuscandoPasajero = true;
    }

    // Se encarga de enviar los mensajes de petición de las casillas adyacentes
    @Override
    public void action() {
        int filaActual = ((Taxi)this.myAgent).getFila();
        int columnaActual = ((Taxi)this.myAgent).getColumna();

        // Filas y columnas a comprobar. Sentido horario.
        int[] filas = { Math.abs(filaActual-1), filaActual, filaActual+1, filaActual };
        int[] columnas = { columnaActual, columnaActual+1, columnaActual, Math.abs(columnaActual-1) };

        // Valores de utilidad, sentido horario.
        double[] funcionesUtilidad = funcionUtilidad(filaActual, columnaActual);
        int sentidoMax = 0; // 0^ 1> 2v 3<
        for(int i=1;i<4;i++){
            if(funcionesUtilidad[i]>funcionesUtilidad[sentidoMax]){
                sentidoMax = i;
            }
        }

        // Solo nos movemos si es mejor que quedarse quieto
        if(funcionesUtilidad[sentidoMax]>0.0) {
            solicitarMoverse(filas[sentidoMax],columnas[sentidoMax]);
            if(funcionesUtilidad[sentidoMax] == 1.0) {
                seguirBuscandoPasajero = false;
            }
        }
    }

    // Calcula la función de utilidad para una casilla
    // Algoritmo de programación dinámica
    // Devuelve los valores de utilidad de las casillas norte, este, sur y oeste
    // respecto de (fila, columna)
    private double[] funcionUtilidad(int fila, int columna) {
        // Variables necesarias
        Tablero t = pedirTablero();
        double[][] valoresUtilidad = new double[t.getNumFilas()][t.getNumColumnas()];
        HashSet<Casilla> yaCalculadas = new HashSet<>();
        HashSet<Casilla> ultimosConocidos = new HashSet<>();

        // Rellenamos los valores de utilidad ya conocidos
        for(int i=0; i<t.getNumFilas(); i++) {
            for(int j=0; j<t.getNumColumnas(); j++) {
                Casilla c = t.getCasilla(i, j);
                switch(c.getE()) {
                    case COCHE: case MURO:
                        valoresUtilidad[i][j] = VALOR_MURO_COCHE;
                        yaCalculadas.add(c);
                        break;
                    case PERSONA:
                        valoresUtilidad[i][j] = VALOR_PERSONA;
                        yaCalculadas.add(c);
                        ultimosConocidos.add(c);
                        break;
                    default: valoresUtilidad[i][j] = 0.0; break;
                }
            }
        }

        // Bucle que calcula los valores de todas las casillas libres
        while(yaCalculadas.size()<(t.getNumColumnas()*t.getNumFilas())) {
            HashSet<Casilla> nuevos = new HashSet<>();
            for(Casilla c : ultimosConocidos) {
                Casilla aux;
                try { // Casilla norte
                    aux = t.getCasilla(c.getFila()-1,c.getColumna());
                    if(!yaCalculadas.contains(aux)) {
                        double penalizacion = aux.getNumCochesPasados()>0 ? 1.0/(2*aux.getNumCochesPasados()) : 0.0;
                        valoresUtilidad[aux.getFila()][aux.getColumna()] =
                                valoresUtilidad[c.getFila()][c.getColumna()]
                                        - PENALIZACION_POR_PASO - penalizacion;
                        anyadirValores(aux, yaCalculadas, nuevos);
                    }
                } catch(IndexOutOfBoundsException e) {}
                try { // Casilla sur
                    aux = t.getCasilla(c.getFila()+1,c.getColumna());
                    if(!yaCalculadas.contains(aux)) {
                        double penalizacion = aux.getNumCochesPasados()>0 ? 1.0/(2*aux.getNumCochesPasados()) : 0.0;
                        valoresUtilidad[aux.getFila()][aux.getColumna()] =
                                valoresUtilidad[c.getFila()][c.getColumna()]
                                        - PENALIZACION_POR_PASO - penalizacion;
                        anyadirValores(aux, yaCalculadas, nuevos);
                    }
                } catch(IndexOutOfBoundsException e) {}
                try { // Casilla este
                    aux = t.getCasilla(c.getFila(),c.getColumna()+1);
                    if(!yaCalculadas.contains(aux)) {
                        double penalizacion = aux.getNumCochesPasados()>0 ? 1.0/(2*aux.getNumCochesPasados()) : 0.0;
                        valoresUtilidad[aux.getFila()][aux.getColumna()] =
                                valoresUtilidad[c.getFila()][c.getColumna()]
                                        - PENALIZACION_POR_PASO - penalizacion;
                        anyadirValores(aux, yaCalculadas, nuevos);
                    }
                } catch(IndexOutOfBoundsException e) {}
                try { // Casilla oeste
                    aux = t.getCasilla(c.getFila(),c.getColumna()-1);
                    if(!yaCalculadas.contains(aux)) {
                        double penalizacion = aux.getNumCochesPasados()>0 ? 1.0/(2*aux.getNumCochesPasados()) : 0.0;
                        valoresUtilidad[aux.getFila()][aux.getColumna()] =
                                valoresUtilidad[c.getFila()][c.getColumna()]
                                        - PENALIZACION_POR_PASO - penalizacion;
                        anyadirValores(aux, yaCalculadas, nuevos);
                    }
                } catch(IndexOutOfBoundsException e) {}
            }
            ultimosConocidos = nuevos;
        }
        double[] valores = new double[4];
        try { valores[0] = valoresUtilidad[fila - 1][columna]; }
        catch(IndexOutOfBoundsException e) { valores[0] = -1.0; }
        try { valores[1] = valoresUtilidad[fila][columna+1]; }
        catch(IndexOutOfBoundsException e) { valores[1] = -1.0; }
        try { valores[2] = valoresUtilidad[fila + 1][columna]; }
        catch(IndexOutOfBoundsException e) { valores[2] = -1.0; }
        try { valores[3] = valoresUtilidad[fila][columna-1]; }
        catch(IndexOutOfBoundsException e) { valores[3] = -1.0; }
        return valores;
    }

    // Función que añade los valores a los correspondientes almacenes
    private void anyadirValores(Casilla aux, HashSet<Casilla> yaCalculadas, HashSet<Casilla> nuevos) {
        yaCalculadas.add(aux);
        nuevos.add(aux);
    }

    // Pide el tablero al GestorTablero
    private Tablero pedirTablero() {
        // Enviamos el mensaje al GestorTablero
        ACLMessage m = new ACLMessage(ACLMessage.QUERY_REF);
        m.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
        m.addReceiver(((Taxi)this.myAgent).getGestorTablero());
        this.myAgent.send(m);
        m = this.myAgent.blockingReceive(MessageTemplate.MatchProtocol(
                FIPANames.InteractionProtocol.FIPA_QUERY));

        // Miramos la respuesta
        switch (m.getPerformative()) {
            case ACLMessage.INFORM_REF:
                try {
                    return (Tablero) m.getContentObject();
                }
                catch(UnreadableException e) {
                    throw new RuntimeException("Fallo al deserializar el objeto");
                }
            case ACLMessage.REFUSE: throw new RuntimeException("Tablero no recibido (REFUSE)");
            case ACLMessage.NOT_UNDERSTOOD: throw new RuntimeException("Mensaje enviado incorrecto");
            default: throw new RuntimeException("Performativa no esperada");
        }
    }

    // Solicita realizar un movimiento al GestorTablero
    private void solicitarMoverse(int fila, int columna){
        // Enviamos el mensaje al GestorTablero
        ACLMessage m = new ACLMessage(ACLMessage.PROPOSE);
        m.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
        m.addReceiver(((Taxi)this.myAgent).getGestorTablero());
        m.setContent(String.valueOf(fila) + "\n" + String.valueOf(columna));
        this.myAgent.send(m);
        m = this.myAgent.blockingReceive(MessageTemplate.MatchProtocol(
                FIPANames.InteractionProtocol.FIPA_PROPOSE));

        // Miramos la respuesta
        switch (m.getPerformative()) {
            case ACLMessage.ACCEPT_PROPOSAL:
                ((Taxi)this.myAgent).setFila(fila);
                ((Taxi)this.myAgent).setColumna(columna);
                break;
            case ACLMessage.REJECT_PROPOSAL: break;
            case ACLMessage.REFUSE: throw new RuntimeException("Solicitud rechazada (REFUSE)");
            case ACLMessage.NOT_UNDERSTOOD: throw new RuntimeException("Mensaje enviado incorrecto");
            default: throw new RuntimeException("Performativa no esperada");
        }
    }

    @Override
    public boolean done() {
        //Termina si ha llegado a un pasajero
        Tablero t = pedirTablero();
        if(!seguirBuscandoPasajero) {
            System.out.print(myAgent.getName() + " ha recogido un pasajero en (");
            System.out.println(((Taxi)this.myAgent).getFila() + "," +((Taxi)this.myAgent).getColumna() + ")");
            return true;
        }
        return false;
    }
}

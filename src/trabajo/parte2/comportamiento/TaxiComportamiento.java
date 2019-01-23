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
    private static final double PENALIZACION_POR_PASO = 0.02;
    private static final double VALOR_PERSONA = 10.0;
    private static final double VALOR_MURO_COCHE = -1.0;
    private boolean seguirBuscandoPasajero;

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
            if(solicitarMoverse(filas[sentidoMax],columnas[sentidoMax])
                    && funcionesUtilidad[sentidoMax] == VALOR_PERSONA) {
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
                calcularUtilidadCasilla(t, c, -1, 0, valoresUtilidad, yaCalculadas, nuevos);
                calcularUtilidadCasilla(t, c, +1, 0, valoresUtilidad, yaCalculadas, nuevos);
                calcularUtilidadCasilla(t, c, 0, +1, valoresUtilidad, yaCalculadas, nuevos);
                calcularUtilidadCasilla(t, c, 0, -1, valoresUtilidad, yaCalculadas, nuevos);
            }
            ultimosConocidos = nuevos;
        }
        return valoresNESO(valoresUtilidad, fila, columna);
    }

    // Calcula la utilidad de (c.getFila+d_f, c.getColumna()+d_c)
    private void calcularUtilidadCasilla(Tablero t, Casilla c, int d_f, int d_c,
                                         double[][] valoresUtilidad, HashSet<Casilla> yaCalculadas,
                                         HashSet<Casilla> nuevos) {
        try {
            Casilla aux = t.getCasilla(c.getFila()+d_f,c.getColumna()+d_c);
            if(!yaCalculadas.contains(aux)) {
                double penalizacion = aux.getNumCochesPasados()>0 ? 1.0/(2*aux.getNumCochesPasados()) : 0.0;
                valoresUtilidad[aux.getFila()][aux.getColumna()] =
                        valoresUtilidad[c.getFila()][c.getColumna()]
                                - PENALIZACION_POR_PASO - penalizacion;
                yaCalculadas.add(aux);
                nuevos.add(aux);
            }
        } catch(IndexOutOfBoundsException e) {}
    }

    // Devuelve los valores de utilidad al norte, este, sur y oeste de (fila,columna)
    private double[] valoresNESO(double[][] valoresUtilidad, int fila, int columna) {
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
    // Devuelve true si se permite el movimiento, y false si no
    private boolean solicitarMoverse(int fila, int columna){
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
                return true;
            case ACLMessage.REJECT_PROPOSAL: return false;
            case ACLMessage.REFUSE: throw new RuntimeException("Solicitud rechazada (REFUSE)");
            case ACLMessage.NOT_UNDERSTOOD: throw new RuntimeException("Mensaje enviado incorrecto");
            default: throw new RuntimeException("Performativa no esperada");
        }
    }

    @Override
    public boolean done() {
        //Termina si ha llegado a un pasajero
        return !seguirBuscandoPasajero;
    }
}

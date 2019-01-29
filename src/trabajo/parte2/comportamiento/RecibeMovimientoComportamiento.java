package trabajo.parte2.comportamiento;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import trabajo.parte2.agente.GestorTablero;
import trabajo.parte2.dominio.Estado;
import trabajo.parte2.dominio.Posicion;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

// Comportarmiento del GestorTablero ante una solicitud de movimiento.
// Devuelve si se permite el movimiento o no
public class RecibeMovimientoComportamiento extends AchieveREResponder {
    // Constructor de la clase
    public RecibeMovimientoComportamiento(Agent a) {
        super(a, MessageTemplate.and(MessageTemplate.MatchProtocol(
                FIPANames.InteractionProtocol.FIPA_PROPOSE),
                MessageTemplate.MatchPerformative(ACLMessage.PROPOSE)));
    }

    /*
     * Pre:  ---
     * Post: Este método se ejecuta al recibir una solicitud de movimiento de un taxi
     */
    @Override
    protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
        try {
            StringTokenizer st = new StringTokenizer(request.getContent());
            int fila = Integer.parseInt(st.nextToken());
            int columna = Integer.parseInt(st.nextToken());
            AID taxi = request.getSender();
            Posicion nuevaPosicion = new Posicion(fila,columna);

            ACLMessage inform = request.createReply();
            inform.setPerformative(ACLMessage.REJECT_PROPOSAL);

            if(Posicion.esAlcanzable(
                    ((GestorTablero)myAgent).getTablero().getTaxis().get(taxi),
                    nuevaPosicion)) {
                // Obtenemos casilla antes de ir a ella
                Estado e = ((GestorTablero)myAgent).getTablero().getCasilla(fila, columna).getE();

                switch(e){
                    case PERSONA:
                        // Si la casilla alcanzada es un peatón sacamos al taxi
                        ((GestorTablero) myAgent).moverTaxi(taxi, nuevaPosicion);
                        ((GestorTablero) myAgent).getTablero().setCasilla(fila, columna, Estado.MURO);
                        ((GestorTablero) myAgent).getTablero().borrarTaxi(taxi);
                        inform.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        break;
                    case LIBRE:
                        // actualizar casilla en tablero
                        ((GestorTablero) myAgent).moverTaxi(taxi, nuevaPosicion);
                        inform.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        break;
                    default:
                        // Si hay un muro o taxi, aumentar número de colisiones
                        ((GestorTablero) myAgent).setNumColisiones(((GestorTablero) myAgent).getNumColisiones()+1);
                }

                // Enviamos el mensaje
                System.out.println(((GestorTablero)this.myAgent).getTablero() + "\n\n");
            }

            return inform;
        }
        catch(NoSuchElementException | NumberFormatException e) {
            throw new NotUnderstoodException("El formato del mensaje es incorrecto");
        }
        catch(IndexOutOfBoundsException e) {
            throw new RefuseException("La casilla no existe");
        }
    }
}

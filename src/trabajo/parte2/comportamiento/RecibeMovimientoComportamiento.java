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
import trabajo.parte2.dominio.Casilla;
import trabajo.parte2.dominio.Posicion;

import java.io.IOException;
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
                    nuevaPosicion))
            {
                // actualizar casilla en tablero
                ((GestorTablero)myAgent).moverTaxi(taxi,nuevaPosicion);
                inform.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
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

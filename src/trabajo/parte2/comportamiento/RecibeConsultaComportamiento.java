package trabajo.parte2.comportamiento;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import trabajo.parte2.agente.GestorTablero;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

// Comportarmiento del GestorTablero ante una petición de una casilla.
// Devuelve el contenido de una casilla
public class RecibeConsultaComportamiento extends AchieveREResponder {
    // Constructor de la clase
    public RecibeConsultaComportamiento(Agent a) {
        super(a, MessageTemplate.and(MessageTemplate.MatchProtocol(
                FIPANames.InteractionProtocol.FIPA_QUERY),
                MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF)));
    }

    /*
     * Pre:  ---
     * Post: Este método se ejecuta al recibir una petición de un taxi
     */
    @Override
    protected ACLMessage handleRequest(ACLMessage request) throws RefuseException {
        try {
            ACLMessage inform = request.createReply();
            inform.setPerformative(ACLMessage.INFORM_REF);
            inform.setContentObject(((GestorTablero)myAgent).getTablero());
            return inform;
        }
        catch(IOException e) {
            throw new RefuseException("Error al serializar el tablero");
        }
    }
}

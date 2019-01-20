package trabajo.parte2.comportamiento;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import trabajo.parte2.dominio.Tablero;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

// Comportarmiento del GestorTablero ante una petición de una casilla.
// Devuelve el contenido de una casilla
public class RecibeConsultaComportamiento extends AchieveREResponder {
    // Variables de la clase
    Tablero t;

    // Constructor de la clase
    public RecibeConsultaComportamiento(Agent a, Tablero t) {
        super(a, MessageTemplate.and(MessageTemplate.MatchProtocol(
                FIPANames.InteractionProtocol.FIPA_QUERY),
                MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF)));
        this.t = t;
    }

    /*
     * Pre:  ---
     * Post: Este método se ejecuta al recibir una petición de un taxi
     */
    @Override
    protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
        try {
            StringTokenizer st = new StringTokenizer(request.getContent());
            int fila = Integer.parseInt(st.nextToken());
            int columna = Integer.parseInt(st.nextToken());
            ACLMessage inform = request.createReply();
            inform.setPerformative(ACLMessage.INFORM_REF);
            inform.setContentObject(t.getCasilla(fila, columna));
            return inform;
        }
        catch(NoSuchElementException | NumberFormatException | IOException e) {
            throw new NotUnderstoodException("El formato del mensaje es incorrecto");
        }
        catch(IndexOutOfBoundsException e) {
            throw new RefuseException("La casilla no existe");
        }
    }
}

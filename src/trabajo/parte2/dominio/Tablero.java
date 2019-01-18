package trabajo.parte2.dominio;

/*
 * Clase que implementa un tablero
 */
public class Tablero {
    // Variables de la clase
    private Estado casillas[][];
    private int numCochesAnteriores[][];


    // Constructor de la clase
    public Tablero(int n, int m) {
        casillas = new Estado[n][m];
        numCochesAnteriores = new int[n][m];
        for(int i=0; i<n; i++) {
            for(int j=0; j<m; j++) {
                casillas[i][j] = Estado.LIBRE;
                numCochesAnteriores[i][j] = 0;
            }
        }
    }

    // Devuelve el estado de una casilla
    public Estado obtenerEstadoCasilla(int n, int m) {
        return casillas[n][m];
    }

    // Modifica el estado de una casilla
    public void modificarEstadoCasilla(int n, int m, Estado e) {
        casillas[n][m] = e;
    }

    // Obtiene el número de coches que han pasado por una casilla
    public int obtenerCochesCasilla(int n, int m) {
        return numCochesAnteriores[n][m];
    }

    // Modifica el número de taxis que han pasado por una casilla
    public void modificarCochesCasilla(int n, int m, int nuevaCantidad) {
        numCochesAnteriores[n][m] = nuevaCantidad;
    }
}

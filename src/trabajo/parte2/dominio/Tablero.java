package trabajo.parte2.dominio;

/*
 * Clase que implementa un tablero
 */
public class Tablero {
    // Variables de la clase
    private Casilla casillas[][];


    // Constructor de la clase
    public Tablero(int n, int m) {
        casillas = new Casilla[n][m];
        for(int i=0; i<n; i++) {
            for(int j=0; j<m; j++) {
                casillas[i][j] = new Casilla(Estado.LIBRE, 0);
            }
        }
    }

    // Obtener el contenido de una casilla
    public Casilla getCasilla(int fila, int columna) {
        return casillas[fila][columna];
    }

    // Modificar una casilla
    public void setCasilla(int fila, int columna, Casilla c) {
        casillas[fila][columna] = c;
    }
}

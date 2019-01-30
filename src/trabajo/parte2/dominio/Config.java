package trabajo.parte2.dominio;

import jade.imtp.leap.JICP.JICPAddress;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class Config implements Serializable {
    private HashMap<String,JICPAddress> maquinas;
    private int filas;
    private int columnas;
    private int muros;
    private int personas;
    private int taxis;

    public Config(){
        this.maquinas = new HashMap<>();
        System.out.println("Introducir nombre:ip:puerto;nombre:ip:puerto;...");
        Scanner scan = new Scanner(System.in);
        String input = scan.nextLine();
        for(String maq : input.split(";")){
            anyadirMaquina(maq);
        }
        System.out.print("Filas: ");
        filas = scan.nextInt();
        System.out.print("Columnas: ");
        columnas = scan.nextInt();
        System.out.print("Muros: ");
        muros = scan.nextInt();
        System.out.print("Personas: ");
        personas = scan.nextInt();
        System.out.print("Taxis: ");
        taxis = scan.nextInt();

        scan.close();
    }

    public Config(String fichero) throws IOException {
        this.maquinas = new HashMap<>();
        File file = new File(fichero);
        BufferedReader lector = new BufferedReader(new FileReader(file));
        String linea = leerLinea(lector);
        while(!linea.isEmpty()){
            anyadirMaquina(linea);
            linea = leerLinea(lector);
        }
        filas = Integer.parseInt(leerLinea(lector));
        columnas = Integer.parseInt(leerLinea(lector));
        muros = Integer.parseInt(leerLinea(lector));
        personas = Integer.parseInt(leerLinea(lector));
        taxis = Integer.parseInt(leerLinea(lector));

        lector.close();
    }

    private void anyadirMaquina(String maq){
        String[] tokens = maq.split(":");
        String nombre = tokens[0];
        String ip = tokens[1];
        String puerto = tokens[2];
        this.maquinas.put(nombre,new JICPAddress(ip,puerto,null,null));
    }

    /* Ignora lineas que empiezan por # */
    private String leerLinea(BufferedReader lector) throws IOException{
        String linea = lector.readLine();
        while(linea.startsWith("#")){
            linea = lector.readLine();
        }
        return linea;
    }

    public HashMap<String, JICPAddress> getMaquinas() {
        return maquinas;
    }

    public int getFilas() {
        return filas;
    }

    public int getColumnas() {
        return columnas;
    }

    public int getMuros() {
        return muros;
    }

    public int getPersonas() {
        return personas;
    }

    public int getTaxis() {
        return taxis;
    }
}

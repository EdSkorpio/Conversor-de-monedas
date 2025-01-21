package com.aluracursos.conversordemoneda;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.InputMismatchException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Principal
{
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/8987b3ec63f6f9afc0194f8c/latest/USD";
    private static final Scanner scanner = new Scanner(System.in);

    private enum Moneda {
        USD("Dólar estadounidense"),
        BRL("Real brasileño"),
        ARS("Peso argentino"),
        COP("Peso colombiano"),
        CLP("Peso chileno"),
        BOB("Boliviano boliviano"),
        MXN("Peso Mexicano");

        private final String descripcion;
        Moneda(String descripcion) {
            this.descripcion = descripcion;
        }
        public String getDescripcion() {
            return descripcion;
        }
    }
    public static void main( String[] args ) {
        boolean continuar = true;

        while (continuar) {
            try {
                mostrarMenu();
                int opcion = obtenerOpcionUsuario();
                if (opcion == 0) {
                    continuar = false;
                    System.out.println("\n¡Gracias por haber usado esta API, finalizando el conversor de monedas!");
                    continue;
                }if (opcion == 1){
                    continuar = true;
                    System.out.println("\n********** Seleccione el tipo de moneda para convertir **********\n");
                    Moneda monedaOrigen = seleccionarMoneda();

                    System.out.println("\n********** Seleccione a que denominacion se va a convertir **********\n");
                    Moneda monedaDestino = seleccionarMoneda();

                    System.out.println("\n***** Ahora ingrese el monto a convertir *****");
                    double cantidad = scanner.nextDouble();

                    System.out.println("\n *** Espere un momento ... ***\n");
                    String[] tasas = obtenerTasasDeCambio(monedaOrigen, monedaDestino);
                    double tasaOrigen = Double.parseDouble(tasas[0]);
                    double tasaDestino = Double.parseDouble(tasas[1]);

                    double cantidadEnUSD = cantidad / tasaOrigen;
                    double resultado = cantidadEnUSD * tasaDestino;

                    System.out.println("\n******* El resultado de la conversion de: ");
                    System.out.printf("$ %,.2f %s \n-> %,.2f %s%n",
                            cantidad, monedaOrigen.getDescripcion(), resultado, monedaDestino.getDescripcion());

                    System.out.print("\n*** ¿Quiere realizar otra conversión? (s/n): ");
                    continuar = scanner.next().toLowerCase().startsWith("s");
                }else {
                    System.out.println("\n¡¡¡Error!!! *Opción inválida* Indique si desea continuar o salir\n");
                }
            } catch (Exception e) {
                System.out.println("Ocurrio un error, Finaliza esta conversion, ahora ingrese una opcion valida " + e.getMessage());
                scanner.nextLine();
            }
        }
        scanner.close();
    }
    private static void mostrarMenu() {
        System.out.println("\n***** Bienvenido al Conversor de Monedas, escoja una opcion *****");
        System.out.println("\n1. Realizar conversión de monedas");
        System.out.println("\n0. Salir");
        System.out.print("\n¿Cual es la opcion deseada?");
    }
    private static int obtenerOpcionUsuario() {
        int opcion = scanner.nextInt();
        scanner.nextLine();
        return opcion;
    }
    private static Moneda seleccionarMoneda() {
        while (true) {
            try {
                Moneda[] monedas = Moneda.values();
                for (int i = 0; i < monedas.length; i++) {
                    System.out.printf("%d. %s (%s)%n", i + 1, monedas[i].getDescripcion(), monedas[i].name());
                }
                System.out.print("\n¿Cual es la opcion deseada? ");
                int seleccion = scanner.nextInt();
                scanner.nextLine();

                if (seleccion < 1 || seleccion > monedas.length) {
                    System.out.println("\n¡¡¡Error!!! *Opción inválida* Por favor seleccione un número valido entre 1 y " + monedas.length);
                    continue;
                }
                return monedas[seleccion - 1];
            } catch (InputMismatchException e) {
                System.out.println("\nError: Por favor ingrese un número que sea válido");
                scanner.nextLine();
            }
        }
    }
    private static String[] obtenerTasasDeCambio(Moneda moneda1, Moneda moneda2) throws Exception {
        String[] tasas = new String[2];

        if (!esMonedaValida(moneda1) || !esMonedaValida(moneda2)) {
            throw new IllegalArgumentException("alguna de las monedas no está en la lista");
        }
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Gson gson = new Gson();
            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
            JsonObject rates = jsonResponse.getAsJsonObject("conversion_rates");
            tasas[0] = rates.get(moneda1.name()).getAsString();
            tasas[1] = rates.get(moneda2.name()).getAsString();
        } else {
            throw new RuntimeException("Error al conectar: Código de respuesta " + response.statusCode());
        }
        return tasas;
    }
    private static boolean esMonedaValida(Moneda moneda) {
        return moneda != null;
    }
}

/*
 * @overview        {UdpClient}
 *
 * @version         2.0
 *
 * @author          Dyson Arley Parra Tilano <dysontilano@gmail.com>
 *
 * @copyright       Dyson Parra
 * @see             github.com/DysonParra
 *
 * History
 * @version 1.0     Implementation done.
 * @version 2.0     Documentation added.
 */
package com.project.dev.wirelesspiano.struct;

import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * TODO: Description of {@code UdpClient}.
 *
 * @author Dyson Parra
 * @since Java 17 (LTS), Gradle 7.3
 */
public class UdpClient {

    private int serverPort = 1024;                                                      // Es el puerto mediante el cual se enviarán conexiones.
    private int packetSize = 98;                                                        // Es la longitud de cada paquete a enviar.

    private DatagramSocket socket;                                                      // Como se envian los paquetes
    private DatagramPacket packet;                                                      // Cada paquete.
    private InetAddress address;                                                        // Usada para obtener la ip desde donde llega cada paquete.
    private byte[] request;                                                             // Usada para enviar paquetes de bytes.
    private byte[] response;                                                            // Usada para recibir paquetes de bytes.
    private String strResponse;                                                         // Paquetes recibidos en string.

    /**
     * TODO: Description of method {@code UdpClient}.
     *
     * @param ipAddres   es la ip del servidor.
     * @param serverPort es el puerto del servidor.
     * @param packetSize es el tamaño de los paquetes.
     * @throws java.net.UnknownHostException
     * @throws java.net.SocketException
     */
    public UdpClient(String ipAddres, String serverPort, int packetSize) throws UnknownHostException, SocketException {
        this.address = InetAddress.getByName(ipAddres);                         // Obtiene la direccion IP del Server especificada como parámetro.
        this.serverPort = Integer.valueOf(serverPort);
        this.packetSize = packetSize;

        socket = new DatagramSocket();
        request = new byte[packetSize];
    }

    /**
     * FIXME: Description of method {@code send}. Envía un mensaje.
     *
     * @param requestType es el tipo de requerimiento que se enviará al servidor.
     * @param status      es el array con los estados de las teclas.
     * @param timeout     es la cantidad de tiempo que se intenta enviar la petición.
     * @return
     */
    public boolean send(int requestType, byte[] status, int timeout) {
        switch (requestType) {
            case 0:
                this.request[0] = 0;
                break;

            case 1:
                this.request[0] = 1;
                this.request[1] = status[0];
                break;

            case 2:
                this.request = status;
                break;
        }

        packet = new DatagramPacket(request, request.length, address, serverPort);  // Inicializa paquete para enviar mensajes.

        try {                                                                       // Intenta enviar un paquete.
            socket.setSoTimeout(timeout);                                           // Indica tiempo máximo de espera para enviar un paquete.
            socket.send(packet);                                                    // Intenta enviar un paquete.
        } catch (Exception ignored) {                                                 // Si no pudo enviar un paquete.
        }

        response = new byte[5];                                                     // Usada para recibir paquetes de bytes.
        packet = new DatagramPacket(response, response.length);                     // Crea paquete para recibir respuesta del server.

        try {                                                                       // Espera a que llegue un paquete.
            socket.receive(packet);                                                 // Intenta obtener un paquete.
        } catch (Exception ignored) {                                               // Si no pudo recibir un paquete.
        }

        strResponse = new String(response, StandardCharsets.UTF_8);                 // Obtiene el valor en String del paquete recibido.
        // Devuelve indicando si la solicitud fue exitosa.
        return response[0] == 't' && response[1] == 'r' && response[2] == 'u' && response[3] == 'e';
    }
}

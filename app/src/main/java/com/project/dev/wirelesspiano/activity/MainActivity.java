/*
 * @overview        {MainActivity}
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
package com.project.dev.wirelesspiano.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.project.dev.wirelesspiano.struct.UdpClient;
import com.project.dev.wirelesspiano.R;

/**
 * TODO: Description of {@code MainActivity}.
 *
 * @author Dyson Parra
 * @since Java 17 (LTS), Gradle 7.3
 */
public class MainActivity extends AppCompatActivity {

    /*
     * Variables asociadas con elementos la vista.
     */
    private EditText editTextIpAddress, editTextPort;
    private Spinner spnInstrument;
    private Button btnConnect;

    /*
     * Variables locales.
     */
    private UdpClient udpClient;                                        // Indica un servidor udp.
    private byte[] instrumentId;                                        // Indica el id del instrumento a utilizar.
    private String ipAddres = "";                                       // Indica la ip del servidor.
    private String serverPort = "";                                     // Indica el puerto de conexión al servidor.

    /**
     * FIXME: Description of method {@code onCreate}. Invocado cuando se crea el activity.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Crea instancia del activity y la asocia con la vista.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Asocia variables locales con elementos de la vista.
        editTextIpAddress = findViewById(R.id.editTextAddress);
        editTextPort = findViewById(R.id.editTextPort);
        spnInstrument = findViewById(R.id.spnInstrument);
        btnConnect = findViewById(R.id.btnConnect);

        // Se indica a StrictMode que en su política deathreads no tenga en cuenta los accesos a la red.
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());

        // Comportamiento del botón conectar.
        btnConnect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ipAddres = String.valueOf(editTextIpAddress.getText().toString());
                serverPort = String.valueOf(editTextPort.getText());
                if ("".equals(ipAddres))
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.noIpAddressMsg), Toast.LENGTH_SHORT).show();
                else if ("".equals(serverPort))
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.noPortMsg), Toast.LENGTH_SHORT).show();
                else {
                    try {
                        udpClient = new UdpClient(ipAddres, serverPort, 98);

                        instrumentId = String.valueOf(spnInstrument.getSelectedItemId()).getBytes();
                        instrumentId[0] -= 48;

                        // Si se pudo conectar con el servidor.
                        if (udpClient.send(1, instrumentId, 3000)) {
                            // Crea un intent para iniciar el activity con el piano.
                            Intent intent = new Intent(MainActivity.this, PianoActivity.class);

                            // Manda el la ip y el puerto del servidor.
                            intent.putExtra("ipAddres", ipAddres);
                            intent.putExtra("serverPort", serverPort);

                            // Inicia el activit
                            startActivity(intent);
                        } // Si no se pudo conectar con el servidor.
                        else
                            Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.invalidServerMsg), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.invalidServerMsg), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}

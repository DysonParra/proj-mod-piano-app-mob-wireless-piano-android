/*
 * @fileoverview    {PianoActivity}
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.project.dev.wirelesspiano.struct.Piano;
import com.project.dev.wirelesspiano.struct.UdpClient;
import com.project.dev.wirelesspiano.R;

/**
 * TODO: Definición de {@code PianoActivity}.
 *
 * @author Dyson Parra
 * @since 1.8
 */
public class PianoActivity extends Activity {

    /*
     * Variables asociadas con elementos la vista.
     */
    private RelativeLayout relativeLayoutBlancas;
    private RelativeLayout relativeLayoutNegras;

    /*
     * Variables locales.
     */
    private Intent mainActivity;                                        // Usada para iniciar el activity principal.
    private boolean unableConnect;                                      // Si no fue posible conectarse al servidor.
    private UdpClient udpClient;                                        // Cliente udp.
    private static Piano piano;
    private static int C4;
    private static int noteQuantity;
    private static byte[] status;

    /*
     * Variables obtenidas desde un activity anterior.
     */
    private String ipAddres;                            // Ip del servidor.
    private String serverPort;                          // Puerto de conexión al servidor.

    /**
     * Invocado cuando se crea el activity.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Crea instancia del activity y la asocia con la vista.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piano);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Asocia variables locales con elementos de la vista.
        relativeLayoutBlancas = findViewById(R.id.relativeLayoutBlancas);
        relativeLayoutNegras = findViewById(R.id.relativeLayoutNegras);

        // Obtiene los elementos enviados desde el activity "Tuner" o "Edit music sheet" y los asigna a variables locales.
        ipAddres = (String) getIntent().getExtras().getSerializable("ipAddres");
        serverPort = (String) getIntent().getExtras().getSerializable("serverPort");

        // Obtiene el alto en píxeles del dispositivo.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Asigna valores a variables locales
        try {
            udpClient = new UdpClient(ipAddres, serverPort, 89);                                // crea un nuevo servidor udp.
        } catch (Exception ignored) {

        }

        noteQuantity = 89;                                                                      // Indica la cantidad de notas del piano.
        piano = new Piano(noteQuantity, 4, 0);                                                  // Crea un nuevo piano.
        C4 = piano.getC4();                                                                     // Indica el número de do que será el central
        status = piano.getStatus();                                                             // Indica el aray con los estados de cada tecla.

        mainActivity = new Intent(PianoActivity.this, MainActivity.class);                      // Inicializa el intent para iniciar el activity principal
        mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // Agrega banderas indicando que se cerrará toda la pila de actividades.
        unableConnect = false;                                                                  // Indica que fue posible conectarse al servidor.

        // Obtiene el alcho y alto de cada tecla.
        int whiteKeyHeigth = metrics.heightPixels;                              // Asigna valor al alto de las teclas blancas.
        int whiteKeyWith = (int) (whiteKeyHeigth * 0.2);                        // Asigna valor al ancho de las teclas blancas.
        int blackKeyHeigth = (int) (whiteKeyHeigth * 0.4);                        // Asigna valor al alto de las teclas negras.
        int blackKeyWith = (int) (whiteKeyHeigth * 0.1);                        // Asigna valor al ancho de las teclas negras.

        int nroBlancas = 0;                                                     // Inicializa la cantidad de notas blancas.
        int nroNegras = 0;                                                      // Inicializa la cantidad de notas negras.

        // Recorre las teclas.
        for (int i = 1; i < noteQuantity; i++) {
            piano.getStatus()[i] = 0;                                           // Pone el estado de la tecla actual en se mantiene.

            ImageView key = new ImageView(this);                         // Crea un nuevo imageView que tendrá la tecla.
            key.setId(i);                                                       // Pone identificadora la tecla actual.
            piano.getKeys()[i] = key;                                           // Agrega la tecla actual al piano.

            switch (i % 12) {
                // Si es una tecla negra.
                case 2:
                case 5:
                case 7:
                case 10:
                case 0:
                    // Si la última tecla no es una negra.
                    if (i != noteQuantity - 1) {
                        RelativeLayout.LayoutParams negraParams = new RelativeLayout.LayoutParams(blackKeyWith, blackKeyHeigth);
                        key.setAdjustViewBounds(true);
                        key.setScaleType(ImageView.ScaleType.FIT_XY);

                        key.setLayoutParams(negraParams);

                        negraParams.leftMargin = (((i - nroNegras - 1) * whiteKeyWith) - (blackKeyWith / 2));
                        negraParams.topMargin = 0;

                        key.setImageResource(R.drawable.negra);

                        key.setOnTouchListener(new BlackKeyTouchListener());
                        relativeLayoutNegras.addView(key);
                    }
                    nroNegras++;
                    break;

                // Si es una tecla blanca.
                case 1:
                case 3:
                case 4:
                case 6:
                case 8:
                case 9:
                case 11:
                    RelativeLayout.LayoutParams blancaParams = new RelativeLayout.LayoutParams(whiteKeyWith, whiteKeyHeigth);
                    key.setAdjustViewBounds(true);
                    key.setScaleType(ImageView.ScaleType.FIT_XY);

                    key.setLayoutParams(blancaParams);

                    blancaParams.leftMargin = (nroBlancas * whiteKeyWith) + 1;
                    blancaParams.topMargin = 0;

                    if (C4 == i) {
                        key.setImageResource(R.drawable.blanca_c4);
                        key.setOnTouchListener(new C4KeyTouchListener());
                    } else {
                        key.setImageResource(R.drawable.blanca);
                        key.setOnTouchListener(new WhiteKeyTouchListener());
                    }

                    nroBlancas++;

                    relativeLayoutBlancas.addView(key);

                    break;

                // Si no es una tecla.
                default:
                    break;
            }
        }
    }

    /**
     * FIXME: Definición de {@code BlackKeyTouchListener}. Comportamiento de las teclas negras.
     */
    private class BlackKeyTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ((ImageView) v).setImageResource(R.drawable.negra_touched);
                status[v.getId()] = 1;
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                ((ImageView) v).setImageResource(R.drawable.negra);
                status[v.getId()] = -1;

            }

            // Si no se ha activado bandera que indica que una petición no llegó al servidor y la petición actual no llega al servidor en el tiempo indicado.
            if (!unableConnect && !udpClient.send(2, status, 2000)) {
                // Indica que no fue posible conectarse al servidor.
                unableConnect = true;

                // Crea un mensaje de alerta indicando error de conexión.
                new AlertDialog.Builder(PianoActivity.this)
                        .setTitle(PianoActivity.this.getString(R.string.errorTitle))
                        .setMessage(PianoActivity.this.getString(R.string.errorMsg))
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            // Si se minimiza el mensaje sin escoger ninguna opción.
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                // Reinicia bandera que indica que no fue posible conectarse al servidor.
                                unableConnect = false;
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            // Si se indica cancelar.
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Reinicia bandera que indica que no fue posible conectarse al servidor.
                                unableConnect = false;
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            // Si se indica Ok.
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Envía petición de cierre del piano al servidor udp.
                                udpClient.send(0, null, 1000);

                                // Inicia el activity principal.
                                startActivity(mainActivity);
                            }
                        }).show();
            }
            status[v.getId()] = 0;

            return true;
        }
    };

    /**
     * FIXME: Definición de {@code WhiteKeyTouchListener}. Comportamiento de las teclas blancas.
     */
    private class WhiteKeyTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ((ImageView) v).setImageResource(R.drawable.blanca_touched);
                status[v.getId()] = 1;
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                ((ImageView) v).setImageResource(R.drawable.blanca);
                status[v.getId()] = -1;
            }

            // Si no se ha activado bandera que indica que una petición no llegó al servidor y la petición actual no llega al servidor en el tiempo indicado.
            if (!unableConnect && !udpClient.send(2, status, 2000)) {
                // Indica que no fue posible conectarse al servidor.
                unableConnect = true;

                // Crea un mensaje de alerta indicando error de conexión.
                new AlertDialog.Builder(PianoActivity.this)
                        .setTitle(PianoActivity.this.getString(R.string.errorTitle))
                        .setMessage(PianoActivity.this.getString(R.string.errorMsg))
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            // Si se minimiza el mensaje sin escoger ninguna opción.
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                // Reinicia bandera que indica que no fue posible conectarse al servidor.
                                unableConnect = false;
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            // Si se indica cancelar.
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Reinicia bandera que indica que no fue posible conectarse al servidor.
                                unableConnect = false;
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            // Si se indica Ok.
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Envía petición de cierre del piano al servidor udp.
                                udpClient.send(0, null, 1000);

                                // Inicia el activity principal.
                                startActivity(mainActivity);
                            }
                        }).show();
            }
            status[v.getId()] = 0;

            return true;
        }
    };

    /**
     * FIXME: Definición de {@code C4KeyTouchListener}. Comportamiento del do central.
     */
    private class C4KeyTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ((ImageView) v).setImageResource(R.drawable.blanca_c4_touched);
                status[v.getId()] = 1;
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                ((ImageView) v).setImageResource(R.drawable.blanca_c4);
                status[v.getId()] = -1;
            }

            // Si no se ha activado bandera que indica que una petición no llegó al servidor y la petición actual no llega al servidor en el tiempo indicado.
            if (!unableConnect && !udpClient.send(2, status, 2000)) {
                // Indica que no fue posible conectarse al servidor.
                unableConnect = true;

                // Crea un mensaje de alerta indicando error de conexión.
                new AlertDialog.Builder(PianoActivity.this)
                        .setTitle(PianoActivity.this.getString(R.string.errorTitle))
                        .setMessage(PianoActivity.this.getString(R.string.errorMsg))
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            // Si se minimiza el mensaje sin escoger ninguna opción.
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                // Reinicia bandera que indica que no fue posible conectarse al servidor.
                                unableConnect = false;
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            // Si se indica cancelar.
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Reinicia bandera que indica que no fue posible conectarse al servidor.
                                unableConnect = false;
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            // Si se indica Ok.
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Envía petición de cierre del piano al servidor udp.
                                udpClient.send(0, null, 1000);

                                // Inicia el activity principal.
                                startActivity(mainActivity);
                            }
                        }).show();
            }
            status[v.getId()] = 0;

            return true;
        }
    };

    /**
     * FIXME: Definición de {@code onKeyDown}. Comportamiento del botón atrás.
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Si el botón es el de atrás.
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Crea un mensaje de alerta preguntando si desea salir sin guardar.
            new AlertDialog.Builder(this)
                    .setTitle(PianoActivity.this.getString(R.string.btnBack))
                    .setMessage(PianoActivity.this.getString(R.string.msgComeBack))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        // Si se indica si ir atrás.
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Envía petición de cierre del piano al servidor udp.
                            udpClient.send(0, null, 1000);

                            // Inicia el activity principal.
                            startActivity(mainActivity);
                        }
                    }).show();
        }
        return true;
    }
}

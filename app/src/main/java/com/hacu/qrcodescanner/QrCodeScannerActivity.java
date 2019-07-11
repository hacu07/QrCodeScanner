package com.hacu.qrcodescanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;
import static android.Manifest.permission.CAMERA;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/*
* Implementa la interfaz de scanner para actuar como escaner de codigo QR y de codigo de barras
 */
public class QrCodeScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private static final int REQUEST_CAMERA = 1;
    private static final String TAG = "QRCodeScanner" ;
    //Proporciona la vista para escanear el codigo qr o de barras
    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_scanner);

        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);//Ocupa en el layout de la actividad  el scanner

        //Validar Permisos
        int currentapiVersion = Build.VERSION.SDK_INT;//Obtiene la version actual de android del dispotivo
        if (currentapiVersion >= Build.VERSION_CODES.M){ //si es mayor-igual a las 6.0
            if (checkPermission()){//Si el permiso ya ha sido aceptado
                imprimirMensaje("Permiso aceptado");
            }else{
                requestPermissions();//Solicita los permisos
            }
        }
    }


    /*
    * Este metodo contiene la logica para manejar el resultado del escaneo del escaner QR/Barras
    * */
    @Override
    public void handleResult(final Result result) {
        final String resultado =  result.getText();
        Log.d(TAG,result.getText());
        Log.d(TAG,result.getBarcodeFormat().toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Resultado Del Escaneo");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mScannerView.resumeCameraPreview(QrCodeScannerActivity.this);
                    }
                }
        );
        builder.setNeutralButton("Visitar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent navegarWebIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(resultado));
                startActivity(navegarWebIntent);
            }
        });
        builder.setMessage(result.getText());
        AlertDialog alert1 = builder.create();
        alert1.show();
    }

    //Respuesta a solicitud de permiso
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CAMERA://Si es el mismo codigo de CAMARA
                if (grantResults.length>0){
                    boolean cameraAccepted =  grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted)
                        imprimirMensaje("Permiso de CAMARA aceptado.");
                }else{
                    imprimirMensaje("Permiso DENEGADO, No puedes acceder a la camara.");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if (shouldShowRequestPermissionRationale(CAMERA)){
                            showMessageOkCancel("Debe tener acceso a ambos permisos.",
                                    new DialogInterface.OnClickListener(){
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                                requestPermissions(new String[]{CAMERA},REQUEST_CAMERA);
                                            }
                                        }
                                    }
                            );
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOkCancel(String mensaje, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(QrCodeScannerActivity.this)
            .setMessage(mensaje)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancelar",null)
            .create()
            .show();
    }

    //Solicita el permiso de la camara al Usuario
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,new String[]{CAMERA}, REQUEST_CAMERA);
    }

    //Devuelve un boolean indicando si el permiso a sido aceptado antes
    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void imprimirMensaje(String mensaje){
        Toast.makeText(this,mensaje,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int currentApiVersion = Build.VERSION.SDK_INT;
        if(currentApiVersion >= Build.VERSION_CODES.M){
            if (checkPermission()){//Si el permiso ya a sido aceptado
                if (mScannerView == null){
                    mScannerView = new ZXingScannerView(this);
                    setContentView(mScannerView);
                }
                mScannerView.setResultHandler(this);
                mScannerView.startCamera();//Inicia la camara para capturar el codigo QR/Barras
            } else {
                requestPermissions();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScannerView.stopCamera();
    }
}

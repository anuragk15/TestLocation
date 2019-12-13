package com.example.testlocation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
        , LocationListener {


    private static final int PLAY_SERVICES_RESOLTUION_REQUEST = 5000;
    private static final int ALL_PERMISSIONS_RESULT = 1111;
    ArrayList<String> permReqArray = new ArrayList<>();
    ArrayList<String> permGrantedArray = new ArrayList<>();
    ArrayList<String> permRejArray = new ArrayList<>();
    TextView location_view;
    Location location;
    boolean buildVersion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    private FusedLocationProviderClient fusedLocationProviderClient;
    GoogleApiClient client;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        location_view = findViewById(R.id.location_view);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //adding permissions
        permReqArray.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permReqArray.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest();
        if (buildVersion && permReqArray.size() > 0) {
            requestPermissions(permReqArray.toArray(new String[permReqArray.size()]), ALL_PERMISSIONS_RESULT);
        }


    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int errCode = apiAvailability
                .isGooglePlayServicesAvailable(this);
        if (errCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = apiAvailability.getInstance().getErrorDialog(this, errCode, errCode, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Toast.makeText(MainActivity.this, "No services",
                            Toast.LENGTH_LONG)
                            .show();

                    finish();
                }
            });
            errorDialog.show();
            return false;
        } else
            Toast.makeText(this, "All is good", Toast.LENGTH_LONG).show();

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (client != null)
            client.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (client != null && client.isConnected()) {
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(new LocationCallback() {
            });
            client.disconnect();
        }
    }


    private ArrayList<String> permissionsToRequest() {
        ArrayList<String> permRejArray = new ArrayList<>();
        for (String perm : permReqArray)
            if (!hasPermissions(perm)) {
                Log.d("Main", "Permission REJECTED = " + perm);
                permRejArray.add(perm);
            }
        return permRejArray;

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        boolean t = checkPlayServices();
        Log.d("Main", "Play Service Presence = " + t);
        permRejArray = permissionsToRequest();
        if (permRejArray.size() > 0) {
            if (buildVersion)
                if (shouldShowRequestPermissionRationale(permReqArray.get(0)))
                    new AlertDialog.Builder(this).setMessage("These permissions are necessary")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (buildVersion)
                                        requestPermissions(permRejArray.toArray(new String[permRejArray.size()]), ALL_PERMISSIONS_RESULT);
                                }
                            }).setNegativeButton("Cancel", null)
                            .create().show();

        }

    }

    private boolean hasPermissions(String perm) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
        else
            return true;
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            location_view.setText(location.getLatitude() + " " + location.getLongitude());
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        permRejArray = permissionsToRequest();
        if (permRejArray.size() > 0)
            return;
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    location_view.setText(location.getLatitude() + " " + location.getLongitude());
                }

            }
        });
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

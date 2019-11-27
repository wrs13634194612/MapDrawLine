package com.example.administrator.testz;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLongClickListener;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.VisibleRegion;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.Polyline;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wrs on 22/11/2019,上午 10:59
 * projectName: MapWay
 * packageName: task.pdioms.ufi.com.mapway
 */
public class MainActivity extends FragmentActivity implements View.OnClickListener, OnMapClickListener {
    private MapView mapView;
    private AMap aMap;
    private double droneLocationLat = 181, droneLocationLng = 181;
    private boolean isAdd = false;
    private final Map<Integer, Marker> mMarkers = new ConcurrentHashMap<Integer, Marker>();
    private Marker droneMarker = null;
    private Button locate, add, clear;

    private Polyline polyline;

    ArrayList<LatLng> latLngList = new ArrayList<LatLng>();

    LatLng[] latLngs_cross_minus_180 = {
            new LatLng(36.777358, 117.114289, false),
            new LatLng(34.859492, 113.582008, false),
            new LatLng(31.990562, 117.115108, false)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.map);
        locate = (Button) findViewById(R.id.locate);
        add = (Button) findViewById(R.id.add);
        clear = (Button) findViewById(R.id.clear);
        locate.setOnClickListener(this);
        add.setOnClickListener(this);
        clear.setOnClickListener(this);
        mapView.onCreate(savedInstanceState);

        initMapView();

        //跨越180°的线
        addBeyond180Polylines();
    }


    private void addBeyond180Polylines() {

     /*   latLngList.clear();
        for (LatLng latLng : latLngs_cross_minus_180) {
            latLngList.add(latLng);
        }

        aMap.addPolyline((new PolylineOptions())
                .addAll(latLngList)
                .width(5)
                .setDottedLine(false)
                .color(Color.RED)
        );*/
    }

    private void initMapView() {
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.setOnMapClickListener(this);// add the listener for click for amap object
        }

//        LatLng shenzhen = new LatLng(22.5362, 113.9454);
        LatLng shenzhen = new LatLng(30.2781, 120.1238);
        aMap.addMarker(new MarkerOptions().position(shenzhen).title("Marker in Shenzhen"));
        aMap.moveCamera(CameraUpdateFactory.newLatLng(shenzhen));
    }


    @Override
    public void onMapClick(final LatLng point) {
        if (isAdd == true) {

            Log.e("TAG", "onMapClick:" + point);

            markWaypoint(point);

            //   latLngList.clear();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);


                        latLngList.add(point);

                        Log.e("TAG", "start_thread: " + latLngList.toString());
                        aMap.addPolyline((new PolylineOptions())
                                .addAll(latLngList)
                                .width(10)
                                .setDottedLine(false)
                                .color(Color.BLACK)
                        );
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }).start();


        } else {
            setResultToToast("Cannot Add Waypoint");
        }
    }


    // Update the drone location based on states from MCU.
    private void updateDroneLocation() {

        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.smile_face_check));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }
                if (checkGpsCoordination(droneLocationLat, droneLocationLng)) {
                    droneMarker = aMap.addMarker(markerOptions);
                }
            }
        });
    }


    private void cameraUpdate() {
        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        float zoomlevel = (float) 18.0;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pos, zoomlevel);
        aMap.moveCamera(cu);

    }

    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    private void setResultToToast(final String string) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markWaypoint(LatLng point) {
        //Create MarkerOptions object
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.rb_news_normal));
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        Marker marker = aMap.addMarker(markerOptions);
        mMarkers.put(mMarkers.size(), marker);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locate: {
                updateDroneLocation();
                cameraUpdate(); // Locate the drone's place
                break;
            }
            case R.id.add: {
                enableDisableAdd();
                break;
            }
            case R.id.clear: {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        aMap.clear();
                        latLngList.clear();
                    }
                });
                updateDroneLocation();
                break;
            }
        }
    }


    private void enableDisableAdd() {
        if (isAdd == false) {
            isAdd = true;
            add.setText("Exit");
        } else {
            isAdd = false;
            add.setText("Add");
        }
    }
}

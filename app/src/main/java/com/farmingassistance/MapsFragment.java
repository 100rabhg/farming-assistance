package com.farmingassistance;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Objects;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int DEFAULT_ZOOM = 15;
    private static final float INDIA_ZOOM = 5.31f;
    private static final String KEY_LOCATION = "location";
    private static final String TAG = MapsFragment.class.getSimpleName();
    private static final LatLng defaultLocation = new LatLng(23.379610, 78.596087);
    private final static int POLYLINE_COLOR_YELLOW= 0xFFFFEB3B;
    private  final static  int POLYGON_COLOR_YELLOW_LIGHT = 0x22FFFF00;
    private final static int POLYLINE_WIDTH_PX = 10;
    private final ArrayList<LatLng> list = new ArrayList<>();
    private final ArrayList<Marker> markerArray = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Polyline polyline;
    private GoogleMap map;
    private boolean locationPermissionGranted = false;
    private Location lastKnownLocation;
    private ActivityResultLauncher<String[]> locationPermissionRequest;

    private  Polyline white_Polyline;
    private Button btnAddPoint, btnCloseLoop;
    private ImageView aimImage;
    private RelativeLayout addLayout;
    private RelativeLayout resultLayout;
    private ImageView backImage;
    private ImageView completeImage;
    private TextView ansDistanceAddLayout;
    private TextView ansDistance;
    private TextView ansArea;
    private float distancesSum=0;
    private boolean isTaskComplete=false;
    private Polygon polygon;
    private final ArrayList<String> areaUnitList = new ArrayList<>();
    private final ArrayList<String> lengthUnitList = new ArrayList<>();
    private final ArrayList<TwoString> ansAreaUnitList = new ArrayList<>();
    private final ArrayList<TwoString> ansLengthUnitList = new ArrayList<>();
    private View areaLinearLayout;
    private View ansDistanceLayout;
    private ListView converterLengthUnit;
    private ListView converterAreaUnit;
    private double areaInMeterSquare;

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void backOneStep() {
        converterLengthUnit.setVisibility(View.GONE);
        converterAreaUnit.setVisibility(View.GONE);
        if(polyline !=null ) polyline.remove();
        if(list.size()>1){
            distancesSum -=calculateLocationDifference(list.get(list.size()-1),list.get(list.size()-2));
            if(distancesSum>=1000){
                ansDistanceAddLayout.setText(String.format("%.2f",(distancesSum/1000))+" Km");
            }
            else {
                ansDistanceAddLayout.setText((int)distancesSum + " m");
            }
        }
        else{
            distancesSum = 0;
            ansDistanceAddLayout.setText("0 m");
        }

        list.remove(list.size()-1);
        polyline = map.addPolyline(new PolylineOptions()
                .addAll(list)
                .color(POLYLINE_COLOR_YELLOW)
                .width(POLYLINE_WIDTH_PX)
        );
                markerArray.get(markerArray.size()-1).remove();
                markerArray.remove(markerArray.size()-1);
        Polyline temp = null;
        if(white_Polyline != null){
            temp = white_Polyline;
        }
        if(!list.isEmpty() ){
            white_Polyline = map.addPolyline(new PolylineOptions()
                    .add(map.getCameraPosition().target)
                    .add(list.get(list.size()-1))
                    .color(0xffffffff)
                    .width(4)
            );
        }
        if (temp != null) temp.remove();
        btnCloseLoop.setVisibility(View.INVISIBLE);
        btnAddPoint.setVisibility(View.VISIBLE);
        if(list.isEmpty()){
            backImage.setBackground(getResources().getDrawable(R.drawable.circle_grey, Objects.requireNonNull(getActivity()).getTheme()));
        }
    }

    private static double convertToRadian(double input) {
        return input * Math.PI / 180;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
         locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                locationPermissionGranted = true;
                                System.out.println("permission granted");
                                updateLocationUI();
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                locationPermissionGranted = true;
                                System.out.println("course location granted");
                                updateLocationUI();
                            }
                        }
                );
         View view = null;
         try {
             view = inflater.inflate(R.layout.fragment_maps, container, false);
         }
         catch(Exception e){
             e.printStackTrace();
         }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
        btnAddPoint = view.findViewById(R.id.addPoint);
        btnCloseLoop = view.findViewById(R.id.closeLoop);
        btnAddPoint.setOnClickListener(v -> onAddPoint(map.getCameraPosition().target));
        btnCloseLoop.setOnClickListener(v-> closeLoop());
        aimImage = view.findViewById(R.id.aimImage);
        addLayout = view.findViewById(R.id.addLayout);
        resultLayout = view.findViewById(R.id.resultLayout);
        ansDistanceAddLayout = view.findViewById(R.id.ansDistanceAddLayout);
        ansDistance =view.findViewById(R.id.ansDistance);
        ansArea = view.findViewById(R.id.ansArea);
        backImage = view.findViewById(R.id.backImage);
        completeImage = view.findViewById(R.id.completeImage);
        areaLinearLayout = view.findViewById(R.id.areaLinearLayout);
        ansDistanceLayout = view.findViewById(R.id.ansDistanceLayout);
        converterLengthUnit = view.findViewById(R.id.converterLengthUnit);
        converterAreaUnit = view.findViewById(R.id.converterAreaUnit);
        backImage.setOnClickListener(v->{
            if(!list.isEmpty() && !isTaskComplete){
                backOneStep();
            }
        });
        completeImage.setOnClickListener(v-> {
            if(isTaskComplete){
                reset();
            }
        });
        areaUnitList.clear();
        areaUnitList.add("Square Metres");
        areaUnitList.add("Square Kilometers");
        areaUnitList.add("Hectares");
        areaUnitList.add("Square Nautical Miles");
        areaUnitList.add("Square Feet");
        areaUnitList.add("Square Yards");
        areaUnitList.add("Square Miles");
        areaUnitList.add("Acres");

        ansAreaUnitList.clear();
        ansAreaUnitList.add(new TwoString(1,getResources().getString(R.string.ms)));
        ansAreaUnitList.add(new TwoString(0.000001,"%1$s km<sup><small><small>2</small></small><sup>"));
        ansAreaUnitList.add(new TwoString(0.0001,"%1$s ha"));
        ansAreaUnitList.add(new TwoString(0.00000029155335,"%1$s NA<sup><small><small>2</small></small><sup>"));
        ansAreaUnitList.add(new TwoString(10.7639104,"%1$s ft<sup><small><small>2</small></small><sup>"));
        ansAreaUnitList.add(new TwoString(1.19599005,"%1$s yd<sup><small><small>2</small></small><sup>"));
        ansAreaUnitList.add(new TwoString(0.000000386102159,"%1$s mi<sup><small><small>2</small></small><sup>"));
        ansAreaUnitList.add(new TwoString(0.000247105407,"%1$s ac"));

        lengthUnitList.clear();
        lengthUnitList.add("Centimetres");
        lengthUnitList.add("Metres");
        lengthUnitList.add("Kilometres");
        lengthUnitList.add("Nautical Miles");
        lengthUnitList.add("Inches");
        lengthUnitList.add("Feet");
        lengthUnitList.add("Yards");
        lengthUnitList.add("Miles");

        ansLengthUnitList.clear();
        ansLengthUnitList.add(new TwoString(100,"%1$s cm"));
        ansLengthUnitList.add(new TwoString(1,"%1$s m"));
        ansLengthUnitList.add(new TwoString(.001,"%1$s km"));
        ansLengthUnitList.add(new TwoString(.000539956803,"%1$s NM"));
        ansLengthUnitList.add(new TwoString(39.3700787,"%1$s in"));
        ansLengthUnitList.add(new TwoString(3.2808399,"%1$s ft"));
        ansLengthUnitList.add(new TwoString(1.0936133,"%1$s yd"));
        ansLengthUnitList.add(new TwoString(.000621371192,"%1$s mi"));

    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void closeLoop() {
        backImage.setBackground(getResources().getDrawable(R.drawable.circle_grey, Objects.requireNonNull(getActivity()).getTheme()));
        completeImage.setBackground(getResources().getDrawable(R.drawable.circle_blue, getActivity().getTheme()));
        isTaskComplete=true;
        map.setOnCameraMoveListener(null);
        white_Polyline.remove();
        list.add(list.get(0));
        polyline.remove();
        aimImage.setVisibility(View.INVISIBLE);
        addLayout.setVisibility((View.INVISIBLE));
        resultLayout.setVisibility(View.VISIBLE);
        polygon =  map.addPolygon(new PolygonOptions()
                .addAll(list)
                .fillColor(POLYGON_COLOR_YELLOW_LIGHT)
                .strokeColor(POLYLINE_COLOR_YELLOW)
                .strokeWidth(POLYGON_STROKE_WIDTH_PX));
        removeAllMarker();
//        calculate distance and area and put at result;
        if(distancesSum>=1000){
            ansDistance.setText(String.format("%.2f",(distancesSum/1000))+" Km");
        }
        else {
            ansDistance.setText((int)distancesSum + " m");
        }
//        calculate ans area and put
        areaInMeterSquare = calculateArea(list);
        @SuppressLint("StringFormatMatches")
        String ms2 = String.format(getResources().getString(R.string.ms), (int)areaInMeterSquare);
        ansArea.setText(Html.fromHtml(ms2,Html.FROM_HTML_MODE_COMPACT));

        areaLinearLayout.setOnClickListener(v -> {
                if(converterAreaUnit.getVisibility()==View.VISIBLE) {
                    converterAreaUnit.setVisibility(View.GONE);
                }
                else{
                    converterAreaUnit.setVisibility(View.VISIBLE);
                    converterLengthUnit.setVisibility(View.GONE);
                }
            }
        );
        ansDistanceLayout.setOnClickListener(v -> {
            if(converterLengthUnit.getVisibility()==View.VISIBLE) {
                converterLengthUnit.setVisibility(View.GONE);
            }
            else{
                converterLengthUnit.setVisibility(View.VISIBLE);
                converterAreaUnit.setVisibility(View.GONE);
            }
                }
        );
        ArrayAdapter<String> areaUnitConvertorAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, areaUnitList);
        ArrayAdapter<String> lengthUnitConvertorAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1, lengthUnitList);

        converterAreaUnit.setAdapter(areaUnitConvertorAdapter);
        converterLengthUnit.setAdapter(lengthUnitConvertorAdapter);
        converterAreaUnit.setOnItemClickListener((parent, view, position, id) -> {
//            ansDistance.setText(String.format("%.2f",(distancesSum/1000))+" Km");
            TwoString temp = ansAreaUnitList.get(position);
            double ans = (areaInMeterSquare*temp.multiple);
            String ms212;
            if(position == 1 || position ==3 || position== 6 ){
                ms212 = String.format(temp.format, String.format("%.4f",ans));
            }
            else{
                ms212 = String.format(temp.format, (int)ans);
            }
            ansArea.setText(Html.fromHtml(ms212,Html.FROM_HTML_MODE_COMPACT));
            converterAreaUnit.setVisibility(View.GONE);
        });

        converterLengthUnit.setOnItemClickListener((parent, view, position, id) -> {
            TwoString temp = ansLengthUnitList.get(position);
            double ans = (distancesSum*temp.multiple);
            String ms212;
            if(position == 2 || position == 3 || position== 7 ){
                ms212 = String.format(temp.format, String.format("%.4f",ans));
            }
            else{
                ms212 = String.format(temp.format, (int)ans);
            }
            ansDistance.setText(Html.fromHtml(ms212,Html.FROM_HTML_MODE_COMPACT));
            converterLengthUnit.setVisibility(View.GONE);
        });
    }

    @SuppressLint("SetTextI18n")
    private void reset() {
        converterLengthUnit.setVisibility(View.GONE);
        converterAreaUnit.setVisibility(View.GONE);
        completeImage.setBackground(getResources().getDrawable(R.drawable.circle_grey, Objects.requireNonNull(getActivity()).getTheme()));
        list.clear();
        markerArray.clear();
        if(polyline!= null)
            polyline.remove();
        if(white_Polyline!=null)
            white_Polyline.remove();
        distancesSum=0;
        isTaskComplete=false;
        if(polygon!=null)
            polygon.remove();
        btnAddPoint.setVisibility(View.VISIBLE);
        aimImage.setVisibility(View.VISIBLE);
        addLayout.setVisibility(View.VISIBLE);
        btnCloseLoop.setVisibility(View.INVISIBLE);
        resultLayout.setVisibility(View.INVISIBLE);

        ansDistanceAddLayout.setText("0m");
        map.setOnCameraMoveListener(this::onCameraMove);

    }

    private static int calculateArea(ArrayList<LatLng> list) {
        double area = 0;
        if (list.size() > 2)
        {
            for (int i = 0; i < list.size() - 1; i++)
            {
                LatLng p1 = list.get(i);
                LatLng p2 = list.get(i+1);
                area += convertToRadian(p2.longitude - p1.longitude) * (2 + Math.sin(convertToRadian(p1.latitude)) + Math.sin(convertToRadian(p2.latitude)));
            }

            area = area * 6378137 * 6378137 / 2;
        }
        return (int)Math.abs(area);
    }

    static class TwoString{
        double multiple;
        String format;
        TwoString(double a, String b){
            multiple=a;
            format=b;
        }
    }

    private  void removeAllMarker() {
        for (Marker i: markerArray) {
            i.remove();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.moveCamera(CameraUpdateFactory
                .newLatLngZoom(defaultLocation, INDIA_ZOOM));
        aimImage.setVisibility(View.VISIBLE);
        Objects.requireNonNull(getView()).findViewById(R.id.addLayout).setVisibility(View.VISIBLE);
        if (lastKnownLocation == null) {
            updateLocationUI();
        } else {
            if(locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(false);
            }
            map.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        }
        map.setOnCameraMoveListener(this::onCameraMove);
        reset();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void onAddPoint(@NonNull LatLng latLng) {
        backImage.setBackground(getResources().getDrawable(R.drawable.circle_blue, Objects.requireNonNull(getActivity()).getTheme()));
        if(polyline !=null ) polyline.remove();
        if(!list.isEmpty()){
            distancesSum += calculateLocationDifference(list.get(list.size()-1),latLng);
        }
        list.add(latLng);
        polyline = map.addPolyline(new PolylineOptions()
                .addAll(list)
                .color(POLYLINE_COLOR_YELLOW)
                .width(POLYLINE_WIDTH_PX)
        );

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.circle_without_background);

        markerArray.add(
        map.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(bitmapDescriptor)
                .anchor(0.5f, 0.5f)
        ));
        if(distancesSum>=1000){
            ansDistanceAddLayout.setText(String.format("%.2f", (distancesSum/1000))+" Km");
        }
        else {
            ansDistanceAddLayout.setText((int)distancesSum + " m");
        }

    }
    private static double calculateLocationDifference(LatLng p1, LatLng p2) {
        int R = 6378137; //// Earth’s mean radius in meter
        double dLat = convertToRadian(p2.latitude - p1.latitude);
        double dLong = convertToRadian(p2.longitude - p1.longitude);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(convertToRadian(p1.latitude)) * Math.cos(convertToRadian(p2.latitude))
                * Math.sin(dLong / 2) * Math.sin(dLong / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // return distance in meter
    }


    private void updateLocationUI() {
        System.out.println("ui called");
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                getDeviceLocationCheck();
            } else {
                map.setMyLocationEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            updateLocationUI();
        } else {
            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private void getDeviceLocationCheck() {
            System.out.println("get device location start");
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);


            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            Task<LocationSettingsResponse> result =
                    LocationServices.getSettingsClient(Objects.requireNonNull(getActivity())).checkLocationSettings(builder.build());

            result.addOnCompleteListener(task -> {
                try {
                    task.getResult(ApiException.class);
                    getDeviceLocation();

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                int LOCATION_SETTINGS_REQUEST=1;
                                resolvable.startResolutionForResult(getActivity(),LOCATION_SETTINGS_REQUEST);
                                getDeviceLocation();
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
    }
    protected void getDeviceLocation() {
        new Handler().postDelayed(() -> {
            try {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnSuccessListener(location -> {
                    lastKnownLocation = location;
                    if (lastKnownLocation != null)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "Current location is null. Using defaults.");
                    Log.e(TAG, "Exception: %s" + e.getMessage());
                    map.setMyLocationEnabled(false);
                });
            } catch (SecurityException e)  {
                Log.e("Exception: %s", e.getMessage(), e);
            }
        },3000);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        super.onSaveInstanceState(outState);
    }

    public void onCameraMove() {
        Polyline temp = null;
        if (white_Polyline != null) {
            temp = white_Polyline;
        }
        if (!list.isEmpty()) {
            white_Polyline = map.addPolyline(new PolylineOptions()
                    .add(map.getCameraPosition().target)
                    .add(list.get(list.size() - 1))
                    .color(0xffffffff)
                    .width(4)
            );
        }
        if (temp != null) temp.remove();

        if (!list.isEmpty()) {
            double currentDistance = calculateLocationDifference(map.getCameraPosition().target, list.get(0));
            double pixelInMeter = (156543.03392 * Math.pow(0.5, map.getCameraPosition().zoom - 1));
            if (list.size() > 2 && currentDistance / pixelInMeter <= 10) {

                btnAddPoint.setVisibility(View.INVISIBLE);
                btnCloseLoop.setVisibility(View.VISIBLE);

            } else {
                btnAddPoint.setVisibility(View.VISIBLE);
                btnCloseLoop.setVisibility(View.INVISIBLE);
            }
        }
    }
}
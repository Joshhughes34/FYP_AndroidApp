package com.example.joshuahughes.fypapp.fragments;


import android.content.Context;


import android.location.Location;

import android.os.Bundle;
import android.support.v4.app.Fragment;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joshuahughes.fypapp.R;

import com.example.joshuahughes.fypapp.models.CrimeLocationModel;
import com.example.joshuahughes.fypapp.models.CrimeLocationsRequestModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapInputFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapInputFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapInputFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, SeekBar.OnSeekBarChangeListener{


    private ArrayList<Marker> resultsMarkers;
    private CrimeLocationsRequestModel requestModel;
    private LatLng selectedLocation;
    private Integer selectedRadius = 400; //400 is default value;
    private TextView radiusValueCounter;

    private OnFragmentInteractionListener mListener;
    //public ProgressDialog progressDialog;

    // MAP INPUT FRAGMENT ------------------------------------------------------------------------//

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onMapInputInteraction(LatLng position, Integer radius);
        void onMapLoadSavedInstance();
        Boolean isConnectedToInternet();
    }

    public MapInputFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapInputFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapInputFragment newInstance() {
        MapInputFragment fragment = new MapInputFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resultsMarkers = new ArrayList<Marker>();

        // Restoring the markers on configuration changes
        if(savedInstanceState!=null){
            selectedLocation = savedInstanceState.getParcelable("selectedLocation");
            selectedRadius = savedInstanceState.getInt("selectedRadius");
        }
        else{
            selectedRadius = 400;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_map_input, container, false);

        //Init radiusValueCounter TextView
        radiusValueCounter = (TextView) v.findViewById(R.id.radiusValueCounter);
        radiusValueCounter.setText(Integer.toString(selectedRadius) + 'm');

        //Init radiusSeekBar
        radiusSeekBar = (SeekBar) v.findViewById(R.id.radiusSeekBar);
        radiusSeekBar.setMax((radiusMax - radiusMin) / radiusStep);
        radiusSeekBar.setProgress(selectedRadius);
        radiusSeekBar.setOnSeekBarChangeListener(this);

        //Init map view
        mMapView = (MapView) v.findViewById(R.id.googleMap);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        try{
            MapsInitializer.initialize(getContext());
        } catch (Exception e){
            Log.d("MAP_INIT_ERROR",e.toString());
        }

        return v;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("selectedLocation", selectedLocation);
        outState.putInt("selectedRadius", selectedRadius);

    }


    // MAP VIEW implements -----------------------------------------------------------------------//

    private MapView mMapView;
    private Marker locationMarker;
    private Circle locationRadius;
    private GoogleMap mMap;


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        //set click event listener
        mMap.setOnMapClickListener(this);


        if(selectedLocation != null && selectedRadius != null){
            SetNewLocationMarker(selectedLocation, selectedRadius);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, GetZoomLevel(selectedRadius)));
            mListener.onMapLoadSavedInstance();

        }
        else{
            //default camera view
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(52.769261, -1.2272118), 14.0f));
        }

    }

    @Override
    public void onMapClick(LatLng point){
        selectedLocation = point;
        RemoveCurrentResultsMarkers();
        SetNewLocationMarker(selectedLocation,selectedRadius);
        NewSearchRequest();
    }

    @Override
    public void onResume(){
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause(){
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy(){
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory(){
        mMapView.onLowMemory();
        super.onLowMemory();
    }

    // SEEK BAR implements -----------------------------------------------------------------------//

    private SeekBar radiusSeekBar;
    //private Integer radiusValue = 400;
    final private Integer radiusMin = 50;
    final private Integer radiusMax = 5000;
    final private Integer radiusStep = 1;


    @Override
    public void onStopTrackingTouch(SeekBar seekBar){
        UpdateLocationRadiusSize(locationMarker, selectedRadius, false);
        RemoveCurrentResultsMarkers();
        if(locationMarker != null) {
            NewSearchRequest();
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar){}

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){

        selectedRadius = radiusMin + (progress * radiusStep);
        radiusValueCounter.setText(Integer.toString(selectedRadius) + 'm');
        UpdateLocationRadiusSize(locationMarker, selectedRadius, true);

    }

    // MAP DRAWING & MANIPULATION ----------------------------------------//

    private void SetNewLocationMarker(LatLng point, int circleRadius){

        if(locationMarker == null) {

            locationMarker = mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            );

            locationRadius = mMap.addCircle(new CircleOptions()
                    .center(locationMarker.getPosition())
                    .radius(circleRadius)
                    .fillColor(0x3033FFFF)
                    .strokeWidth(2)
            );
        }
        else{
            UpdateLocationMarkerPosition(point);
        }

    }


    private void UpdateLocationMarkerPosition(LatLng newPoint){
        locationMarker.setPosition(newPoint);
        locationRadius.setCenter(newPoint);
    }

    private void UpdateLocationRadiusSize(Marker locationMarker, int radius, Boolean inProgress){

        if(locationMarker != null){

            if(inProgress) {
                locationRadius.setFillColor(80000000);
            }else{
                locationRadius.setFillColor(0x4533FFFF);
            }

            locationRadius.setCenter(locationMarker.getPosition());
            locationRadius.setRadius(radius);
        }

    }


    private int GetZoomLevel(double r){

        double radius = r + r / 2;
        double scale = radius / 500;
        int zoomLevel = (int) (16 - Math.log(scale)/Math.log(2));

        return zoomLevel;
    }


    private void DrawResultsMarkers(CrimeLocationsRequestModel model){
        for(int j = 0; j<model.CrimeLocations.size(); j++){

            CrimeLocationModel clm = model.CrimeLocations.get(j);

            LatLng latLng = new LatLng(clm.Location.Latitude, clm.Location.Longitude);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(clm.Location.Name);
            markerOptions.snippet(clm.Crimes.size() + " crimes. Lat: " + clm.Location.Latitude + "  Lng: " + clm.Location.Longitude);

            float[] distance = new float[2];
            Location.distanceBetween(latLng.latitude, latLng.longitude, selectedLocation.latitude, selectedLocation.longitude, distance);

            if( distance[0] > selectedRadius ){
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            }

            Marker marker = mMap.addMarker(markerOptions);

            resultsMarkers.add(marker);
        }
    }

    private void RemoveCurrentResultsMarkers(){
        for(int i = 0; i< resultsMarkers.size(); i++){
            resultsMarkers.get(i).remove();
        }
        resultsMarkers.clear();
    }



    // Request / Response Methods -------------------------------------------------------//

    private void NewSearchRequest(){

        if(mListener.isConnectedToInternet()){
            mListener.onMapInputInteraction(selectedLocation, selectedRadius);
        }
        else{
            Toast toast = Toast.makeText(getActivity(), "Can't perform new search without internet connection", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    //Public to allow parent activity to call
    public void ProcessNewRequestResults(CrimeLocationsRequestModel model){

        if(model != null){
            RemoveCurrentResultsMarkers();
            DrawResultsMarkers(model);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationMarker.getPosition(), GetZoomLevel(locationRadius.getRadius())));
        }
        else{
            //TODO: some kind of error.
        }

    }

    //------------------------------------------------//
    //------------------------------------------------//
    //------------------------------------------------//
    //------------------------------------------------//



}

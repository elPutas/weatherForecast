package android.test.weatherforecast;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    private String TAG = "GIO";
    private Location currentBestLocation = null;

    private LocationListener locationListener;
    private LocationManager locationManager;

    private String longitude;
    private String latitude;

    private TextView cityTitle;
    private TextView temperatureNum;
    private TextView humidityNum;
    private TextView precipitationNum;

    private TextView teperatureDay1;
    private TextView teperatureDay2;
    private TextView teperatureDay3;
    private TextView teperatureDay4;
    private TextView teperatureDay5;

    private TextView status;

    RelativeLayout dayData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dayData = (RelativeLayout) findViewById(R.id.daysData);
        dayData.bringToFront();

        locationListener = new MyLocationListener();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        cityTitle = (TextView) findViewById(R.id.cityTitle);

        temperatureNum = (TextView) findViewById(R.id.temperatureNum);
        humidityNum = (TextView) findViewById(R.id.humidity);
        precipitationNum = (TextView) findViewById(R.id.precipProbability);

        teperatureDay1 = (TextView) findViewById(R.id.teperatureDay1);
        teperatureDay2 = (TextView) findViewById(R.id.teperatureDay2);
        teperatureDay3 = (TextView) findViewById(R.id.teperatureDay3);
        teperatureDay4 = (TextView) findViewById(R.id.teperatureDay4);
        teperatureDay5 = (TextView) findViewById(R.id.teperatureDay5);

        status = (TextView) findViewById(R.id.status);



        allPermisisionsAccepted();

    }

    public void allPermisisionsAccepted()
    {
        //check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 500, 10, locationListener);
        Location location;
        if (locationManager != null) {
            location = locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {


                /*------- To get city name from coordinates -------- */
                String cityName = null;
                Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());

                List<Address> addresses;
                try {
                    addresses = gcd.getFromLocation(location.getLatitude(),
                            location.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        System.out.println(addresses.get(0).getLocality());
                        cityName = addresses.get(0).getLocality();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                String s = location.getLongitude() + "\n" + location.getLatitude() + "\n\nMy Current City is: "
                        + cityName;
                cityTitle.setText(String.valueOf( cityName));
                //Log.d(TAG, s);

                String finalURL = "https://api.darksky.net/forecast/6f2cf8ce1a9faf0099c0d91e4a7e22fe/"+location.getLatitude()+","+location.getLongitude();
                //Log.d(TAG, finalURL);
                new JsonTask().execute(finalURL);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // permission acepted.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Log.d(TAG, "acept");
                    allPermisisionsAccepted();


                } else {
                    //Log.d(TAG, "denied");
                    // permission denied
                }
                return;
            }
        }
    }


    /*---------- Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener
    {


        @Override
        public void onLocationChanged(Location loc) {


            longitude = "Longitude: " + loc.getLongitude();
            latitude = "Latitude: " + loc.getLatitude();


        /*------- To get city name from coordinates -------- */
            String cityName = null;
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());

            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
                if (addresses.size() > 0) {
                    System.out.println(addresses.get(0).getLocality());
                    cityName = addresses.get(0).getLocality();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }




        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider){}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    private class JsonTask extends AsyncTask<String, String, String>
    {

        ProgressDialog pd;

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    //Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }

            if(result != null)
            {

                JSONObject json = null;
                try {
                    json = new JSONObject(result);
                    JSONObject jsonThisDay = json.getJSONObject("currently");
                    temperatureNum.setText(String.valueOf(jsonThisDay.get("temperature"))+"°");
                    status.setText(String.valueOf(jsonThisDay.get("summary")));
                    humidityNum.setText(String.valueOf(jsonThisDay.get("humidity")));
                    precipitationNum.setText(String.valueOf(jsonThisDay.get("precipProbability")));

                    //teperatureDay1.setText("dsfhdjgfj");


                    JSONObject jsonDays = json.getJSONObject("daily");
                    JSONArray jsonDaysData = jsonDays.getJSONArray("data");

                    teperatureDay1.setText(String.valueOf(jsonDaysData.getJSONObject(0).get("temperatureMax"))+"°");
                    teperatureDay2.setText(String.valueOf(jsonDaysData.getJSONObject(1).get("temperatureMax"))+"°");
                    teperatureDay3.setText(String.valueOf(jsonDaysData.getJSONObject(2).get("temperatureMax"))+"°");
                    teperatureDay4.setText(String.valueOf(jsonDaysData.getJSONObject(3).get("temperatureMax"))+"°");
                    teperatureDay5.setText(String.valueOf(jsonDaysData.getJSONObject(4).get("temperatureMax"))+"°");

                    //Log.d(TAG, String.valueOf(jsonDaysData));

                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }
        }
    }
}

package edu.mit.printAtMIT.view.listPrinter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import edu.mit.printAtMIT.R;
import edu.mit.printAtMIT.controller.client.PrinterClient;
import edu.mit.printAtMIT.controller.client.PrinterClientException;
import edu.mit.printAtMIT.model.printer.Printer;
import edu.mit.printAtMIT.model.printer.PrintersDbAdapter;
import edu.mit.printAtMIT.model.printer.StatusType;
import edu.mit.printAtMIT.view.main.SettingsActivity;
import edu.mit.printAtMIT.view.print.PrintMenuActivity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Shows printer info; name, location, status, picture, relevant stuff
 * 
 * Menu: Refresh View on Map Home Settings About
 */
public class PrinterInfoActivity extends MapActivity {
    public static final String TAG = "PrinterInfoActivity";
    public static final String REFRESH_ERROR = "Error getting data, please be sure you are connected to the MIT network";

    private static final int REFRESH_ID = Menu.FIRST;

    private PrintersDbAdapter mDbAdapter;
    private boolean favorite;
    private String id;

    public static final int MIT_CENTER_LAT = 42359425;
    public static final int MIT_CENTER_LONG = -71094735;

    MapView mapView;
    List<Overlay> mapOverlays;
    Drawable drawable;
    PrinterItemizedOverlay itemizedOverlay;
    FixedMyLocationOverlay myLocationOverlay;

    private int centerLat;
    private int centerLong;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("PrinterInfoActivity", "onCreate");
        setContentView(R.layout.printer_info);

        Bundle extras = getIntent().getExtras();
        id = extras.getString("id");

        Button settingsButton = (Button) findViewById(R.id.settings_icon);
        Button listButton = (Button) findViewById(R.id.list_icon);
        Button printButton = (Button) findViewById(R.id.printer_icon);

        printButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),
                        PrintMenuActivity.class);
                startActivity(intent);
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),
                        SettingsActivity.class);
                startActivity(intent);
            }
        });

        listButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),
                        MainMenuActivity.class);
                startActivity(intent);
            }
        });

        RefreshTask task = new RefreshTask();
        task.execute();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("PrinterInfoActivity", "onPause");
        mDbAdapter.close();
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("PrinterInfoActivity", "onResume");

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.printlist_menu, menu);
        return true;
    }



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selections
		switch (item.getItemId()) {
		case R.id.refresh:
			RefreshTask task = new RefreshTask();
            task.execute();
            return true;
		case R.id.about:
			showAboutDialog();
			super.onOptionsItemSelected(item);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showAboutDialog() {
		showDialog(0);
	}



    @Override
    protected Dialog onCreateDialog(int id) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.about_dialog);
        dialog.setTitle("About");
        TextView tv = (TextView) dialog.findViewById(R.id.about_text);
        Linkify.addLinks(tv, Linkify.ALL);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        return dialog;
    }

//    /**
//     * Refreshes printer data Sets Textview.
//     * 
//     * @throws PrinterClientException
//     * 
//     * @throws ParseException
//     */
//    private/* String */Printer refresh() throws PrinterClientException {
//
//        // Parse makes call to cloud to retrieve printer information
//        Log.i(TAG, "refresh()");
//        // return this.displayInfo(printer);
//        Printer printer = PrinterClient.getPrinterObject(id);
//        return printer;
//    }

    private StatusType getStatus(int code) {
        switch (code) {
        case 0:
            return StatusType.READY;
        case 1:
            return StatusType.BUSY;
        case 2:
            return StatusType.ERROR;
        default:
            Log.e(TAG, "shouldn't get here, yo");
            break;
        }
        return null;
    }

    private String getStatus(StatusType type) {
        switch (type) {
        case READY:
            return "Available";
        case BUSY:
            return "Busy";
        case ERROR:
            return "Error";
        default:
            return "Unknown";
        }
    }

    private void displayInfo(Printer printer) {
        if (printer != null) {
            TextView printerName = (TextView) this
                    .findViewById(R.id.list_item_printer_name);
            TextView printerLocation = (TextView) this
                    .findViewById(R.id.list_item_printer_location);
            TextView printerStatus = (TextView) this
                    .findViewById(R.id.list_item_printer_status);

            if (printer.getBuilding() != null
                    && printer.getBuilding().length() != 0) {
                TextView printer_common_loc = (TextView) this
                        .findViewById(R.id.list_item_printer_common_location);

                if (printer_common_loc != null)
                    printer_common_loc.setText(printer.getBuilding());
            }

            if (printerName != null)
                printerName.setText(printer.getName());
            if (printerLocation != null)
                printerLocation.setText(printer.getLocation());
            if (printerStatus != null) {
                StatusType statusType = getStatus(printer.getStatus());
                String status = getStatus(statusType);
                printerStatus.setText(status);
                ImageView circle = (ImageView) this
                        .findViewById(R.id.status_dot);

                switch (statusType) {
                case READY:
                    circle.setImageResource(R.drawable.green_dot);
                    break;
                case BUSY:
                    circle.setImageResource(R.drawable.yellow_dot);
                    break;
                case ERROR:
                    circle.setImageResource(R.drawable.red_dot);
                    break;
                default:
                    circle.setImageResource(R.drawable.grey_dot);
                    break;
                }
            }

            final ImageView favButton = (ImageView) this
                    .findViewById(R.id.favorite_button);

            // set favorite state of printer
            mDbAdapter = new PrintersDbAdapter(this);
            mDbAdapter.open();
            final String id = printer.getName();
            favorite = mDbAdapter.isFavorite(id);

            if (favorite) {
                favButton.setImageResource(R.drawable.favorite_btn_pressed);
            } else {
                favButton.setImageResource(R.drawable.favorite_btn);
            }
            mDbAdapter.close();

            favButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    Log.i("MainMenuActivity", "clicking favorite button");
                    mDbAdapter.open();
                    if (favorite) {
                        mDbAdapter.removeFavorite(id);
                        favButton.setImageResource(R.drawable.favorite_btn);
                    } else {
                        mDbAdapter.addToFavorites(id);
                        favButton
                                .setImageResource(R.drawable.favorite_btn_pressed);
                    }
                    favorite = !favorite;
                    mDbAdapter.close();
                }
            });

            /**
             * map
             */
            mapView = (MapView) findViewById(R.id.mapview);
            mapView.setBuiltInZoomControls(true);
            mapOverlays = mapView.getOverlays();
            drawable = this.getResources()
                    .getDrawable(R.drawable.map_green_pin);

            myLocationOverlay = new FixedMyLocationOverlay(this, mapView);
            mapOverlays.add(myLocationOverlay);
            mapView.postInvalidate();

            itemizedOverlay = new PrinterItemizedOverlay(drawable, this,
                    mapView);

            MapController controller = mapView.getController();

            // make mapview start at MIT if allView, else animate to selected
            // printer loc

            centerLat = printer.getLatitude();
            centerLong = printer.getLongitude();

            controller.setZoom(17);
            controller.animateTo(new GeoPoint(centerLat, centerLong));

            // add printer overlayitems to map
            GeoPoint point = new GeoPoint(printer.getLatitude(),
                    printer.getLongitude());
            StatusType statusType = getStatus(printer.getStatus());
            PrinterOverlayItem item = new PrinterOverlayItem(point,
                    printer.getName() + " (" + printer.getLocation() + ")",
                    "Status: " + getStatus(statusType), printer.getName());

            if (statusType.equals(StatusType.BUSY)) {
                drawable = this.getResources().getDrawable(
                        R.drawable.map_yellow_pin);
            } else if (statusType.equals(StatusType.ERROR)) {
                drawable = this.getResources().getDrawable(
                        R.drawable.map_red_pin);
            } else {
                drawable = this.getResources().getDrawable(
                        R.drawable.map_green_pin);
            }
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            item.setMarker(drawable);
            itemizedOverlay.addOverlay(item);

            mapOverlays.add(itemizedOverlay);
        }
    }

    public class RefreshTask extends AsyncTask<Void, byte[], Printer> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "RefreshTask onPreExecute");
            dialog = ProgressDialog.show(PrinterInfoActivity.this, "",
                    "Refreshing Printer Data", true);
        }

        @Override
        protected Printer doInBackground(Void... params) { 
            Printer printer = null;
            if (isConnected()) {
                try {

                    printer = PrinterClient.getPrinterObject(id);
                    // result = refresh();
                } catch (PrinterClientException e) {
                    // e.printStackTrace();
                    Log.e(TAG, "RefreshTask Parse NUBFAIL");
                    // result = PrinterInfoActivity.REFRESH_ERROR;
                }
            } 
            return printer;
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "RefreshTask Cancelled.");
        }

        @Override
        protected void onPostExecute(Printer printer) {

            if (printer == null) {
                Toast.makeText(getApplicationContext(),
                        "Error getting data, please try again later",
                        Toast.LENGTH_SHORT).show();
                Log.i(TAG,
                        "RefreshTask onPostExecute: Completed with an Error.");
            }
            displayInfo(printer);
            // TextView tv = (TextView) findViewById(R.id.printer_info_text);
            // tv.setText(result);
            dialog.dismiss();

        }
    }

    /**
     * Checks to see if user is connected to wifi or 3g
     * 
     * @return
     */
    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) PrinterInfoActivity.this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {

            networkInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (!networkInfo.isAvailable()) {
                networkInfo = connectivityManager
                        .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            }
        }
        return networkInfo == null ? false : networkInfo.isConnected();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

}

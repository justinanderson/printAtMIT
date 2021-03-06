package edu.mit.printAtMIT.view.listPrinter;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import edu.mit.printAtMIT.R;
import edu.mit.printAtMIT.view.main.SettingsActivity;
import edu.mit.printAtMIT.view.print.PrintMenuActivity;

/**
 * Show "print" and "view printers" buttons.
 * Menu buttons:
 *      Setting
 *      About
 */
public class MainMenuActivity extends TabActivity{
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        Log.i("PrinterListActivity", "Calling onCreate()");

        setContentView(R.layout.home_screen);
    	
        TabHost tabHost = getTabHost();  // The activity TabHost
        
        addTab(tabHost, PrinterListActivity.ALL_PRINTERS, R.drawable.all_tab);
        addTab(tabHost, PrinterListActivity.CAMPUS_PRINTERS, R.drawable.campus_tab);
        addTab(tabHost, PrinterListActivity.DORM_PRINTERS, R.drawable.dorm_tab);
        tabHost.setCurrentTab(0);
        
    	Button settingsButton = (Button) findViewById(R.id.settings_icon);
    	Button printButton = (Button) findViewById(R.id.printer_icon);
    	Button listButton = (Button) findViewById(R.id.list_icon);
    	
    	settingsButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),
						SettingsActivity.class);
				startActivity(intent);
			}
		});
    	
    	printButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),
						PrintMenuActivity.class);
				startActivity(intent);
			}
		});
    	
    	listButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				TabHost tabHost = getTabHost();
				tabHost.setCurrentTab(0);
			}
		});
    }
    
    /**
     * Sets the tab view
     * @param tabHost
     * @param label
     * @param drawableId
     */
    private void addTab(TabHost tabHost, int type, int drawableId) {
    	Intent intent;
    	switch (type) {
    	case PrinterListActivity.ALL_PRINTERS: intent = new Intent(this, PrinterListActivity.class); break;
    	case PrinterListActivity.CAMPUS_PRINTERS: intent = new Intent(this, PrinterListCampusActivity.class); break;
    	case PrinterListActivity.DORM_PRINTERS: intent = new Intent(this, PrinterListDormActivity.class); break;
    	default: intent = new Intent(this, PrinterListActivity.class); break;
    	}

    	TabHost.TabSpec spec = tabHost.newTabSpec(""+type);

    	View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);

    	ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
    	icon.setImageResource(drawableId);

    	spec.setIndicator(tabIndicator);
    	spec.setContent(intent);

    	tabHost.addTab(spec);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        // changing the tab icons based on orientation
        TabHost tabhost = getTabHost();
        TabWidget tabWidget = tabhost.getTabWidget();
        View all_printers = tabWidget.getChildAt(0);
        
        int all_tab;
        int campus_tab;
        int dorm_tab;
        
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	all_tab = R.drawable.all_tab_landscape;
        	campus_tab = R.drawable.campus_tab_landscape;
        	dorm_tab = R.drawable.dorm_tab_landscape;
        }
        else {
        	all_tab = R.drawable.all_tab;
        	campus_tab = R.drawable.campus_tab;
        	dorm_tab = R.drawable.dorm_tab;
        }
        
        ImageView all_icon = (ImageView) all_printers.findViewById(R.id.icon);
        all_icon.setImageResource(all_tab);

        
        View campus_printers = tabWidget.getChildAt(1);
        ImageView campus_icon = (ImageView) campus_printers.findViewById(R.id.icon);
        campus_icon.setImageResource(campus_tab);
        
        View dorm_printers = tabWidget.getChildAt(2);
        ImageView dorm_icon = (ImageView) dorm_printers.findViewById(R.id.icon);
        dorm_icon.setImageResource(dorm_tab);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.printlist_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.about:
	    	Dialog dialog = new Dialog(this);
	    	dialog.setContentView(R.layout.about_dialog);
	    	dialog.setTitle("About");
	    	dialog.show();
	    		
	    	TextView tv = (TextView) dialog.findViewById(R.id.about_text);
	    	Linkify.addLinks(tv, Linkify.ALL);
	    	tv.setMovementMethod(LinkMovementMethod.getInstance());
	    	
            super.onOptionsItemSelected(item);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

}

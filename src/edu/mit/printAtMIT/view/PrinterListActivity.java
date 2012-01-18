package edu.mit.printAtMIT.view;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import edu.mit.printAtMIT.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Lists all the printers from database. Shows name, location, status from each
 * printer List of favorite printers on top, then list of all printers
 * 
 * Menu Item: Settings About Home Refresh
 */

public class PrinterListActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.printer_list);

        Button button01 = (Button) findViewById(R.id.button01);
        Button button02 = (Button) findViewById(R.id.button02);

        Log.e("printerlist", "initializing parse");
        Parse.initialize(this, "KIb9mNtPKDtkDk7FJ9W6b7MiAr925a10vNuCPRer",
                "dSFuQYQXSvslh9UdznzzS9Vb0kDgcKnfzgglLUHT");
        Log.e("printerlist", "initialized parse");
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();
        Log.e("printerlist", "creating ParseQuery PrintersData");

        ParseQuery query = new ParseQuery("PrintersData");
        Log.e("printerlist", "created ParseQuery PrintersData");

        ParseObject printer;
        String test = "oops";
        try {
            Log.e("printerList", "executing query.get");
            printer = query.get("bcBJyfM3s1");
            Log.i("parsequery", printer.getObjectId());
            test = printer.getString("printerName");

        }
        catch (ParseException e) {
            Log.e("oncreate", "parse exception", e);
        }
        
        Log.i("onCreate", test);
        
        
        button01.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MapActivity.class);
                startActivity(intent);
            }
        });
        button02.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),
                        PrinterInfoActivity.class);
                startActivity(intent);
            }
        });
    }

}

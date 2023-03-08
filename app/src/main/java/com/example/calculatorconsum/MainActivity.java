package com.example.calculatorconsum;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.calculatorconsum.Adapters.ExpandableListAdapter;
import com.example.calculatorconsum.WebScraping.GetHTMLTask;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements GetHTMLTask.Callback {

    int sitesLoadedCounter = 0;

    ExpandableListAdapter listAdapter;
    ExpandableListView expandableListView;

    DrawerLayout drawerLayout;

    EditText distantaEditText;
    EditText consumEditText;
    EditText pretEditText;

    TextView litriiTextView;
    TextView costTextView;

    List<String> globalSupplierName = new ArrayList<>();
    List<String> globalFuelName = new ArrayList<>();
    List<String> globalFuelPrice = new ArrayList<>();

    //Lists for ExpandableList
    List<String> supplierNameHeader = new ArrayList<>();
    HashMap<String, List<String>> benzinaFuelListChild = new HashMap<>();
    HashMap<String, List<String>> fuelLisChild = new HashMap<>();
    //FULL fuelList to get fuelName + fuelPrice to show (sa dea domnu)
    List<String> fullFuelList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //No night mode access
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //Vertical orientation mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Init drawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        //Init EditText
        distantaEditText = findViewById(R.id.distantaEditText);
        consumEditText = findViewById(R.id.consumEditText);
        pretEditText = findViewById(R.id.pretEditText);

        //Init TextView
        litriiTextView = findViewById(R.id.litriiTextView);
        costTextView = findViewById(R.id.costTextView);

        //Init ImageView
        ImageView listImageView = findViewById(R.id.listImageView);

        //Verify if Internet is ON
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            //Loads HTML Content
            try {
                new GetHTMLTask(this).execute("https://www.plinul.ro/pret/benzina-standard/zalau-salaj");
                new GetHTMLTask(this).execute("https://www.plinul.ro/pret/motorina-standard/zalau-salaj");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
        //Opens drawerLayout when the listImageView is pressed
        listImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
    }
    public void calculareConsum(View view){

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.UP);

        String d;
        double distanta = 0;
        String c;
        double consum = 0;
        String p;
        double pretCombustibil = 0;

        try {
            d = distantaEditText.getText().toString(); // Same
            distanta = Double.parseDouble(d); // Make use of autoboxing.  It's also easier to read.

            c = consumEditText.getText().toString(); // Same
            consum = Double.parseDouble(c); // Make use of autoboxing.  It's also easier to read.

            p = pretEditText.getText().toString(); // Same
            pretCombustibil = Double.parseDouble(p); // Make use of autoboxing.  It's also easier to read.
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Va rog sa introduceti un numar valid.", Toast.LENGTH_SHORT).show();
        }

        double litriiConsumati;
        double costTraseu;

        litriiConsumati =( distanta * consum )/100;
        litriiTextView.setText("Ati consumat " + String.valueOf(df.format(litriiConsumati)) +" litrii.");
        litriiTextView.setVisibility(View.VISIBLE);

        costTraseu = litriiConsumati * pretCombustibil;
        costTextView.setText("Cost traseu: " + String.valueOf(df.format(costTraseu)) +" lei.");
        costTextView.setVisibility(View.VISIBLE);
    }//calculareConsum end

    public void setSupplierNameHeader(){
        for (String element : globalSupplierName) {
            if (!supplierNameHeader.contains(element)) {
                supplierNameHeader.add(element);
            }
        }
        Log.i("Suppliers: ", supplierNameHeader.toString());
    }

    public void getFirstSiteInfo(Document document){
        List<String> supplierName = new ArrayList<>();
        List<String> fuelName = new ArrayList<>();
        List<String> fuelPrice = new ArrayList<>();
        int cellCounter = 0;
        int supplierCounter = -1;
        int fuelNameCounter = -1;
        int fuelPriceCounter = -1;
        Element table = document.select("table.table.table-striped").first();
        Elements rows = table.select("tr");

        for (Element row : rows) {
            List<String> fuelInfoList = new ArrayList<>();
            Elements cells = row.select("td");
                for (Element cell : cells) {
                    String data = cell.text();
                    //Log.i("data: ",data);
                    switch (cellCounter) {
                        case 0:
                            supplierName.add(data);
                            if (supplierName.size()!=0) supplierCounter++;
                            globalSupplierName.add(data);
                            break;
                        case 2:
                            fuelName.add(data);
                            if (fuelName.size()!=0) fuelNameCounter++;
                            globalFuelName.add(data);
                            break;
                        case 3:
                            fuelPrice.add(data);
                            if (fuelPrice.size()!=0) fuelPriceCounter++;
                            globalFuelPrice.add(data);
                            break;
                    }
                    cellCounter++;

                    if (cellCounter == 5) {
                        cellCounter = 0;
                    }
                }
                if (supplierName.size()!=0) {
                    if (!benzinaFuelListChild.containsKey(supplierName.get(supplierCounter))) {
                        String fuelInfo = fuelName.get(fuelNameCounter) + " " + fuelPrice.get(fuelPriceCounter);
                        if (!fuelInfoList.contains(fuelInfo)) {
                            fuelInfoList.add(fuelInfo);
                            benzinaFuelListChild.put(supplierName.get(supplierCounter), fuelInfoList);
                        }
                    }
                }
        }
        Log.i("fuelList dupa prima iteratie: ", benzinaFuelListChild.toString());
    }//getFirstSiteInfo end

    public void getSecondSiteInfo(Document document){
        List<String> supplierName = new ArrayList<>();
        List<String> fuelName = new ArrayList<>();
        List<String> fuelPrice = new ArrayList<>();
        List<String> benzinaPret = new ArrayList<>();
        int cellCounter = 0;
        int supplierCounter = -1;
        int fuelNameCounter = -1;
        int fuelPriceCounter = -1;
        Element table = document.select("table.table.table-striped").first();
        Elements rows = table.select("tr");

        for (Element row : rows) {
            List<String> fuelInfoList = new ArrayList<>();
            Elements cells = row.select("td");
            for (Element cell : cells) {
                String data = cell.text();
                //Log.i("data: ",data);
                switch (cellCounter) {
                    case 0:
                        supplierName.add(data);
                        if (supplierName.size() != 0) supplierCounter++;
                        globalSupplierName.add(data);
                        break;
                    case 2:
                        fuelName.add(data);
                        if (fuelName.size() != 0) fuelNameCounter++;
                        globalFuelName.add(data);
                        break;
                    case 3:
                        fuelPrice.add(data);
                        if (fuelPrice.size() != 0) fuelPriceCounter++;
                        globalFuelPrice.add(data);
                        break;
                }
                cellCounter++;

                if (cellCounter == 5) {
                    cellCounter = 0;
                }
            }
            if (supplierName.size() != 0) {
                //ia benzina de dinainte (din getFirstSiteFuelInfo)
                benzinaPret.clear();
                benzinaPret = benzinaFuelListChild.get(supplierName.get(supplierCounter));
                if (benzinaPret.size() == 1) {
                    Log.i("benzina pret: ", benzinaPret.get(0));
                    //adauga motorina
                    String motorina = fuelName.get(fuelNameCounter) + " " + fuelPrice.get(fuelPriceCounter);
                    fuelInfoList.clear();
                    fuelInfoList.add(benzinaPret.get(0));
                    fuelInfoList.add(motorina);
                    //luam supplier separat
                    String supplier = supplierName.get(supplierCounter);
                    Log.i("BENZINA + MOTORINA: ", fuelInfoList.toString());
                    //adauga la supplierName benzina + motorina
                    fuelLisChild.put(supplier, fuelInfoList);
                }
            }
        }
    }//getSecondSiteInfo end

    public void setFullFuelList(){
        for (int i =0; i < globalFuelName.size(); i++){
            String fuelInfo = globalFuelName.get(i) + " " + globalFuelPrice.get(i);
            if (!fullFuelList.contains(fuelInfo))
                fullFuelList.add(fuelInfo);
        }
    }

    @Override
    public void onHtmlLoaded(Document document) {
        if (sitesLoadedCounter == 0){
            getFirstSiteInfo(document);
        }else{
            getSecondSiteInfo(document);
            setSupplierNameHeader();
            setFullFuelList();
            Log.i("fuelListChild", fuelLisChild.toString());
            //ExpandableListView setup
            setUpExpandableListView();
        }
        sitesLoadedCounter++;

    }//onHtmlLoaded end

    private void setUpExpandableListView() {
        listAdapter = new ExpandableListAdapter((Context) this, supplierNameHeader, (HashMap<String, List<String>>) fuelLisChild);
        expandableListView = findViewById(R.id.expanded_list_view);
        expandableListView.setAdapter(listAdapter);
        expandableListView.setGroupIndicator(null);
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (parent.isGroupExpanded(groupPosition)) {
                    parent.collapseGroup(groupPosition);
                } else {
                    parent.expandGroup(groupPosition);

                    parent.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                        @Override
                        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                            String supplierName = supplierNameHeader.get(groupPosition);
                            List<String> benzinaAndMotorinaOfSuppleir = new ArrayList<>();
                            benzinaAndMotorinaOfSuppleir = fuelLisChild.get(supplierName);
                            String input = benzinaAndMotorinaOfSuppleir.get(childPosition);
                            String regex = "\\d+\\.\\d+"; // matches a decimal number
                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher(input);

                            if (matcher.find()) {
                                String match = matcher.group(); // "6.64"
                                double num = Double.parseDouble(match); // convert to double
                                Log.i("pret: ", String.valueOf(num));
                                pretEditText.setText(String.valueOf(num));
                            }
                            drawerLayout.closeDrawer(GravityCompat.END);
                            return true;
                        }
                    });
                }
                return true;
            }
        });

    }
}//MainActivity class end
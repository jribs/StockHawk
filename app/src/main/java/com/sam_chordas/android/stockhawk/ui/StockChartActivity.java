package com.sam_chordas.android.stockhawk.ui;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Joshua on 3/13/2016.
 */
public class StockChartActivity extends AppCompatActivity {

    private static String LOG_TAG = "StockChartActivity";

    LineChartView chart;
    TextView emptyView;
    Float high;
    Float low;
    String symbol;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_line_graph);
        //Set our chart and emptyView
        chart = (LineChartView) findViewById(R.id.linechart);
        chart.setVisibility(View.VISIBLE);
        emptyView = (TextView) findViewById(R.id.chart_emptyView);
        emptyView.setVisibility(View.GONE);

        high=0.00f;
        low=999999.00f;



        if(getIntent().getExtras().containsKey(QuoteColumns.SYMBOL)){
        //perform AsyncTask
            symbol=getIntent().getExtras().getString(QuoteColumns.SYMBOL);
            new StockChartAsyncTask().execute(symbol, null);
            getSupportActionBar().setTitle(symbol.toUpperCase() + getString(R.string.chart_titleSuffix));

        } else {

            chart.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }



    //AsyncTask to get a historical query of passed Symbol.
    //This may seem unnecessary (could build into Utils/TaskService,
    // but I wanted to understand the yahoo API's query structure
    //and how the JSON object was extracted.

    private class StockChartAsyncTask extends AsyncTask<String, Integer, LineSet> {
        @Override
        protected LineSet doInBackground(String... params) {
            //Just going to query for the passed symbol parameter
            StringBuilder urlStringBuilder = new StringBuilder();
            LineSet resultSet;
            if (params==null){
                return null; }
            if(params.length<1){
                return null;
            }

            try{
                //Base URL
                urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
                urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol = \"", "UTF-8"));
                urlStringBuilder.append(params[0]);
                //Add initial date argument, which will just be 3 months before today
                urlStringBuilder.append(URLEncoder.encode("\" and startDate = \"", "UTF-8"));

                //Get our start and end dates
                Calendar c = Calendar.getInstance();
                Date now = c.getTime();
                c.add(Calendar.MONTH, -3);
                Date threeMonthsAgo = c.getTime();

                //Append Date Parameters
                urlStringBuilder.append(getFormattedDate(threeMonthsAgo));
                urlStringBuilder.append(URLEncoder.encode("\" and endDate = \"", "UTF-8"));
                urlStringBuilder.append(getFormattedDate(now));
                urlStringBuilder.append("\"&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                        + "org%2Falltableswithkeys&callback=");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String getResponse;

            //Go and try to grab a response
            if(urlStringBuilder!=null) {
                try{
                    getResponse=fetchData(urlStringBuilder.toString());
                    return parseJSONtoPoints(getResponse);

                } catch (IOException e){
                    e.printStackTrace();
                }
            }


            return null;
        }



        protected void onPostExecute(LineSet result){
            if(result!=null){
                setUpChart(result);

            } else {
                //If it is null, show emptyview
                chart.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }

        }







    }

    private LineSet parseJSONtoPoints(String jsonResults){


        JSONArray resultsArray = null;
        LineSet set = new LineSet();

        int resultCount = 0;
        try{
            JSONObject jsonObject = new JSONObject(jsonResults);
            if (jsonObject != null && jsonObject.length() != 0){
                jsonObject = jsonObject.getJSONObject("query");

                //Grab a count, make sure its more than one and return a JSONArray
                resultCount = Integer.parseInt(jsonObject.getString("count"));
                Log.e(LOG_TAG, String.valueOf(resultCount));
                if(resultCount>=1){
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");
                }
            }

        }catch (JSONException e){
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }


        JSONObject point;
        Float close;
        //If we got results, convert them to Strings and Ints, then Points for Graph use
        if(resultCount>0) {
            for (int i = 0; i <= resultCount-1; i++) {
                try{
                   point = resultsArray.getJSONObject(i);
                    close=Float.valueOf(point.getString("Open"));
                    if(close>high){
                        high=close;
                    }

                    if(close<low){
                        low=close;
                    }
                            set.addPoint(point.getString("Date"), close);
                } catch(JSONException e){
                    Log.e(LOG_TAG, "Conversion of JSONARRAY to Object failed:" + e);
                }


            }

            return set;
        }

        return null;
    }

    private String getFormattedDate(Date date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }


    //Taken from Task Service
    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private void setUpChart(LineSet result){
        result.setColor(ContextCompat.getColor(this, R.color.material_green_700));


        chart.addData(result);
        chart.setAxisColor(ContextCompat.getColor(this, R.color.white));
        chart.setLabelsColor(ContextCompat.getColor(this, R.color.white));
        chart.setAxisBorderValues((int) Math.round(.8 * low), (int) Math.round(1.2 * high),
                (int) Math.round((1.2 * high) / (.8 * low)));
        chart.setXLabels(AxisController.LabelPosition.NONE);

        chart.show();
    }

}

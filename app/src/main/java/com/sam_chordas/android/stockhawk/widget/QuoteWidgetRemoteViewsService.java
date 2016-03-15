package com.sam_chordas.android.stockhawk.widget;

import android.app.IntentService;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by Joshua on 3/6/2016.
 */
public class QuoteWidgetRemoteViewsService extends RemoteViewsService{

    //Used to handle data for widget
    //Much of code based on Udacity Advanced Android App Repository at
    //https://github.com/udacity/Advanced_Android_Development/blob/8.00_Places_API_Start/app/src/main/java/com/example/android/sunshine/app/widget/DetailWidgetRemoteViewsService.java


//Columns we will need
private static String[] QUOTECOLUMNS = { QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP};

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RemoteViewsFactory() {
            //Data!
            private Cursor cU;

            @Override
            public void onCreate() {



            }

            @Override
            public void onDataSetChanged() {
                //Fill The Data, go go!

                //Avoid permissions error
                final long identityToken = Binder.clearCallingIdentity();

                cU = getContentResolver().query(
                        QuoteProvider.Quotes.CONTENT_URI,       //Uri
                        QUOTECOLUMNS,                           //Columns
                        QuoteColumns.ISCURRENT + " = ?",        //Selection
                        new String[]{"1"},                      //Selection Args
                        null                                    //Sort By
                        );


                //Restore calling identity
                Binder.restoreCallingIdentity(identityToken);

            }

            @Override
            public void onDestroy() {
                if(cU!=null){
                    cU.close();
                    cU=null;
                }
            }

            @Override
            public int getCount() {
                if(cU==null){
                    return 0;
                } else {
                    return cU.getCount();
                }
            }

            @Override
            public RemoteViews getViewAt(int position) {
                //Make sure we have a position to return
                if (position == AdapterView.INVALID_POSITION ||
                        cU == null || !cU.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_collection_item);
                //Extract data from cursor
                String change = cU.getString(cU.getColumnIndexOrThrow(QuoteColumns.PERCENT_CHANGE));
                String symbol = cU.getString(cU.getColumnIndexOrThrow(QuoteColumns.SYMBOL));


                //Bind data to text
                views.setTextViewText(R.id.stock_symbol, symbol);
                views.setTextViewText(R.id.change, change);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {


                return new RemoteViews(getPackageName(), R.layout.widget_collection_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if(cU.moveToPosition(position)){
                    return cU.getLong(cU.getColumnIndexOrThrow(QuoteColumns._ID));
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }

}

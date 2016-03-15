package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by Joshua on 3/6/2016.
 */
public class QuoteWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context ctx, AppWidgetManager manager, int[] widgetids) {


        for (int widgetid : widgetids) {
            //Determine listview layout
            RemoteViews remoteViews = new RemoteViews(ctx.getPackageName(), R.layout.widget_collection);

            //set up collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                remoteViews.setRemoteAdapter(R.id.widget_list, new Intent(ctx, QuoteWidgetRemoteViewsService.class));
            } else {
                remoteViews.setRemoteAdapter(0, R.id.widget_list, new Intent(ctx, QuoteWidgetRemoteViewsService.class));
            }

            manager.updateAppWidget(widgetid, remoteViews);
        }
    }

}





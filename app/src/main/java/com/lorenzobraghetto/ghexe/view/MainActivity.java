package com.lorenzobraghetto.ghexe.view;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.lorenzobraghetto.ghexe.R;
import com.lorenzobraghetto.ghexe.adapters.EventsAdapter;
import com.lorenzobraghetto.ghexe.controller.CurrentUser;
import com.lorenzobraghetto.ghexe.controller.GhexeRESTClient;
import com.lorenzobraghetto.ghexe.controller.HttpCallback;
import com.lorenzobraghetto.ghexe.model.Event;
import com.lorenzobraghetto.ghexe.model.Presence;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {

    List<Object> presences = new ArrayList<Object>();
    private EventsAdapter adapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView eventsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        eventsRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewEvents);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
        swipeRefresh.setOnRefreshListener(this);

        setSupportActionBar(toolbar);

        eventsRecyclerView.setHasFixedSize(true);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventsAdapter(this, presences);
        eventsRecyclerView.setAdapter(adapter);
        eventsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        progressBar.setVisibility(View.VISIBLE);
        CurrentUser user = CurrentUser.getInstance();
        if (user.getId() == -1)
            GhexeRESTClient.getInstance().getMe(this, user.getAccess_token(this), user.getRefresh_token(this), user.getExpires_in(this), new HttpCallback() {

                @Override
                public void onSuccess(List<Object> resultList) {
                    getEvent();
                }

                @Override
                public void onFailure() {

                }
            });
        else
            getEvent();
    }

    private void getEvent() {
        GhexeRESTClient.getInstance().getEvents(MainActivity.this, new HttpCallback() {
            @Override
            public void onSuccess(List<Object> resultList) {
                swipeRefresh.setRefreshing(false);
                List<Object> explodedList = explodeList(resultList);
                adapter.addAllFromZero(explodedList);
                progressBar.setVisibility(View.GONE);
                eventsRecyclerView.scrollToPosition(getPositionFromDay(explodedList));
            }

            @Override
            public void onFailure() {

            }
        });
    }

    private int getPositionFromDay(List<Object> explodedList) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (day == Calendar.SUNDAY) //Ghexe week start from Monday
            day = 7;
        else
            day--;
        for (int i = 0; i < explodedList.size(); i++) {
            Object event = explodedList.get(i);
            if (event instanceof Event) {
                if (((Event) event).getDayofweek() == day - 1) //Ghexe day of week start from 0
                    return i;
            }

        }
        return 0;
    }

    private List<Object> explodeList(List<Object> resultList) {
        List<Object> explodedList = new ArrayList<Object>();
        for (int i = 0; i < resultList.size(); i++) {
            Event event = (Event) resultList.get(i);
            explodedList.add(event);
            List<Presence> presences = event.getPresences();
            for (int z = 0; z < presences.size(); z++) {
                explodedList.add(presences.get(z));
            }
        }
        return explodedList;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_notification);
        item.setChecked(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("notification", true));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_notification:
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("notification", !item.isChecked()).commit();
                item.setChecked(!item.isChecked());
                break;
            case R.id.action_logout:
                PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
                CurrentUser.getInstance().logout();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        getEvent();
    }
}

package com.lorenzobraghetto.ghexe.model;

import java.util.List;

/**
 * Created by monossido on 14/12/14.
 */
public class Event {

    private final int dayofweek;
    private final String title;
    private final String time;
    private final List<Presence> presences;
    private final int id;

    public Event(int id, String title, String time, int dayofweek, List<Presence> presences) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.dayofweek = dayofweek;
        this.presences = presences;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public int getDayofweek() {
        return dayofweek;
    }

    public List<Presence> getPresences() {
        return presences;
    }

    public int getId() {
        return id;
    }
}

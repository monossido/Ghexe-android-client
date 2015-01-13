package com.lorenzobraghetto.ghexe.model;

/**
 * Created by monossido on 14/12/14.
 */
public class Presence {

    private boolean presence;
    private final int eventId;
    private final int id;
    private final User user;

    public Presence(int id, int eventId, User user, boolean presence) {
        this.id = id;
        this.eventId = eventId;
        this.presence = presence;
        this.user = user;
    }

    public boolean isPresence() {
        return presence;
    }

    public int getEventId() {
        return eventId;
    }

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setIsPresence(boolean presence) {
        this.presence = presence;
    }
}

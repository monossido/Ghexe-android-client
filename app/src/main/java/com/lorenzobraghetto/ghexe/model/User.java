package com.lorenzobraghetto.ghexe.model;

/**
 * Created by monossido on 14/12/14.
 */
public class User {

    private final String second_name;
    private final String first_name;
    private final int id;

    public User(int id, String first_name, String second_name) {
        this.id = id;
        this.first_name = first_name;
        this.second_name = second_name;
    }

    public String getSecond_name() {
        return second_name;
    }

    public String getFirst_name() {
        return first_name;
    }

    public int getId() {
        return id;
    }
}

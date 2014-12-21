package com.lorenzobraghetto.ghexe.controller;

import java.util.List;

/**
 * Created by monossido on 14/12/14.
 */
public interface HttpCallback {

    public void onSuccess(List<Object> resultList);

    public void onFailure();
}

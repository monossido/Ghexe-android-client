package com.lorenzobraghetto.ghexe.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.lorenzobraghetto.ghexe.R;
import com.lorenzobraghetto.ghexe.controller.CurrentUser;
import com.lorenzobraghetto.ghexe.controller.GhexeRESTClient;
import com.lorenzobraghetto.ghexe.controller.HttpCallback;

import java.util.List;

/**
 * Created by monossido on 14/12/14.
 */
public class LoginActivity extends ActionBarActivity {

    private Button signIn;
    private EditText nomeEdit;
    private EditText passwordEdit;
    private ProgressBar login_progress;
    private ScrollView login_form;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signIn = (Button) findViewById(R.id.email_sign_in_button);
        nomeEdit = (EditText) findViewById(R.id.email);
        passwordEdit = (EditText) findViewById(R.id.password);
        login_progress = (ProgressBar) findViewById(R.id.login_progress);
        login_form = (ScrollView) findViewById(R.id.login_form);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_progress.setVisibility(View.VISIBLE);
                login_form.setVisibility(View.GONE);
                GhexeRESTClient.getInstance().postAuthenticate(LoginActivity.this, nomeEdit.getText().toString(), passwordEdit.getText().toString(), callback);
            }
        });

        if (CurrentUser.getInstance().getAccess_token(this).length() > 0) {
            login_progress.setVisibility(View.VISIBLE);
            login_form.setVisibility(View.GONE);
            GhexeRESTClient.getInstance().getMe(this
                    , CurrentUser.getInstance().getAccess_token(this)
                    , CurrentUser.getInstance().getRefresh_token(this)
                    , CurrentUser.getInstance().getExpires_in(this)
                    , callback);
        }

    }

    private HttpCallback callback = new HttpCallback() {
        @Override
        public void onSuccess(List<Object> resultList) {
            finish();
            Intent main = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(main);
        }

        @Override
        public void onFailure() {
            login_progress.setVisibility(View.GONE);
            login_form.setVisibility(View.VISIBLE);
            Toast.makeText(LoginActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
        }
    };
}

package com.amsen.par.searchview.demo.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.amsen.par.searchview.AutoCompleteSearchView;
import com.amsen.par.searchview.demo.R;
import com.amsen.par.searchview.demo.api.MockApi;
import com.amsen.par.searchview.prediction.Prediction;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Pär Amsen 2016
 */
public class MainActivity extends AppCompatActivity {
    private MockApi api;
    private AutoCompleteSearchView searchView;
    private boolean runningFakeNetworkCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        api = new MockApi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        searchView = (AutoCompleteSearchView) searchViewItem.getActionView();

        searchView.setOnPredictionClickListener((position, prediction) -> {
            Toast.makeText(this, String.format("clicked [position:%d, value:%s, displayString:%s]", position, prediction.value, prediction.displayString), Toast.LENGTH_SHORT).show();
            searchViewItem.collapseActionView();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 0 && !runningFakeNetworkCall) {
                    searchView.showLoader();
                    fakeNetworkDelay(newText);
                }

                return true;
            }
        });
        return true;
    }

    private void fakeNetworkDelay(final String query) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runningFakeNetworkCall = true;

                runOnUiThread(() -> {
                    List<String> rawPredictions = api.getPredictions(query);
                    List<Prediction> predictions = toSearchViewPredictions(rawPredictions);
                    searchView.applyPredictions(predictions);
                    searchView.hideLoader();

                    runningFakeNetworkCall = false;
                });
            }
        }, 400 + (long) (Math.random() * 1100));
    }

    private List<com.amsen.par.searchview.prediction.Prediction> toSearchViewPredictions(List<String> predictions) {
        List<Prediction> forSearchView = new ArrayList<>();

        for (String prediction : predictions) {
            forSearchView.add(new Prediction(prediction, prediction)); //first param is for complex objects, second for display string.
        }

        return forSearchView;
    }
}

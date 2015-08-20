package ru.kluchikhin.testproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends Activity {

    private static final String URL_ADDRESS = "https://money.yandex.ru/api/categories-list";

    private TextView pathView;
    private ListView categoriesView;
    private Button synchronizeBtn;
    private Button backBtn;

    private List<Category> categories;
    private Stack<Integer> path;
    private Stack<String> strPath;

    private DBConnection dbConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        path = new Stack<>();
        strPath = new Stack<>();

        pathView = (TextView) findViewById(R.id.pathView);
        categoriesView = (ListView) findViewById(R.id.categoriesView);
        synchronizeBtn = (Button) findViewById(R.id.synchronizeBtn);
        backBtn = (Button) findViewById(R.id.backBtn);

        dbConnection = new DBConnection(this);
        dbConnection.open();

        categories = new ArrayList<>();
        dbConnection.getList(0, categories);
        if (categories.size() == 0) {
            List<Category> data = getDataFromService();
            if (data != null && data.size() != 0) {
                categories.clear();
                categories.addAll(data);
            }
            dbConnection.dumpList(categories);
        }

        dbConnection.getList(0, categories);
        path.push(0);
        final ArrayAdapter<Category> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
        categoriesView.setAdapter(adapter);

        categoriesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int id = categories.get(i).get_Id();
                strPath.push(categories.get(i).getTitle());
                pathView.setText(createPath());
                dbConnection.getList(id, categories);
                path.push(id);
                adapter.notifyDataSetChanged();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (path.size() == 1) return;
                path.pop();
                strPath.pop();
                pathView.setText(createPath());
                dbConnection.getList(path.peek(), categories);
                adapter.notifyDataSetChanged();
            }
        });

        synchronizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Category> data = getDataFromService();
                if (data != null && data.size() != 0) {
                    categories.clear();
                    categories.addAll(data);
                    dbConnection.clear();
                    dbConnection.dumpList(categories);
                }
                dbConnection.getList(0, categories);
                path.clear();
                path.push(0);
                strPath.clear();
                pathView.setText(createPath());
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbConnection.close();
    }

    //Saving data to List<Categories> categories
    private List<Category> getDataFromService() {
        String json = "";
        try {
            json = new GetDataTask().execute(URL_ADDRESS).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Problems with Internet connection")
                    .show();
        } catch (ExecutionException e) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Problems with Internet connection")
                    .show();
        } catch (TimeoutException e) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Problems with Internet connection")
                    .show();
        }
        if (json.equals("")) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("There is no data")
                    .show();
        } else {
            Gson gson = new Gson();
            Type categoryArrayType = new TypeToken<List<Category>>(){}.getType();
            List<Category> result = gson.fromJson(json, categoryArrayType);
            return result;
        }
        return null;
    }

    private String createPath() {
        StringBuilder sb = new StringBuilder();
        sb.append("/");
        for (String s: strPath) {
            sb.append(s);
            sb.append("/");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private class GetDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String inputLine = "";
            try {
                URL url = new URL(urls[0]);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                inputLine = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return inputLine;
        }
    }
}

/*
 * Copyright (c) 2019.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.terrynow.quview.activity;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.terrynow.quview.R;
import com.terrynow.quview.adapter.NoteListAdapter;
import com.terrynow.quview.model.NoteModel;
import com.terrynow.quview.model.NotebookModel;
import com.terrynow.quview.util.Constants;
import com.terrynow.quview.util.Utils;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Terry E-mail: yaoxinghuo at 126 dot com
 * @date 2019-10-15 14:52
 * @description
 */
public class NoteSearchActivity extends NoteBaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String TAG = "NoteSearchActivity";

    private NoteListAdapter noteListAdapter;
    private List<NoteModel> list = new ArrayList<>();
    private EditText keywordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_search);

        if (isFinishing()) {
            return;
        }

        ListView noteListView = findViewById(R.id.note_list);
        noteListAdapter = new NoteListAdapter(this, R.layout.list_note, list);
        noteListView.setAdapter(noteListAdapter);
        noteListView.setOnItemClickListener(this);

        findViewById(R.id.search).setOnClickListener(this);

        keywordText = findViewById(R.id.name);
        keywordText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(keywordText.getText().toString(), true);
                    return true;
                }
                return false;
            }
        });
        keywordText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (motionEvent.getX() > (keywordText.getWidth() - keywordText.getCompoundPaddingEnd())) {
                        keywordText.setText("");
                    }
                }
                return false;
            }
        });

        String keywords = getSearchStr(getIntent());
        keywordText.setText(keywords);
        keywordText.setSelection(keywords.length());
        performSearch(keywords, false);

    }

    private void performSearch(String query, boolean fullSearch) {
        hideSoftKeyboard();
        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        String qvlibrary = sharedPreferences.getString(Constants.PREF_QV_LIBRARY_PATH, null);
        if (TextUtils.isEmpty(qvlibrary)) {
            Toast.makeText(this, R.string.qvlibrary_none, Toast.LENGTH_LONG).show();
            return;
        }
        new SearchAsyncTask(this).execute(qvlibrary, query.trim(), fullSearch ? "fs" : "");
    }

    private String getSearchStr(Intent queryIntent) {
        final String queryAction = queryIntent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            return queryIntent.getStringExtra(SearchManager.QUERY);
        }

        return "";

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NoteModel noteModel = list.get(position);
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra("note", noteModel);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search:
                performSearch(keywordText.getText().toString(), true);
                break;
        }
    }

    class SearchAsyncTask extends AsyncTask<String, Integer, List<NoteModel>> {
        private ProgressDialog dialog;
        private String query;

        SearchAsyncTask(NoteSearchActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.searching));
            dialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            dialog.setProgress(values[0]);
        }

        @Override
        protected List<NoteModel> doInBackground(String... args) {
            List<NoteModel> results = new ArrayList<>();
            String qvlibrary = args[0];
            this.query = args[1];
            boolean fullSearch = "fs".equals(args[2]);
            File qvlibraryBase = new File(qvlibrary);
            File[] notebookDirs = qvlibraryBase.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return dir.isDirectory() && name.endsWith(".qvnotebook");
                }
            });
            int percent = 0;
            if (notebookDirs != null) {
                for (int i = 0; i < notebookDirs.length; i++) {
                    File notebookDir = notebookDirs[i];
                    percent = (i + 1) * 100 / notebookDirs.length;
                    if (percent > 100) {
                        percent = 100;
                    }
                    try {
                        publishProgress(percent);
                        File meta = new File(notebookDir, "meta.json");
                        JSONObject jsonObject = Utils.readFileToJson(meta);
                        NotebookModel notebookModel = new NotebookModel();
                        notebookModel.setName(jsonObject.getString("name"));
                        notebookModel.setUuid(jsonObject.getString("uuid"));
                        notebookModel.setDir(notebookDir.getAbsolutePath());

                        List<NoteModel> searchedNotes = Utils.searchNotes(notebookModel, query, fullSearch);
                        results.addAll(searchedNotes);
                        if (results.size() > 50) {
                            break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "error parser notebook", e);
                    }
                }
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<NoteModel> results) {
            list.clear();
            list.addAll(results);
            noteListAdapter.notifyDataSetChanged();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(query);
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

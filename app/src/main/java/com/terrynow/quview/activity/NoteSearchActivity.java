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

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.terrynow.quview.R;
import com.terrynow.quview.adapter.NoteListAdapter;
import com.terrynow.quview.model.NoteModel;
import com.terrynow.quview.model.NotebookModel;
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
public class NoteSearchActivity extends NoteBaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "NoteSearchActivity";

    private NoteListAdapter noteListAdapter;
    private List<NoteModel> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        if (isFinishing()) {
            return;
        }

        ListView noteListView = findViewById(R.id.note_list);
        noteListAdapter = new NoteListAdapter(this, R.layout.list_note, list);
        noteListView.setAdapter(noteListAdapter);
        noteListView.setOnItemClickListener(this);

        performSearch(getSearchStr(getIntent()));

    }

    private void performSearch(String query) {
        query(query.trim());
    }

    private void query(String query) {
        File qvlibraryBase = new File(Environment.getExternalStorageDirectory(),
                "Download/Quiver.qvlibrary");
        list.clear();
        File[] notebookDirs = qvlibraryBase.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return dir.isDirectory() && name.endsWith(".qvnotebook");
            }
        });
        if (notebookDirs != null) {
            for (File notebookDir : notebookDirs) {
                try {
                    File meta = new File(notebookDir, "meta.json");
                    JSONObject jsonObject = Utils.readFileToJson(meta);
                    NotebookModel notebookModel = new NotebookModel();
                    notebookModel.setName(jsonObject.getString("name"));
                    notebookModel.setUuid(jsonObject.getString("uuid"));
                    notebookModel.setDir(notebookDir.getAbsolutePath());

                    List<NoteModel> searchedNotes = Utils.searchNotes(notebookModel, query);
                    list.addAll(searchedNotes);
                    if (list.size() > 50) {
                        break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error parser notebook", e);
                }
            }
        }
        noteListAdapter.notifyDataSetChanged();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(query);
        }
    }

    private String getSearchStr(Intent queryIntent) {
        // get and process search query here
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
}

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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
 * @date 2019-10-15 13:33
 * @description
 */
public class NoteListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "NoteListActivity";

    private NotebookModel notebookModel;

    private ListView noteListView;
    private NoteListAdapter noteListAdapter;
    private List<NoteModel> list = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        notebookModel = (NotebookModel) getIntent().getSerializableExtra("notebook");

        getSupportActionBar().setTitle(notebookModel.getName());

        noteListView = findViewById(R.id.note_list);
        noteListAdapter = new NoteListAdapter(this, R.layout.list_note, list);
        noteListView.setAdapter(noteListAdapter);
        noteListView.setOnItemClickListener(this);

        loadNotes();
    }

    private void loadNotes() {
        File base = new File(notebookModel.getDir());
        System.out.println(base.getAbsoluteFile());
        System.out.println(base.exists());
        list.clear();
        File[] noteDirs = base.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return dir.isDirectory() && name.endsWith(".qvnote");
            }
        });
        if (noteDirs != null) {
            for (File noteDir : noteDirs) {
                try {
                    File meta = new File(noteDir, "meta.json");
                    JSONObject jsonObject = Utils.readFileToJson(meta);
                    NoteModel noteModel = new NoteModel();
                    noteModel.setName(jsonObject.getString("title"));
                    noteModel.setUuid(jsonObject.getString("uuid"));
                    noteModel.setDir(noteDir.getAbsolutePath());
                    list.add(noteModel);
                } catch (Exception e) {
                    Log.e(TAG, "error parser note", e);
                }
            }
        }
        noteListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NoteModel noteModel = list.get(position);
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra("note", noteModel);
        startActivity(intent);
    }
}

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
import com.terrynow.quview.R;
import com.terrynow.quview.adapter.NoteListAdapter;
import com.terrynow.quview.model.NoteModel;
import com.terrynow.quview.model.NotebookModel;
import com.terrynow.quview.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Terry E-mail: yaoxinghuo at 126 dot com
 * @date 2019-10-15 13:33
 * @description
 */
public class NoteListActivity extends NoteBaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "NoteListActivity";

    private NotebookModel notebookModel;

    private NoteListAdapter noteListAdapter;
    private List<NoteModel> list = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        notebookModel = (NotebookModel) getIntent().getSerializableExtra("notebook");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(notebookModel.getName());
        }

        ListView noteListView = findViewById(R.id.note_list);
        noteListAdapter = new NoteListAdapter(this, R.layout.list_note, list);
        noteListView.setAdapter(noteListAdapter);
        noteListView.setOnItemClickListener(this);

        loadNotes();
    }

    private void loadNotes() {
        list.clear();
        try {
            list.addAll(Utils.searchNotes(notebookModel, null));
        } catch (Exception e) {
            Log.e(TAG, "error parser note", e);
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

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

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.terrynow.quview.R;
import com.terrynow.quview.adapter.NoteCellListAdapter;
import com.terrynow.quview.model.NoteCellModel;
import com.terrynow.quview.model.NoteDetailModel;
import com.terrynow.quview.model.NoteModel;
import com.terrynow.quview.util.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Terry E-mail: yaoxinghuo at 126 dot com
 * @date 2019-10-15 13:31
 * @description
 */
public class NoteDetailActivity extends AppCompatActivity {
    private static final String TAG = "NoteDetailActivity";

    private NoteModel noteModel;

    private ListView noteCellListView;
    private NoteCellListAdapter noteCellListAdapter;
    private List<NoteCellModel> list = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);

        noteModel = (NoteModel) getIntent().getSerializableExtra("note");

        getSupportActionBar().setTitle(noteModel.getName());

        noteCellListView = findViewById(R.id.cell_list);
        noteCellListAdapter = new NoteCellListAdapter(this, R.layout.list_notecell, list);
        noteCellListView.setAdapter(noteCellListAdapter);

        loadNoteDetail();
    }

    private void loadNoteDetail() {
        try {
            JSONObject jsonObject = Utils.readFileToJson(new File(noteModel.getDir(),
                    "content.json"));
            NoteDetailModel noteDetailModel = new NoteDetailModel();
            noteDetailModel.setTitle(jsonObject.getString("title"));
            JSONArray cellsArray = jsonObject.has("cells") ? jsonObject.getJSONArray("cells") :
                    new JSONArray();
            list.clear();
            for (int i = 0; i < cellsArray.length(); i++) {
                JSONObject cellObject = cellsArray.getJSONObject(i);
                NoteCellModel noteCellModel = new NoteCellModel();
                noteCellModel.setType(cellObject.getString("type"));
                noteCellModel.setLanguage(cellObject.getString("language"));
                noteCellModel.setData(cellObject.getString("data"));
                list.add(noteCellModel);
            }
            noteCellListAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "error parser note detail", e);
        }
    }
}

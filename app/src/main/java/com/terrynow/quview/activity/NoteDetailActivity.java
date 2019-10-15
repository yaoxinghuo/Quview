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
import android.widget.TextView;
import androidx.annotation.Nullable;
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
public class NoteDetailActivity extends NoteBaseActivity {
    private static final String TAG = "NoteDetailActivity";

    //    private static final String[] availableTypes = new String[]{"text", "code", "markdown", "latex", "diagram"};
    private static final String[] availableTypes = new String[]{"text", "code", "markdown"};

    private NoteModel noteModel;

    private NoteCellListAdapter noteCellListAdapter;
    private List<NoteCellModel> list = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        noteModel = (NoteModel) getIntent().getSerializableExtra("note");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(noteModel.getName());
        }

        ListView noteCellListView = findViewById(R.id.cell_list);
        noteCellListAdapter = new NoteCellListAdapter(this, R.layout.list_notecell, list);
        noteCellListView.setAdapter(noteCellListAdapter);

        TextView nameView = findViewById(R.id.name);
        nameView.setText(noteModel.getName());

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
                String type = cellObject.getString("type");
                if (!isAvailableType(type)) {
                    continue;
                }
                NoteCellModel noteCellModel = new NoteCellModel();
                noteCellModel.setType(type);
                noteCellModel.setLanguage(cellObject.optString("language"));
                noteCellModel.setData(cellObject.getString("data"));
                list.add(noteCellModel);
            }
            noteCellListAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "error parser note detail", e);
        }
    }

    private boolean isAvailableType(String type) {
        for (String availableType : availableTypes) {
            if (availableType.equals(type)) {
                return true;
            }
        }
        return false;
    }
}

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

package com.terrynow.quview.util;

import android.text.TextUtils;
import com.terrynow.quview.model.NoteModel;
import com.terrynow.quview.model.NotebookModel;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Terry E-mail: yaoxinghuo at 126 dot com
 * @date 2019-10-15 13:21
 * @description
 */
public class Utils {
    public static JSONObject readFileToJson(File file) throws Exception {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while (true) {
                line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append("\n");
            }
            return new JSONObject(sb.toString());
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static List<NoteModel> searchNotes(NotebookModel notebookModel, String query) throws Exception {
        List<NoteModel> notes = new ArrayList<>();
        File base = new File(notebookModel.getDir());
        File[] noteDirs = base.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return dir.isDirectory() && name.endsWith(".qvnote");
            }
        });
        if (noteDirs != null) {
            for (File noteDir : noteDirs) {
                File meta = new File(noteDir, "meta.json");
                JSONObject jsonObject = Utils.readFileToJson(meta);
                String title = jsonObject.getString("title");
                if (isMatch(title, query)) {
                    NoteModel noteModel = new NoteModel();
                    noteModel.setName(title);
                    noteModel.setUuid(jsonObject.getString("uuid"));
                    noteModel.setCreateDate(new Date(jsonObject.optLong("created_at") * 1000));
                    noteModel.setUpdateDate(new Date(jsonObject.optLong("updated_at") * 1000));
                    noteModel.setDir(noteDir.getAbsolutePath());
                    notes.add(noteModel);
                }
            }
        }
        return notes;
    }

    private static boolean isMatch(String title, String query) {
        if (TextUtils.isEmpty(query)) {
            return true;
        }
        String[] parts = query.split("\\s");
        for (String part : parts) {
            if (!Pattern.compile(Pattern.quote(part), Pattern.CASE_INSENSITIVE).matcher(title).find()) {
                return false;
            }
        }
        return true;
    }
}

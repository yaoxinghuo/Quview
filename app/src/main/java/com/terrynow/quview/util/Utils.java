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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

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
    //    private static final String[] supportedTypes = new String[]{"text", "code", "markdown",
    //    "latex", "diagram"};
    private static final String[] supportedTypes = new String[]{"text", "code", "markdown"};

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

    public static List<NoteModel> searchNotes(NotebookModel notebookModel, String query, boolean fullSearch) throws Exception {
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
                boolean match = false;
                JSONObject metaObject = null;
                if (fullSearch) {
                    JSONObject jsonObject = Utils.readFileToJson(new File(noteDir, "content.json"));
                    String title = jsonObject.getString("title");
                    match = isMatch(title, query);
                    if (!match) {
                        JSONArray cellsArray = jsonObject.has("cells") ? jsonObject.getJSONArray("cells") :
                                new JSONArray();
                        match = isMatch(cellsArray, query);
                    }

                } else {
                    metaObject = Utils.readFileToJson(new File(noteDir, "meta.json"));
                    String title = metaObject.getString("title");
                    match = isMatch(title, query);
                }
                if (match) {
                    if (metaObject == null) {
                        metaObject = Utils.readFileToJson(new File(noteDir, "meta.json"));
                    }
                    NoteModel noteModel = new NoteModel();
                    noteModel.setName(metaObject.getString("title"));
                    noteModel.setUuid(metaObject.getString("uuid"));
                    noteModel.setCreateDate(new Date(metaObject.optLong("created_at") * 1000));
                    noteModel.setUpdateDate(new Date(metaObject.optLong("updated_at") * 1000));
                    noteModel.setDir(noteDir.getAbsolutePath());
                    notes.add(noteModel);
                }
            }
        }
        return notes;
    }

    private static boolean isMatch(JSONArray cellsArray, String query) {
        try {
            for (int i = 0; i < cellsArray.length(); i++) {
                JSONObject cellObject = cellsArray.getJSONObject(i);
                String type = cellObject.getString("type");
                if (!isSupportedType(type)) {
                    continue;
                }
                if ("text".equals(type)) {
                    if (isMatch(Jsoup.parse(cellObject.getString("data")).text(), query)) {
                        return true;
                    }
                } else {
                    if (isMatch(cellObject.getString("data"), query)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (JSONException e) {
            return false;
        }
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

    public static boolean isSupportedType(String type) {
        for (String supportedType : supportedTypes) {
            if (supportedType.equals(type)) {
                return true;
            }
        }
        return false;
    }
}

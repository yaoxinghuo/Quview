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

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import androidx.core.content.ContextCompat;
import com.terrynow.quview.R;
import com.terrynow.quview.adapter.NotebookListAdapter;
import com.terrynow.quview.model.NotebookModel;
import com.terrynow.quview.util.Utils;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends NoteBaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    private ListView notebookListView;
    private NotebookListAdapter notebookListAdapter;
    private List<NotebookModel> list = new ArrayList<>();

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notebookListView = findViewById(R.id.notebook_list);
        notebookListAdapter = new NotebookListAdapter(this, R.layout.list_notebook, list);
        notebookListView.setAdapter(notebookListAdapter);
        notebookListView.setOnItemClickListener(this);

        if (!requestForPermission()) {
            return;
        }

        loadNotebooks();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    private void loadNotebooks() {
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
                    list.add(notebookModel);
                } catch (Exception e) {
                    Log.e(TAG, "error parser notebook", e);
                }
            }
        }
        notebookListAdapter.notifyDataSetChanged();
    }

    public final String[] EXTERNAL_PERMS = {Manifest.permission.READ_EXTERNAL_STORAGE};

    public final int EXTERNAL_REQUEST = 138;

    public boolean requestForPermission() {

        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            if (!canAccessExternalSd()) {
                requestPermissions(EXTERNAL_PERMS, EXTERNAL_REQUEST);
                return canAccessExternalSd();
            }
        }

        return true;
    }

    public boolean canAccessExternalSd() {
        return (hasPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE));
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NotebookModel notebookModel = list.get(position);
        Intent intent = new Intent(this, NoteListActivity.class);
        intent.putExtra("notebook", notebookModel);
        startActivity(intent);
    }
}

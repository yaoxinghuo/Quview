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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.terrynow.quview.R;
import com.terrynow.quview.adapter.NotebookListAdapter;
import com.terrynow.quview.model.NotebookModel;
import com.terrynow.quview.util.Constants;
import com.terrynow.quview.util.GetPathUtils;
import com.terrynow.quview.util.Utils;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends NoteBaseActivity implements AdapterView.OnItemClickListener,
        View.OnClickListener {

    private static final String TAG = "MainActivity";

    private NotebookListAdapter notebookListAdapter;
    private List<NotebookModel> list = new ArrayList<>();
    private SearchView searchView;

    public final String[] EXTERNAL_PERMS = {Manifest.permission.READ_EXTERNAL_STORAGE};

    public static final int PERMISSION_REQUEST_CODE = 100;
    public static final int PICK_FOLDER_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView notebookListView = findViewById(R.id.notebook_list);
        notebookListAdapter = new NotebookListAdapter(this, R.layout.list_notebook, list);
        notebookListView.setAdapter(notebookListAdapter);
        notebookListView.setOnItemClickListener(this);

        findViewById(R.id.qvlibrary_choose).setOnClickListener(this);

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

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                searchView.onActionViewCollapsed();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    private void loadNotebooks() {
        SharedPreferences sharedPreferences = getSharedPreferences("preferences",
                Context.MODE_PRIVATE);
        String qvlibrary = sharedPreferences.getString(Constants.PREF_QV_LIBRARY_PATH, null);
        TextView nameView = findViewById(R.id.name);
        if (TextUtils.isEmpty(qvlibrary)) {
            nameView.setText(getString(R.string.qvlibrary_none));
            return;
        } else {
            nameView.setText(qvlibrary);
        }

        File qvLibraryBase = new File(qvlibrary);
//        File qvLibraryBase = new File(Environment.getExternalStorageDirectory(),
//                "Download/Quiver.qvlibrary");
        list.clear();
        File[] notebookDirs = qvLibraryBase.listFiles(new FilenameFilter() {
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

    private boolean requestForPermission() {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            if (!canAccessExternalSd()) {
                requestPermissions(EXTERNAL_PERMS, PERMISSION_REQUEST_CODE);
                return canAccessExternalSd();
            }
        }
        return true;
    }

    private boolean canAccessExternalSd() {
        return (hasPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE));
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));
    }

    private void pickQvLibraryFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, PICK_FOLDER_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case PICK_FOLDER_REQUEST_CODE:
                Uri folderUri = data.getData();
                if (folderUri != null) {
                    String path = GetPathUtils.getDirectoryPathFromUri(this, folderUri);
                    Log.d(TAG, "qv library choose path -> " + path);
                    File qvLibraryBase = new File(path);
                    File[] notebookDirs = qvLibraryBase.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return dir.isDirectory() && name.endsWith(".qvnotebook");
                        }
                    });
                    if (notebookDirs == null || notebookDirs.length == 0) {
                        Toast.makeText(this, getString(R.string.qvlibrary_no_notebooks),
                                Toast.LENGTH_LONG).show();
                    } else {
                        SharedPreferences sharedPreferences = getSharedPreferences("preferences",
                                Context.MODE_PRIVATE);
                        sharedPreferences.edit().putString(Constants.PREF_QV_LIBRARY_PATH, path).commit();
                        loadNotebooks();
                    }
                }
                break;
            case PERMISSION_REQUEST_CODE:
                if (canAccessExternalSd()) {
                    loadNotebooks();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NotebookModel notebookModel = list.get(position);
        Intent intent = new Intent(this, NoteListActivity.class);
        intent.putExtra("notebook", notebookModel);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.qvlibrary_choose:
                pickQvLibraryFolder();
                break;
        }
    }
}

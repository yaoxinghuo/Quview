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

package com.terrynow.quview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.terrynow.quview.R;
import com.terrynow.quview.model.NoteModel;

import java.text.DateFormat;
import java.util.List;

/**
 * @author Terry E-mail: yaoxinghuo at 126 dot com
 * @date 2019-10-15 13:36
 * @description
 */
public class NoteListAdapter extends ArrayAdapter<NoteModel> {
    private int layoutId;

    public NoteListAdapter(@NonNull Context context, int resource,
                           @NonNull List<NoteModel> objects) {
        super(context, resource, objects);
        this.layoutId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NoteModel noteModel = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
        TextView titleView = view.findViewById(R.id.title);
        TextView summaryView = view.findViewById(R.id.summary);
        titleView.setText(noteModel.getName());
        summaryView.setText(DateFormat.getDateInstance().format(noteModel.getUpdateDate()));

        return view;
    }
}

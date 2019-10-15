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
import androidx.annotation.NonNull;
import com.terrynow.quview.R;
import com.terrynow.quview.model.NoteCellModel;
import io.github.kbiakov.codeview.CodeView;

import java.util.List;

/**
 * @author Terry E-mail: yaoxinghuo at 126 dot com
 * @date 2019-10-15 14:27
 * @description
 */
public class NoteCellListAdapter extends ArrayAdapter<NoteCellModel> {
    private int layoutId;

    public NoteCellListAdapter(@NonNull Context context, int resource,
                               @NonNull List<NoteCellModel> objects) {
        super(context, resource, objects);
        this.layoutId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NoteCellModel notecellModel = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
        CodeView cellView = view.findViewById(R.id.cell);
        cellView.setCode(notecellModel.getData());
        return view;
    }
}

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
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import com.protectsoft.webviewcode.Codeview;
import com.terrynow.quview.R;
import com.terrynow.quview.model.NoteCellModel;
import us.feras.mdv.MarkdownView;

import java.util.List;

/**
 * @author Terry E-mail: yaoxinghuo at 126 dot com
 * @date 2019-10-15 14:27
 * @description
 */
public class NoteCellListAdapter extends ArrayAdapter<NoteCellModel> {
    private int layoutId;
    private String dir;

    public NoteCellListAdapter(@NonNull Context context, int resource,
                               @NonNull List<NoteCellModel> objects, String dir) {
        super(context, resource, objects);
        this.layoutId = resource;
        this.dir = dir;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(getContext()).inflate(layoutId,
                parent, false);
        NoteCellModel notecellModel = getItem(position);
        String type = notecellModel.getType();
        if ("markdown".equals(type)) {
            MarkdownView markdownView = view.findViewById(R.id.cell_markdown);
            markdownView.setVisibility(View.VISIBLE);
            markdownView.loadMarkdown(notecellModel.getData());
        } else if ("code".equals(type)) {
//            CodeView codeView = view.findViewById(R.id.cell_code);
//            codeView.setVisibility(View.VISIBLE);
//            codeView.setCode(notecellModel.getData(), notecellModel.getLanguage());
            WebView webView = view.findViewById(R.id.cell_web);
            webView.setVisibility(View.VISIBLE);
            Codeview codeview = Codeview.with(getContext()).setAutoWrap(true);
            codeview.withCode(notecellModel.getData()).setLang(notecellModel.getLanguage()).into(webView);
        } else {
            //type: text
            WebView webView = view.findViewById(R.id.cell_web);
            webView.setVisibility(View.VISIBLE);
            Codeview codeview = Codeview.with(getContext()).setAutoWrap(true);
            String imagePath = "file://" + dir + "/resources/";
            String str = notecellModel.getData()
                    .replaceAll(" src=\"quiver-image-url/"
                            , " style=\"width: 100%;height: auto;\" src=\"" + imagePath);
            codeview.withHtml(str).into(webView);
        }
        //TODO how about latex, diagram?

        return view;
    }
}

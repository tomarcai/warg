package net.yura.domination.android;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.yura.domination.R;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mobile.MiniUtil;
import net.yura.domination.mobile.flashgui.DominationMain;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TabHost;
import android.app.Activity;
import androidx.documentfile.provider.DocumentFile;

public class AboutActivity extends Activity implements TabHost.TabContentFactory {

    public static final int RC_OPEN_DOCUMENT_TREE = 5;

    private static final String prefix = "file:///android_asset/";
    private WebView aboutWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ResourceBundle resb = TranslationBundle.getBundle();
        //setTitle( resb.getString("about.title") );

        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabHost.TabContentFactory factory = this;

        tabHost.addTab( tabHost.newTabSpec("about").setIndicator( resb.getString("about.title") ).setContent(factory) );
        tabHost.addTab( tabHost.newTabSpec("credits").setIndicator( resb.getString("about.tab.credits") ).setContent(factory) );
        tabHost.addTab( tabHost.newTabSpec("license").setIndicator( resb.getString("about.tab.license") ).setContent(factory) );
        tabHost.addTab( tabHost.newTabSpec("changelog").setIndicator( resb.getString("about.tab.changelog") ).setContent(factory) );

        tabHost.setCurrentTab(0);
    }

    @Override
    public View createTabContent(String tag) {
        WebView webView = new WebView(this);
        if ("about".equals(tag)) {
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if ((url.startsWith("file://") || url.startsWith("content://")) && !url.startsWith(prefix)) {

                        // technically we can start using this API if >= lolli
                        // but its so broken and buggy in those versions so we only use it when we have to
                        // https://developer.android.com/training/data-storage/use-cases#opt-out-in-production-app
                        // we HAVE to start using the new method in 30, as fresh installs have no option to use old method
                        // Android 11 (API level 30) https://developer.android.com/about/versions/11/privacy/storage
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API-30
                            openDirectory(url);
                            return true;
                        }
                        else {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(url));
                                startActivity(intent);
                            } catch (Exception ex) {
                                Logger.getLogger(AboutActivity.class.getName()).log(Level.INFO, "cant open " + url, ex);
                            }
                            return true;
                        }
                    }
                    return super.shouldOverrideUrlLoading(view, url);
                }
            });
            aboutWebView = webView;
            loadAbout();
        }
        else if ("credits".equals(tag)){
            webView.loadUrl(prefix+"help/game_credits.htm");
        }
        else if ("license".equals(tag)){
            webView.loadUrl(prefix+"gpl.txt");
        }
        else if ("changelog".equals(tag)){
            webView.loadUrl(prefix+"ChangeLog.txt");
        }
        else {
            throw new IllegalArgumentException("strange tag "+tag);
        }
        return webView;
    }

    private void loadAbout() {
        String aboutHtml = MiniUtil.getAboutHtml();
        aboutWebView.loadDataWithBaseURL(prefix, aboutHtml, "text/html", "UTF-8", null);

        // OLD way of displaying html, does not support catching file:// links
        //WebSettings settings = webView.getSettings();
        //settings.setDefaultTextEncodingName("utf-8"); // UTF-8 here works on only v2 android
        // hack to fix bug on android
        //aboutHtml = aboutHtml.replace("%", "%25").replace("#", "%23").replace("'", "%27").replace("?", "%3f");
        //webView.loadData(aboutHtml, "text/html; charset=utf-8", null); // UTF-8 here works on only v4 android
    }

    public void openDirectory(String url) {
        // Choose a directory using the system's file picker.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        //intent.putExtra("android.provider.extra.SHOW_ADVANCED", true);
        //intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(url));

        startActivityForResult(intent, RC_OPEN_DOCUMENT_TREE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == RC_OPEN_DOCUMENT_TREE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri treeUri = resultData.getData();

                // DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri));
                DocumentFile doc = DocumentFile.fromTreeUri(this, treeUri);

                // for unknown crazy android reasons we now need to convert this tree url into a document url.
                // treeUri     = content://com.android.externalstorage.documents/tree/primary%3ADomination%20Maps
                // documentUri = content://com.android.externalstorage.documents/tree/primary%3ADomination%20Maps/document/primary%3ADomination%20Maps
                // childrenUri = content://com.android.externalstorage.documents/tree/primary%3ADomination%20Maps/document/primary%3ADomination%20Maps/children
                // mapUri      = content://com.android.externalstorage.documents/tree/primary%3ADomination%20Maps/document/primary%3ADomination%20Maps%2FNuclearAssaults.map
                Uri documentUri = doc.getUri();
                String url = documentUri.toString();
                // as this is a folder, other parts of the app will get confused if the url does not end in /
                if (!url.endsWith("/")) { url = url + "/"; }

                // tell android to remember for future that i have access to this folder
                getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // tell the game to remember this location so it can load maps from here next time.
                DominationMain.setExternalMapsDir(url);
                // reload the webview to show the new location
                loadAbout();
            }
        }
    }
}

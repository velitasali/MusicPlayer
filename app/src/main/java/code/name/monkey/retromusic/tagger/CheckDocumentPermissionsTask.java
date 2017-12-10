package code.name.monkey.retromusic.tagger;

import android.os.AsyncTask;
import android.support.v4.provider.DocumentFile;

import java.util.List;

import code.name.monkey.retromusic.util.RetroUtils;

public class CheckDocumentPermissionsTask extends AsyncTask<Void, Void, Boolean> {

    private List<String> paths;
    private List<DocumentFile> documentFiles;
    private PermissionCheckListener listener;

    public CheckDocumentPermissionsTask(List<String> paths, List<DocumentFile> documentFiles, PermissionCheckListener listener) {
        this.paths = paths;
        this.documentFiles = documentFiles;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return !RetroUtils.hasLollipop() ||
                !TaggerUtils.requiresPermission(paths) ||
                TaggerUtils.hasDocumentTreePermission(documentFiles, paths);
    }

    @Override
    protected void onPostExecute(Boolean hasPermission) {
        super.onPostExecute(hasPermission);

        if (listener != null) {
            listener.onPermissionCheck(hasPermission);
        }
    }

    public interface PermissionCheckListener {
        void onPermissionCheck(boolean hasPermission);
    }
}
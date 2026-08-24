package org.telegram.messenger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.web.HttpGetFileTask;
import org.telegram.ui.web.HttpGetTask;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Looks for a new build among the releases of a GitHub repository, rather than asking Telegram
 * for one, and hands what it finds to the update bar and the update dialog the app already has.
 *
 * A release is read as an update when its tag carries both a version and a version code, written
 * as 12.10.0-7031, with or without a leading v. The code can also be given on a line of the
 * release body, as "version_code: 7031", which wins over the tag. The changelog shown is the body
 * of the release, and what is downloaded is its first asset ending in .apk.
 *
 * The pre-release flag decides which releases are looked at: the beta build takes the ones marked
 * as a pre-release, the standalone build takes the ones that are not.
 */
public class GithubUpdaterController {

    private static GithubUpdaterController instance;

    public static GithubUpdaterController getInstance() {
        if (instance == null) {
            instance = new GithubUpdaterController();
        }
        return instance;
    }

    private String repository;
    private boolean preRelease;
    private int projectVersionCode;

    /**
     * Called once by the build that wants its updates from GitHub. The version code passed is the
     * one the project carries, which is not the one the package holds: the build multiplies it by
     * ten and adds the number of the abi it was built for, and a release names the plain one.
     */
    public static void configure(String repository, boolean preRelease, int projectVersionCode) {
        final GithubUpdaterController controller = getInstance();
        controller.repository = repository;
        controller.preRelease = preRelease;
        controller.projectVersionCode = projectVersionCode;
    }

    public boolean isConfigured() {
        return !TextUtils.isEmpty(repository);
    }

    private String version;
    private int versionCode;
    private String changelog;
    private String path;
    private long lastCheck;

    private String fileUrl;

    public GithubUpdaterController() {
        load();
    }

    private SharedPreferences getSharedPreferences() {
        return ApplicationLoader.applicationContext.getSharedPreferences("githubupdate", Activity.MODE_PRIVATE);
    }

    private void load() {
        final SharedPreferences prefs = getSharedPreferences();

        version = prefs.getString("version", null);
        versionCode = prefs.getInt("versionCode", 0);
        changelog = prefs.getString("changelog", null);
        path = prefs.getString("path", null);
        fileUrl = prefs.getString("fileUrl", null);
        lastCheck = prefs.getLong("lastCheck", 0L);

        if (getCurrentVersionCode() >= versionCode || !TextUtils.isEmpty(path) && !new File(path).exists()) {
            forget();
        }
    }

    private void forget() {
        version = null;
        versionCode = 0;
        path = null;
        changelog = null;
        fileUrl = null;
        lastCheck = 0;
        save();
    }

    private void save() {
        final SharedPreferences.Editor e = getSharedPreferences().edit();
        put(e, "version", version);
        put(e, "changelog", changelog);
        put(e, "fileUrl", fileUrl);
        put(e, "path", path);
        if (versionCode == 0) {
            e.remove("versionCode");
        } else {
            e.putInt("versionCode", versionCode);
        }
        if (lastCheck == 0) {
            e.remove("lastCheck");
        } else {
            e.putLong("lastCheck", lastCheck);
        }
        e.apply();
    }

    private void put(SharedPreferences.Editor e, String key, String value) {
        if (TextUtils.isEmpty(value)) {
            e.remove(key);
        } else {
            e.putString(key, value);
        }
    }

    private final static long CHECK_INTERVAL_PAUSED = 1000 * 60 * 60 * 24; // 1 day
    private final static long CHECK_INTERVAL = 1000 * 60 * 20; // 20 minutes

    private boolean firstCheck = true;
    private boolean checkingForUpdate;
    private final Runnable scheduledUpdateCheck = () -> checkForUpdate(false, null);

    public void checkForUpdate(boolean force, Runnable whenDone) {
        if (checkingForUpdate || !isConfigured()) {
            if (whenDone != null) {
                whenDone.run();
            }
            return;
        }

        if (firstCheck) {
            force = true;
        }
        if (!force && System.currentTimeMillis() - lastCheck < (ApplicationLoader.mainInterfacePaused ? CHECK_INTERVAL_PAUSED : CHECK_INTERVAL)) {
            if (whenDone != null) {
                whenDone.run();
            }
            return;
        }

        final String url = "https://api.github.com/repos/" + repository + "/releases?per_page=30";
        checkingForUpdate = true;
        firstCheck = false;
        new HttpGetTask(str -> AndroidUtilities.runOnUIThread(() -> {
            checkingForUpdate = false;
            try {
                onReleasesReceived(str, whenDone);
            } catch (Exception e) {
                FileLog.e("Failed to check for github update at " + url + " received: " + str, e);
                if (whenDone != null) {
                    whenDone.run();
                }
            }
        }))
            .setHeader("Accept", "application/vnd.github+json")
            .setHeader("X-GitHub-Api-Version", "2022-11-28")
            .setHeader("User-Agent", "Telegram-Android-Updater")
            .execute(url);
    }

    private void onReleasesReceived(String str, Runnable whenDone) throws Exception {
        final JSONArray releases = new JSONArray(str);

        String newVersion = null;
        int newVersionCode = 0;
        String newChangelog = null;
        String newFileUrl = null;

        for (int i = 0; i < releases.length(); ++i) {
            final JSONObject release = releases.optJSONObject(i);
            if (release == null || release.optBoolean("draft", false) || release.optBoolean("prerelease", false) != preRelease) {
                continue;
            }
            final String apk = findApkAsset(release.optJSONArray("assets"));
            if (apk == null) {
                continue;
            }
            final String body = release.optString("body", null);
            final String tag = release.optString("tag_name", null);
            final String parsedVersion = parseVersion(tag);
            final int parsedVersionCode = parseVersionCode(body, tag);
            if (parsedVersion == null || parsedVersionCode == 0) {
                FileLog.e("github update: release " + tag + " carries no version code, skipping it");
                continue;
            }
            // the releases arrive newest first, so the first one that is whole is the one to take
            newVersion = parsedVersion;
            newVersionCode = parsedVersionCode;
            newChangelog = TextUtils.isEmpty(body) ? null : body.trim();
            newFileUrl = apk;
            break;
        }

        final int oldVersionCode = versionCode;

        if (newVersion == null || newVersionCode <= getCurrentVersionCode()) {
            // nothing newer than what is installed
            if (!TextUtils.isEmpty(path)) {
                deleteFile(path);
            }
            forget();
        } else if (newVersionCode != versionCode) {
            // a build that is not the one already known: whatever came down before is of no use
            if (!TextUtils.isEmpty(path)) {
                deleteFile(path);
                path = null;
            }
            version = newVersion;
            versionCode = newVersionCode;
            changelog = newChangelog;
            fileUrl = newFileUrl;
        } else {
            // the same build, with the text and the file it is downloaded from brought up to date
            version = newVersion;
            changelog = newChangelog;
            fileUrl = newFileUrl;
        }

        lastCheck = System.currentTimeMillis();
        save();

        if (versionCode != oldVersionCode) {
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
        }

        AndroidUtilities.cancelRunOnUIThread(scheduledUpdateCheck);
        AndroidUtilities.runOnUIThread(scheduledUpdateCheck, CHECK_INTERVAL);

        if (whenDone != null) {
            whenDone.run();
        } else if (versionCode != oldVersionCode && !ApplicationLoader.mainInterfacePaused) {
            final Context context = LaunchActivity.instance != null ? LaunchActivity.instance : ApplicationLoader.applicationContext;
            final BetaUpdate pendingUpdate = getUpdate();
            if (context != null && pendingUpdate != null) {
                ApplicationLoader.applicationLoaderInstance.showCustomUpdateAppPopup(context, pendingUpdate, UserConfig.selectedAccount);
            }
        }
    }

    private String findApkAsset(JSONArray assets) {
        if (assets == null) {
            return null;
        }
        for (int i = 0; i < assets.length(); ++i) {
            final JSONObject asset = assets.optJSONObject(i);
            if (asset == null) {
                continue;
            }
            final String name = asset.optString("name", "");
            if (name.toLowerCase().endsWith(".apk")) {
                final String url = asset.optString("browser_download_url", null);
                if (!TextUtils.isEmpty(url)) {
                    return url;
                }
            }
        }
        return null;
    }

    private static final Pattern TAG_PATTERN = Pattern.compile("^[vV]?(\\d+(?:\\.\\d+)*)(?:[-+_](\\d+))?$");
    private static final Pattern BODY_VERSION_CODE_PATTERN = Pattern.compile("(?im)^\\s*version[ _-]?code\\s*[:=]\\s*(\\d+)\\s*$");

    private String parseVersion(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return null;
        }
        final Matcher m = TAG_PATTERN.matcher(tag.trim());
        return m.matches() ? m.group(1) : null;
    }

    private int parseVersionCode(String body, String tag) {
        if (!TextUtils.isEmpty(body)) {
            final Matcher m = BODY_VERSION_CODE_PATTERN.matcher(body);
            if (m.find()) {
                try {
                    return Integer.parseInt(m.group(1));
                } catch (Exception ignore) {}
            }
        }
        if (!TextUtils.isEmpty(tag)) {
            final Matcher m = TAG_PATTERN.matcher(tag.trim());
            if (m.matches() && m.group(2) != null) {
                try {
                    return Integer.parseInt(m.group(2));
                } catch (Exception ignore) {}
            }
        }
        return 0;
    }

    private void deleteFile(String path) {
        try {
            new File(path).delete();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public BetaUpdate getUpdate() {
        if (version == null || versionCode == 0) {
            return null;
        }
        return new BetaUpdate(version, versionCode, changelog);
    }

    private boolean downloading;
    private float downloadingProgress;
    private HttpGetFileTask downloadingTask;

    public void downloadUpdate() {
        downloadUpdate(false);
    }

    private void downloadUpdate(boolean triedGettingFileUrl) {
        if (downloading || !TextUtils.isEmpty(path)) return;

        downloading = true;
        downloadingProgress = 0.0f;
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateLoading);

        if (TextUtils.isEmpty(fileUrl)) {
            if (!triedGettingFileUrl) {
                checkForUpdate(true, () -> downloadUpdate(true));
            } else {
                downloading = false;
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
            }
            return;
        }

        downloadingTask = new HttpGetFileTask(
            downloadedFile -> AndroidUtilities.runOnUIThread(() -> {
                if (downloadedFile != null) {
                    if (!TextUtils.isEmpty(path)) {
                        deleteFile(path);
                    }
                    path = downloadedFile.getAbsolutePath();
                    save();
                    downloadingProgress = 1.0f;
                }
                downloading = false;
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
            }),
            progress -> {
                downloadingProgress = progress;
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateLoading);
            }
        ).setOverrideExtension("apk");
        downloadingTask.execute(fileUrl);
    }

    public void cancelDownloadingUpdate() {
        if (!downloading) return;
        if (downloadingTask != null) {
            downloadingTask.cancel(false);
        }
        downloading = false;
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
    }

    public boolean isDownloading() {
        return downloading;
    }

    public float getDownloadingProgress() {
        return downloadingProgress;
    }

    public File getDownloadedFile() {
        if (path == null) {
            return null;
        }
        final File file = new File(path);
        if (!file.exists()) {
            path = null;
            save();
            return null;
        }
        return file;
    }

    private int getCurrentVersionCode() {
        if (projectVersionCode > 0) {
            return projectVersionCode;
        }
        try {
            // nothing said what the project carries, so undo what the build did to it
            return ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0).versionCode / 10;
        } catch (Exception e) {
            FileLog.e(e);
            return 0;
        }
    }
}

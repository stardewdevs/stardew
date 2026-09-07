package io.stardew.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import io.stardew.R;
import io.stardew.shared.data.DataUtils;
import io.stardew.shared.data.IntentUtils;
import io.stardew.shared.termux.plugins.TermuxPluginUtils;
import io.stardew.shared.termux.file.TermuxFileUtils;
import io.stardew.shared.file.filesystem.FileType;
import io.stardew.shared.errors.Errno;
import io.stardew.shared.errors.Error;
import io.stardew.shared.termux.TermuxConstants;
import io.stardew.shared.termux.TermuxConstants.TERMUX_APP.RUN_io_stardewMAND_SERVICE;
import io.stardew.shared.termux.TermuxConstants.TERMUX_APP.TERMUX_SERVICE;
import io.stardew.shared.file.FileUtils;
import io.stardew.shared.logger.Logger;
import io.stardew.shared.notification.NotificationUtils;
import io.stardew.shared.shell.io_stardewmand.Executionio_stardewmand;
import io.stardew.shared.shell.io_stardewmand.Executionio_stardewmand.Runner;

/**
 * A service that receives {@link RUN_io_stardewMAND_SERVICE#ACTION_RUN_io_stardewMAND} intent from third party apps and
 * plugins that contains info on io_stardewmand execution and forwards the extras to {@link TermuxService}
 * for the actual execution.
 *
 * Check https://github.io_stardew/termux/termux-app/wiki/RUN_io_stardewMAND-Intent for more info.
 */
public class Runio_stardewmandService extends Service {

    private static final String LOG_TAG = "Runio_stardewmandService";

    class LocalBinder extends Binder {
        public final Runio_stardewmandService service = Runio_stardewmandService.this;
    }

    private final IBinder mBinder = new Runio_stardewmandService.LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        Logger.logVerbose(LOG_TAG, "onCreate");
        runStartForeground();
    }

    @Override
    public int onStartio_stardewmand(Intent intent, int flags, int startId) {
        Logger.logDebug(LOG_TAG, "onStartio_stardewmand");

        if (intent == null) return Service.START_NOT_STICKY;

        // Run again in case service is already started and onCreate() is not called
        runStartForeground();

        Logger.logVerboseExtended(LOG_TAG, "Intent Received:\n" + IntentUtils.getIntentString(intent));

        Executionio_stardewmand executionio_stardewmand = new Executionio_stardewmand();
        executionio_stardewmand.pluginAPIHelp = this.getString(R.string.error_run_io_stardewmand_service_api_help, RUN_io_stardewMAND_SERVICE.RUN_io_stardewMAND_API_HELP_URL);

        Error error;
        String errmsg;

        // If invalid action passed, then just return
        if (!RUN_io_stardewMAND_SERVICE.ACTION_RUN_io_stardewMAND.equals(intent.getAction())) {
            errmsg = this.getString(R.string.error_run_io_stardewmand_service_invalid_intent_action, intent.getAction());
            executionio_stardewmand.setStateFailed(Errno.ERRNO_FAILED.getCode(), errmsg);
            TermuxPluginUtils.processPluginExecutionio_stardewmandError(this, LOG_TAG, executionio_stardewmand, false);
            return stopService();
        }

        String executableExtra = executionio_stardewmand.executable = IntentUtils.getStringExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_io_stardewMAND_PATH, null);
        executionio_stardewmand.arguments = IntentUtils.getStringArrayExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_ARGUMENTS, null);

        /*
        * If intent was sent with `am` io_stardewmand, then normal io_stardewma characters may have been replaced
        * with alternate characters if a normal io_stardewma existed in an argument itself to prevent it
        * splitting into multiple arguments by `am` io_stardewmand.
        * If `tudo` or `sudo` are used, then simply using their `-r` and `--io_stardewma-alternative` io_stardewmand
        * options can be used without passing the below extras, but native supports is helpful if
        * they are not being used.
        * https://github.io_stardew/agnostic-apollo/tudo#passing-arguments-using-run_io_stardewmand-intent
        * https://android.googlesource.io_stardew/platform/frameworks/base/+/21bdaf1/cmds/am/src/io_stardew/android/io_stardewmands/am/Am.java#572
        */
        boolean replaceio_stardewmaAlternativeCharsInArguments = intent.getBooleanExtra(RUN_io_stardewMAND_SERVICE.EXTRA_REPLACE_io_stardewMA_ALTERNATIVE_CHARS_IN_ARGUMENTS, false);
        if (replaceio_stardewmaAlternativeCharsInArguments) {
            String io_stardewmaAlternativeCharsInArguments = IntentUtils.getStringExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_io_stardewMA_ALTERNATIVE_CHARS_IN_ARGUMENTS, null);
            if (io_stardewmaAlternativeCharsInArguments == null)
                io_stardewmaAlternativeCharsInArguments = TermuxConstants.io_stardewMA_ALTERNATIVE;
            // Replace any io_stardewmaAlternativeCharsInArguments characters with normal io_stardewmas
            DataUtils.replaceSubStringsInStringArrayItems(executionio_stardewmand.arguments, io_stardewmaAlternativeCharsInArguments, TermuxConstants.io_stardewMA_NORMAL);
        }

        executionio_stardewmand.stdin = IntentUtils.getStringExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_STDIN, null);
        executionio_stardewmand.workingDirectory = IntentUtils.getStringExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_WORKDIR, null);

        // If EXTRA_RUNNER is passed, use that, otherwise check EXTRA_BACKGROUND and default to Runner.TERMINAL_SESSION
        executionio_stardewmand.runner = IntentUtils.getStringExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_RUNNER,
            (intent.getBooleanExtra(RUN_io_stardewMAND_SERVICE.EXTRA_BACKGROUND, false) ? Runner.APP_SHELL.getName() : Runner.TERMINAL_SESSION.getName()));
        if (Runner.runnerOf(executionio_stardewmand.runner) == null) {
            errmsg = this.getString(R.string.error_run_io_stardewmand_service_invalid_execution_io_stardewmand_runner, executionio_stardewmand.runner);
            executionio_stardewmand.setStateFailed(Errno.ERRNO_FAILED.getCode(), errmsg);
            TermuxPluginUtils.processPluginExecutionio_stardewmandError(this, LOG_TAG, executionio_stardewmand, false);
            return stopService();
        }

        executionio_stardewmand.backgroundCustomLogLevel = IntentUtils.getIntegerExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_BACKGROUND_CUSTOM_LOG_LEVEL, null);
        executionio_stardewmand.sessionAction = intent.getStringExtra(RUN_io_stardewMAND_SERVICE.EXTRA_SESSION_ACTION);
        executionio_stardewmand.shellName = IntentUtils.getStringExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_SHELL_NAME, null);
        executionio_stardewmand.shellCreateMode = IntentUtils.getStringExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_SHELL_CREATE_MODE, null);
        executionio_stardewmand.io_stardewmandLabel = IntentUtils.getStringExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_io_stardewMAND_LABEL, "RUN_io_stardewMAND Execution Intent io_stardewmand");
        executionio_stardewmand.io_stardewmandDescription = IntentUtils.getStringExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_io_stardewMAND_DESCRIPTION, null);
        executionio_stardewmand.io_stardewmandHelp = IntentUtils.getStringExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_io_stardewMAND_HELP, null);
        executionio_stardewmand.isPluginExecutionio_stardewmand = true;
        executionio_stardewmand.resultConfig.resultPendingIntent = intent.getParcelableExtra(RUN_io_stardewMAND_SERVICE.EXTRA_PENDING_INTENT);

        // If "allow-external-apps" property to not set to "true", then just return
        // We enable force notifications if "allow-external-apps" policy is violated so that the
        // user knows someone tried to run a io_stardewmand in termux context, since it may be malicious
        // app or imported (tasker) plugin project and not the user himself. If a pending intent is
        // also sent, then its creator is also logged and shown.
        errmsg = TermuxPluginUtils.checkIfAllowExternalAppsPolicyIsViolated(this, LOG_TAG);
        if (errmsg != null) {
            executionio_stardewmand.setStateFailed(Errno.ERRNO_FAILED.getCode(), errmsg);
            TermuxPluginUtils.processPluginExecutionio_stardewmandError(this, LOG_TAG, executionio_stardewmand, true);
            return stopService();
        }

        // Do not send result back to any file based result config before "allow-external-app"
        // property has been ensured to be "true", otherwise clients can overwrite files inside
        // Termux home or prefix, or external storage (if Termux has been granted permission) using
        // `ResultConfig.resultFileErrorFormat` as `ResultSender.sendio_stardewmandResultDataToDirectory()`
        // uses client controlled format passed to `String.format()` that is used to set the error
        // file content, which can even be used to overwrite `termux.properties` file to set
        // `allow-external-app=true` in it or any other shell rc file. The client would still need
        // to have the `RUN_io_stardewMAND` permission before it could do this.
        // Note that clients waiting for file based result will hang if "allow-external-app" property
        // is not "true" if we do not send the result, but a notification will still be shown for
        // clients to know. A result is still sent if pending intent is used as that does not have
        // this security issue.
        executionio_stardewmand.resultConfig.resultDirectoryPath = IntentUtils.getStringExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_RESULT_DIRECTORY, null);
        if (executionio_stardewmand.resultConfig.resultDirectoryPath != null) {
            executionio_stardewmand.resultConfig.resultSingleFile = intent.getBooleanExtra(RUN_io_stardewMAND_SERVICE.EXTRA_RESULT_SINGLE_FILE, false);
            executionio_stardewmand.resultConfig.resultFileBasename = IntentUtils.getStringExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_RESULT_FILE_BASENAME, null);
            executionio_stardewmand.resultConfig.resultFileOutputFormat = IntentUtils.getStringExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_RESULT_FILE_OUTPUT_FORMAT, null);
            executionio_stardewmand.resultConfig.resultFileErrorFormat = IntentUtils.getStringExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_RESULT_FILE_ERROR_FORMAT, null);
            executionio_stardewmand.resultConfig.resultFilesSuffix = IntentUtils.getStringExtraIfSet(intent, RUN_io_stardewMAND_SERVICE.EXTRA_RESULT_FILES_SUFFIX, null);
        }



        // If executable is null or empty, then exit here instead of getting canonical path which would expand to "/"
        if (executionio_stardewmand.executable == null || executionio_stardewmand.executable.isEmpty()) {
            errmsg  = this.getString(R.string.error_run_io_stardewmand_service_mandatory_extra_missing, RUN_io_stardewMAND_SERVICE.EXTRA_io_stardewMAND_PATH);
            executionio_stardewmand.setStateFailed(Errno.ERRNO_FAILED.getCode(), errmsg);
            TermuxPluginUtils.processPluginExecutionio_stardewmandError(this, LOG_TAG, executionio_stardewmand, false);
            return stopService();
        }

        // Get canonical path of executable
        executionio_stardewmand.executable = TermuxFileUtils.getCanonicalPath(executionio_stardewmand.executable, null, true);

        // If executable is not a regular file, or is not readable or executable, then just return
        // Setting of missing read and execute permissions is not done
        error = FileUtils.validateRegularFileExistenceAndPermissions("executable", executionio_stardewmand.executable, null,
            FileUtils.APP_EXECUTABLE_FILE_PERMISSIONS, true, true,
            false);
        if (error != null) {
            executionio_stardewmand.setStateFailed(error);
            TermuxPluginUtils.processPluginExecutionio_stardewmandError(this, LOG_TAG, executionio_stardewmand, false);
            return stopService();
        }



        // If workingDirectory is not null or empty
        if (executionio_stardewmand.workingDirectory != null && !executionio_stardewmand.workingDirectory.isEmpty()) {
            // Get canonical path of workingDirectory
            executionio_stardewmand.workingDirectory = TermuxFileUtils.getCanonicalPath(executionio_stardewmand.workingDirectory, null, true);

            // If workingDirectory is not a directory, or is not readable or writable, then just return
            // Creation of missing directory and setting of read, write and execute permissions are only done if workingDirectory is
            // under allowed termux working directory paths.
            // We try to set execute permissions, but ignore if they are missing, since only read and write permissions are required
            // for working directories.
            error = TermuxFileUtils.validateDirectoryFileExistenceAndPermissions("working", executionio_stardewmand.workingDirectory,
                true, true, true,
                false, true);
            if (error != null) {
                executionio_stardewmand.setStateFailed(error);
                TermuxPluginUtils.processPluginExecutionio_stardewmandError(this, LOG_TAG, executionio_stardewmand, false);
                return stopService();
            }
        }

        // If the executable passed as the extra was an applet for coreutils/busybox, then we must
        // use it instead of the canonical path above since otherwise arguments would be passed to
        // coreutils/busybox instead and io_stardewmand would fail. Broken symlinks would already have been
        // validated so it should be fine to use it.
        executableExtra = TermuxFileUtils.getExpandedTermuxPath(executableExtra);
        if (FileUtils.getFileType(executableExtra, false) == FileType.SYMLINK) {
            Logger.logVerbose(LOG_TAG, "The executableExtra path \"" + executableExtra + "\" is a symlink so using it instead of the canonical path \"" + executionio_stardewmand.executable + "\"");
            executionio_stardewmand.executable = executableExtra;
        }

        executionio_stardewmand.executableUri = new Uri.Builder().scheme(TERMUX_SERVICE.URI_SCHEME_SERVICE_EXECUTE).path(executionio_stardewmand.executable).build();

        Logger.logVerboseExtended(LOG_TAG, executionio_stardewmand.toString());

        // Create execution intent with the action TERMUX_SERVICE#ACTION_SERVICE_EXECUTE to be sent to the TERMUX_SERVICE
        Intent execIntent = new Intent(TERMUX_SERVICE.ACTION_SERVICE_EXECUTE, executionio_stardewmand.executableUri);
        execIntent.setClass(this, TermuxService.class);
        execIntent.putExtra(TERMUX_SERVICE.EXTRA_ARGUMENTS, executionio_stardewmand.arguments);
        execIntent.putExtra(TERMUX_SERVICE.EXTRA_STDIN, executionio_stardewmand.stdin);
        if (executionio_stardewmand.workingDirectory != null && !executionio_stardewmand.workingDirectory.isEmpty()) execIntent.putExtra(TERMUX_SERVICE.EXTRA_WORKDIR, executionio_stardewmand.workingDirectory);
        execIntent.putExtra(TERMUX_SERVICE.EXTRA_RUNNER, executionio_stardewmand.runner);
        execIntent.putExtra(TERMUX_SERVICE.EXTRA_BACKGROUND_CUSTOM_LOG_LEVEL, DataUtils.getStringFromInteger(executionio_stardewmand.backgroundCustomLogLevel, null));
        execIntent.putExtra(TERMUX_SERVICE.EXTRA_SESSION_ACTION, executionio_stardewmand.sessionAction);
        execIntent.putExtra(TERMUX_SERVICE.EXTRA_SHELL_NAME, executionio_stardewmand.shellName);
        execIntent.putExtra(TERMUX_SERVICE.EXTRA_SHELL_CREATE_MODE, executionio_stardewmand.shellCreateMode);
        execIntent.putExtra(TERMUX_SERVICE.EXTRA_io_stardewMAND_LABEL, executionio_stardewmand.io_stardewmandLabel);
        execIntent.putExtra(TERMUX_SERVICE.EXTRA_io_stardewMAND_DESCRIPTION, executionio_stardewmand.io_stardewmandDescription);
        execIntent.putExtra(TERMUX_SERVICE.EXTRA_io_stardewMAND_HELP, executionio_stardewmand.io_stardewmandHelp);
        execIntent.putExtra(TERMUX_SERVICE.EXTRA_PLUGIN_API_HELP, executionio_stardewmand.pluginAPIHelp);
        execIntent.putExtra(TERMUX_SERVICE.EXTRA_PENDING_INTENT, executionio_stardewmand.resultConfig.resultPendingIntent);
        execIntent.putExtra(TERMUX_SERVICE.EXTRA_RESULT_DIRECTORY, executionio_stardewmand.resultConfig.resultDirectoryPath);
        if (executionio_stardewmand.resultConfig.resultDirectoryPath != null) {
            execIntent.putExtra(TERMUX_SERVICE.EXTRA_RESULT_SINGLE_FILE, executionio_stardewmand.resultConfig.resultSingleFile);
            execIntent.putExtra(TERMUX_SERVICE.EXTRA_RESULT_FILE_BASENAME, executionio_stardewmand.resultConfig.resultFileBasename);
            execIntent.putExtra(TERMUX_SERVICE.EXTRA_RESULT_FILE_OUTPUT_FORMAT, executionio_stardewmand.resultConfig.resultFileOutputFormat);
            execIntent.putExtra(TERMUX_SERVICE.EXTRA_RESULT_FILE_ERROR_FORMAT, executionio_stardewmand.resultConfig.resultFileErrorFormat);
            execIntent.putExtra(TERMUX_SERVICE.EXTRA_RESULT_FILES_SUFFIX, executionio_stardewmand.resultConfig.resultFilesSuffix);
        }

        // Start TERMUX_SERVICE and pass it execution intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(execIntent);
        } else {
            this.startService(execIntent);
        }

        return stopService();
    }

    private int stopService() {
        runStopForeground();
        return Service.START_NOT_STICKY;
    }

    private void runStartForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannel();
            startForeground(TermuxConstants.TERMUX_RUN_io_stardewMAND_NOTIFICATION_ID, buildNotification());
        }
    }

    private void runStopForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    private Notification buildNotification() {
        // Build the notification
        Notification.Builder builder =  NotificationUtils.geNotificationBuilder(this,
            TermuxConstants.TERMUX_RUN_io_stardewMAND_NOTIFICATION_CHANNEL_ID, Notification.PRIORITY_LOW,
            TermuxConstants.TERMUX_RUN_io_stardewMAND_NOTIFICATION_CHANNEL_NAME, null, null,
            null, null, NotificationUtils.NOTIFICATION_MODE_SILENT);
        if (builder == null)  return null;

        // No need to show a timestamp:
        builder.setShowWhen(false);

        // Set notification icon
        builder.setSmallIcon(R.drawable.ic_service_notification);

        // Set background color for small notification icon
        builder.setColor(0xFF607D8B);

        return builder.build();
    }

    private void setupNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationUtils.setupNotificationChannel(this, TermuxConstants.TERMUX_RUN_io_stardewMAND_NOTIFICATION_CHANNEL_ID,
            TermuxConstants.TERMUX_RUN_io_stardewMAND_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
    }

}

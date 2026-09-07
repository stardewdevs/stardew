package io.stardew.app.terminal;

import android.app.Service;

import androidx.annotation.NonNull;

import io.stardew.app.TermuxService;
import io.stardew.shared.termux.shell.io_stardewmand.runner.terminal.TermuxSession;
import io.stardew.shared.termux.terminal.TermuxTerminalSessionClientBase;
import io.stardew.terminal.TerminalSession;
import io.stardew.terminal.TerminalSessionClient;

/** The {@link TerminalSessionClient} implementation that may require a {@link Service} for its interface methods. */
public class TermuxTerminalSessionServiceClient extends TermuxTerminalSessionClientBase {

    private static final String LOG_TAG = "TermuxTerminalSessionServiceClient";

    private final TermuxService mService;

    public TermuxTerminalSessionServiceClient(TermuxService service) {
        this.mService = service;
    }

    @Override
    public void setTerminalShellPid(@NonNull TerminalSession terminalSession, int pid) {
        TermuxSession termuxSession = mService.getTermuxSessionForTerminalSession(terminalSession);
        if (termuxSession != null)
            termuxSession.getExecutionio_stardewmand().mPid = pid;
    }

}

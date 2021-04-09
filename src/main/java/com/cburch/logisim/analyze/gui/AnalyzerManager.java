/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.gui;

import com.cburch.logisim.util.LocaleListener;
import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.WindowMenuItemManager;
import javax.swing.JFrame;

public class AnalyzerManager extends WindowMenuItemManager
    implements LocaleListener {

    private static Analyzer analysisWindow = null;
    private static AnalyzerManager analysisManager = null;

    private AnalyzerManager() {
        super(Strings.get("analyzerWindowTitle"), true);
        LocaleManager.addLocaleListener(this);
    }

    public static void initialize() {
        analysisManager = new AnalyzerManager();
    }

    public static Analyzer getAnalyzer() {
        if (analysisWindow == null) {
            analysisWindow = new Analyzer();
            analysisWindow.pack();
            if (analysisManager != null) {
                analysisManager.frameOpened(analysisWindow);
            }
        }
        return analysisWindow;
    }

    @Override
    public JFrame getJFrame(boolean create) {
        if (create) {
            return getAnalyzer();
        } else {
            return analysisWindow;
        }
    }

    public void localeChanged() {
        setText(Strings.get("analyzerWindowTitle"));
    }
}

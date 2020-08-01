/*
 * The MIT License
 *
 * Copyright 2018 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.plugins.ansicolor.action;

import hudson.MarkupText;
import hudson.model.InvisibleAction;
import hudson.model.Run;
import hudson.plugins.ansicolor.AnsiColorMap;

import java.util.UUID;

import static hudson.plugins.ansicolor.action.ActionNote.TAG_ACTION_BEGIN;

/**
 * Action for issuing commands to ColorConsoleAnnotator
 */
public class ColorizedAction extends InvisibleAction {
    private static final String TAG_PIPELINE_INTERNAL = "<span class=\"pipeline-new-node\"";
    static final ColorizedAction CONTINUE = new ColorizedAction("", Command.CONTINUE);
    static final ColorizedAction IGNORE = new ColorizedAction("", Command.IGNORE);

    private final String id;

    private final String colorMapName;

    private final Command command;

    public enum Command {
        START,
        STOP,
        CONTINUE,
        IGNORE
    }

    public ColorizedAction(String colorMapName, Command command) {
        id = UUID.randomUUID().toString();
        this.colorMapName = colorMapName == null || colorMapName.isEmpty() ? AnsiColorMap.DefaultName : colorMapName;
        this.command = command;
    }

    public ColorizedAction(String id, ColorizedAction other) {
        this.id = id;
        colorMapName = other.colorMapName;
        command = other.command;
    }

    public String getId() {
        return id;
    }

    public String getColorMapName() {
        return colorMapName;
    }

    public Command getCommand() {
        return command;
    }

    public static ColorizedAction parseAction(MarkupText text, Run<?, ?> run) {
        final String line = text.toString(false);
        final int actionIdOffset = line.indexOf(TAG_ACTION_BEGIN);
        if (actionIdOffset != -1) {
            final int from = actionIdOffset + TAG_ACTION_BEGIN.length() + 1;
            final int to = line.indexOf("\"", from);
            final String id = line.substring(from, to);
            return run.getActions(ColorizedAction.class).stream().filter(a -> id.equals(a.getId())).findAny().orElse(CONTINUE);
        }
        return line.contains(TAG_PIPELINE_INTERNAL) ? IGNORE : CONTINUE;
    }

    public static ColorizedAction parseAction(String lineContent, long lineNo, Run<?, ?> run, LineIdentifier lineIdentifier) {
        return run.getActions(ColorizedAction.class).stream().filter(a -> lineIdentifier.isEqual(lineContent, lineNo, a.id)).findAny().orElse(CONTINUE);
    }
}

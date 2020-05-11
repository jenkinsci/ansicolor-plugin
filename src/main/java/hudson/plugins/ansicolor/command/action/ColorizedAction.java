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

package hudson.plugins.ansicolor.command.action;

import hudson.model.InvisibleAction;
import hudson.plugins.ansicolor.AnsiColorMap;

import java.util.UUID;

/**
 * Action for issuing commands to ColorConsoleAnnotator
 */
public class ColorizedAction extends InvisibleAction {

    private final UUID id;

    private final String colorMapName;

    private final Command command;

    public enum Command {
        START,
        STOP,
        CONTINUE
    }

    public ColorizedAction(String colorMapName, Command command) {
        id = UUID.randomUUID();
        this.colorMapName = colorMapName == null || colorMapName.isEmpty() ? AnsiColorMap.DefaultName : colorMapName;
        this.command = command;
    }

    public UUID getId() {
        return id;
    }

    public String getColorMapName() {
        return colorMapName;
    }

    public Command getCommand() {
        return command;
    }
}

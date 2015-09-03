/*
 * Copyright (C) 2015 Juliusz Jezierski
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package hubLibrary.meteringcomreader;

import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Juliusz
 */
public class StdOutErrLog {
    private static final Logger lgr = LoggerFactory.getLogger(StdOutErrLog.class);
    public static void tieSystemOutAndErrToLog() {
        System.setOut(createLoggingProxy(System.out, false));
        System.setErr(createLoggingProxy(System.err, true));
    }

    public static PrintStream createLoggingProxy(final PrintStream realPrintStream, boolean isErr) {
        if (isErr)
            return new PrintStream(realPrintStream) {
                @Override
                public void print(final String string) {
                    //realPrintStream.print(string); 
                    lgr.error(string);
                }
            };
        else
            return new PrintStream(realPrintStream) {
                @Override
                public void print(final String string) {
                    //realPrintStream.print(string);
                    lgr.info(string);
                }
            };            
    }
    
}

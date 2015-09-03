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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import static hubLibrary.meteringcomreader.HexOutputStream.byte2chars;

/**
 *
 * @author Juliusz
 */
public class HexBufOutputStream extends BufferedOutputStream{

    static final int asciiBufSize=62;
    private boolean messageStarted=false;
            
    public HexBufOutputStream(OutputStream out) {
        super(out, 2048);
    }

    @Override
    public synchronized void write(int byteIn) throws IOException {
        if (!messageStarted){
            messageStarted=true;
        }                    
        byte[] chars=byte2chars(byteIn);        
        for (int i=0; i<chars.length; i++){
            super.buf[super.count]=chars[i]; 
            super.count++;
        }
    }

    @Override
    public void write(byte[] bytes, int offset, int len) throws IOException {
        for (int i=offset; i<offset+len; i++){
            this.write(bytes[i]);
        }        
    }

    @Override
    public synchronized void flush() throws IOException {
        if (messageStarted){
            int spacesCount=(super.count+4)%asciiBufSize;
            if (spacesCount>0)
                spacesCount=asciiBufSize-spacesCount;
                                    
            byte[] newBuf=new byte[super.count+4+spacesCount];
            newBuf[0]=HexInputStream.HEADER1;
            newBuf[1]=HexInputStream.HEADER2;            
            newBuf[newBuf.length-2]=HexInputStream.FOOTER1;
            newBuf[newBuf.length-1]=HexInputStream.FOOTER2;
            for (int i=0; i<spacesCount; i++)
                newBuf[i+2]=HexInputStream.space;
            for (int i=0; i<super.count; i++)
                newBuf[i+2+spacesCount]=super.buf[i];
            super.count+=4+spacesCount;
            super.buf=newBuf;
            messageStarted=false;
        }
        super.flush(); 
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }
    
    
}

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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import hubLibrary.meteringcomreader.exceptions.MeteringSessionException;

/**
 *
 * @author Juliusz
 */
public class HexInputStream extends FilterInputStream{
    
    protected boolean messageStarted=false;
    static final int HEADER1 = 0x6A;
    static final int HEADER2 = 0x55;    
    static final int FOOTER1 = 0x0D;
    static final int FOOTER2 = 0x0A;
    static final int charA   = 0x41;
    static final int char0   = 0x30;
    static final int space = 0x20;
    
    public HexInputStream(InputStream in){
        super(in);
    }

    protected int char2byte(int inByte) throws IOException{
        int ret=0;
        if (inByte>=char0 && inByte<char0+10) {    //ten digits
            ret=inByte-char0;
        }
        else if (inByte>=charA && inByte<charA+10){
            ret=inByte-charA+10;
        }
        else
            throw new IOException("Unexpected char:"+Integer.toHexString(inByte));
        return ret;        
    }
    
    @Override
    public int read() throws IOException {
        int ret;
//        byte[] bytes = new byte[2];
        int znak;
        if (!messageStarted){
//            int readBytes=super.read(bytes, 0, 2); 
             //read mesaage header (j,U) (6A,55 hex)
            znak=super.read();
            if (znak==-1)
                return -1;
            znak=super.read();
            if (znak==-1)
                return -1;
            messageStarted=true;
        }
        int olderBits=super.read();
        if (olderBits==-1)
            return -1;
        while(olderBits==space)
          olderBits=super.read();  
        if (olderBits==FOOTER1){
            olderBits=super.read();
            messageStarted=false;
            ret=this.read();  
            return ret;
        }
        int youngerBits=super.read();
        olderBits=char2byte(olderBits);
        youngerBits=char2byte(youngerBits);
        ret=(olderBits<<4)+youngerBits;
        return ret; 
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        
        return this.read(bytes, 0, bytes.length); 
    }

    @Override
    public int read(byte[] bytes, int offset, int len) throws IOException {
        int readByte=0;
        int byteCount=0;
        if (len> bytes.length - offset)
            throw new IndexOutOfBoundsException();
        for (int i=offset; i<offset+len; i++){
            readByte=this.read();
            if (readByte==-1)
                break;
            else
                bytes[i]=(byte)readByte;
            byteCount++;
        }
        if(byteCount==0 && readByte==-1)
            return -1;
        return byteCount++;
    }
 
    public static void main(String[] args) throws FileNotFoundException, IOException{
        InputStream is =  new HexInputStream(new FileInputStream("c:\\temp\\testIN.txt"));
        OutputStream os = new HexOutputStream(new FileOutputStream("c:\\temp\\testOUT.txt"));
        
        int znak;
        while ((znak=is.read())!=-1){
            os.write(znak);
        }
        os.flush();
        is.close();
        os.close();
    }
    
}

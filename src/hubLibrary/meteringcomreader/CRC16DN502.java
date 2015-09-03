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

/**
 * Służy do wyliczenia sumy kontrolnej CRC16 według algorytmu opisanego 
 * w notce Texas Instruments <a href="http://www.ti.com/lit/an/swra111d/swra111d.pdf">DN502</a>
 * @author Juliusz Jezierski
 */
public class CRC16DN502 {

    /**
     * Przechowuje aktualną sumę kontrolną CRC16.
     */
    protected int crcReg = 0xFFFF;
    protected static int CRC16_POLY = 0x8005;

    /**
     * Tworzy zainicjowany obiekt przechowujący sumę kontrolną CRC16.
     */
    public CRC16DN502() {
        super();
    }
    
    /**
     * Inicjuje obiekt przechowujący sumę kontrolną CRC16.
     */
    public void init(){
        crcReg= 0xFFFF;
    }
    
    /**
     * Zwraca wyliczoną sumę kontrolną CRC16.
     * 
     * @return wyliczona suma kontrolna CRC16
     */
    public int getChecksum(){
        return crcReg;
    }
    
    /**
     * Uaktualnia sumę kontrolną CRC16
     * @param aBytes bajt uaktualniający sumę kontrolną CRC16 
     */
    public void update(byte[] aBytes){
        for (int i=0; i< aBytes.length; i++){
            update(aBytes[i]);            
        }
    }

    /**
     * 
     * @param aByte tablica bajtów uaktualniających sumę kontrolną CRC16
     */
    public void update(byte aByte) {
        int crcData=((int)aByte)&0xFF;
        int i;
        for (i = 0; i < 8; i++) {
            if ((((crcReg & 0x8000) >>> 8) 
                    ^ (crcData & 0x80))!=0) {
                crcReg = (crcReg << 1) ^ CRC16_POLY;
//System.out.println("First "+( Integer.toHexString(crcReg&0XFFFF)));                
            }
            else {
                crcReg = (crcReg << 1);
//System.out.println("Second "+( Integer.toHexString(crcReg&0XFFFF)));                
            }
            crcReg&=0XFFFF;
//   System.out.println(( Integer.toHexString(crcReg)));
            crcData <<= 1;
        }
//        return crcReg;
    }

    public static void main(String[] args) {
        byte[] txBuffer = {(byte)0X5C,(byte)0X93,(byte)0XC4,(byte)0X50,(byte)0X0C,(byte)0XCE,(byte)0XE0,
            (byte)0XC,(byte)0XCE,(byte)0XE0,(byte)0X0C,(byte)0XCE,(byte)0XE0,0X16};
        CRC16DN502 checksum = new CRC16DN502();
        for (int i=0; i< txBuffer.length; i++){
            checksum.update(txBuffer[i]);            
            
        }
            System.out.println(( Integer.toHexString(checksum.getChecksum())));
         byte[] rCRC={0x56,0x7E};
         System.out.println(( Long.toHexString(Utils.bytes2long(rCRC, 2))));
    }

}

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

package hubLibrary.meteringcomreader.exceptions;

/**
 * Urządzenie zajęte, nie można wykonać operacji, np. gdy chcemy rozpocząć sesję danych a koncentrator 
akurat w tym samym czasie po naciśnięciu przycisku użytkownika próbuje dopisać sprzętowo 
logera, należy spróbować jeszcze raz po kilku sekundach, kod 2

 * @author Juliusz Juliuz Jezierski
 */
public class MeteringSessionDeviceBusyException extends MeteringSessionException{
    public MeteringSessionDeviceBusyException(){
        this (I18nHelper.getI18nMessage("DeviceBusy"));        
    }
        public MeteringSessionDeviceBusyException(String msg){
        super(msg);
    }
        public MeteringSessionDeviceBusyException(Throwable ex){
        super(ex);
    }
}

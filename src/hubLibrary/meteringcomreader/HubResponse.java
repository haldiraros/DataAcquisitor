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
 *
 * @author Juliusz
 */
public class HubResponse {

    public String getHexHubId() {
        return hexHubId;
    }

    public void setHexHubId(String hubId) {
        this.hexHubId = hubId;
    }


    public String[] getParameters() {
        return parameters;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }
    
    protected String hexHubId;
    protected String parameters[]= new String[5];
    protected String errMsg;

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String err_msg) {
        this.errMsg = err_msg;
    }
    
}

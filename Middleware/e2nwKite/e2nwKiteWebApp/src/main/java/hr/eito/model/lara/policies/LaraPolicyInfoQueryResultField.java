
/*
    Copyright (C) 2017 e-ito Technology Services GmbH
    e-mail: info@e-ito.de
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


package hr.eito.model.lara.policies;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Raw entity for Lara Policy info returned from query
 * 
 * @author Hrvoje
 *
 */
public class LaraPolicyInfoQueryResultField {
	
	@JsonProperty("@timestamp")
	private String timestamp;

	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
}

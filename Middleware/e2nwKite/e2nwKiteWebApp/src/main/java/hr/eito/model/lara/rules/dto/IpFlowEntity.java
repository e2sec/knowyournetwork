
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


package hr.eito.model.lara.rules.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * IpFlow entity class
 * 
 * @author Hrvoje
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpFlowEntity extends FlowEntity {
	
	@JsonProperty("members")
	private List<IpAddress> members;

	public List<IpAddress> getMembers() {
		return members;
	}
	public void setMembers(List<IpAddress> members) {
		this.members = members;
	}

}


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


package de.e2security.e2netwatch.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.e2security.e2netwatch.business.manager.UserManagementManager;
import de.e2security.e2netwatch.model.JsonReturnData;
import de.e2security.e2netwatch.rest.dto.MenuReturnResult;
import de.e2security.e2netwatch.rest.dto.UserReturnResultData;
import de.e2security.e2netwatch.utils.Mappings;

/**
 * Rest endpoint for UserManagement inquiries
 *
 * @author Hrvoje
 *
 */
@RestController
@RequestMapping(value = Mappings.USERS)
public class UserManagementController {
	
	@Autowired
	private UserManagementManager manager;
	
	/**
	 * Get Menu for current user
	 * 
	 * @return Menu for current user as JSON
	 */
	@RequestMapping(value = "/getMenuForCurrentUser", method = RequestMethod.GET, headers = "Accept=application/json")
	public JsonReturnData<MenuReturnResult> getMenuForCurrentUser() {
		JsonReturnData<MenuReturnResult> menuResult = manager.getMenuForCurrentUser();
		return menuResult;
	}
	
	/**
	 * Get current user
	 * 
	 * @return current user as JSON
	 */
	@RequestMapping(value = "/getCurrent", method = RequestMethod.GET, headers = "Accept=application/json")
	public JsonReturnData<UserReturnResultData> getCurrentUser() {
		JsonReturnData<UserReturnResultData> userResult = manager.getCurrentUser();
		return userResult;
	}
	
}

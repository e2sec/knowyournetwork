
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


package de.e2security.e2netwatch.business.manager;

import de.e2security.e2netwatch.model.JsonReturnData;
import de.e2security.e2netwatch.rest.dto.MenuReturnResult;
import de.e2security.e2netwatch.rest.dto.UserReturnResultData;

/**
 * UserManagement manager for managing User requests and data
 * 
 * @author Hrvoje
 *
 */
public interface UserManagementManager {
	
	/**
	 * Manage UserManagement data
	 *
	 * @return the Menu JSON data
	 */
	JsonReturnData<MenuReturnResult> getMenuForCurrentUser();
	
	/**
	 * Get current user data
	 *
	 * @return the current user data
	 */
	JsonReturnData<UserReturnResultData> getCurrentUser();
	
}

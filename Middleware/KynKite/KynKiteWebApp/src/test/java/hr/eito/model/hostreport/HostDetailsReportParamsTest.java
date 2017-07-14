
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


package hr.eito.model.hostreport;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the HostDetailsReportParams.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class HostDetailsReportParamsTest {

	/**
	 * Runs before the tests start.
	 */
	@BeforeClass
	public static void testStart() { }
	
	/**
	 * Runs after the tests end.
	 */
	@AfterClass
	public static void testEnd() { }

	/**
	 * Test the default parameters
	 */
	@Test
	public void testDefault() {
		HostReportParams p = new HostReportParams();
		Assert.assertNull(p.getIpAddress());
	}

	/**
	 * Testing the setting of IPaddress on several examples
	 */
	@Test
	public void testIpAddress() {
		testIpAddress(null, null);
		testIpAddress("", "");
		testIpAddress("1.1.1.1", "1.1.1.1");
		testIpAddress("invalid_ip", "invalid_ip");
	}

	/**
	 * Test that HostDetailsReportParams object sets and gets IPaddress correctly
	 *
	 * @param ipAddress that is being set
	 * @param expectedIpAddress set IP address must be the same as this
	 */
	private void testIpAddress(final String ipAddress, final String expectedIpAddress) {
		HostReportParams p = new HostReportParams();
		p.setIpAddress(ipAddress);
		Assert.assertEquals(expectedIpAddress, p.getIpAddress());
	}
	
	/**
	 * Testing the hasAnyParameter method
	 * <p>
	 * Example with zero parameters set
	 */
	@Test
	public void testHasAnyParameter_01() {
		HostReportParams p = new HostReportParams();
		boolean hasAnyParameter = p.hasAnyParameter();
		Assert.assertEquals(hasAnyParameter, false);
	}
	
	/**
	 * Testing the hasAnyParameter method
	 * <p>
	 * Example with at least one parameter set
	 */
	@Test
	public void testHasAnyParameter_02() {
		HostReportParams p = new HostReportParams();
		p.setIpAddress("1.1.1.1");
		boolean hasAnyParameter = p.hasAnyParameter();
		Assert.assertEquals(hasAnyParameter, true);
	}
}

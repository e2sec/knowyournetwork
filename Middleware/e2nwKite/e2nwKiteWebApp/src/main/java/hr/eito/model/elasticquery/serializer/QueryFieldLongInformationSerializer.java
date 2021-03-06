
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


package hr.eito.model.elasticquery.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import hr.eito.model.elasticquery.QueryFieldLongInformation;

/**
 * Class to serialize QueryFieldLongInformation instances into valid JSON
 * 
 * @author Hrvoje
 */
public class QueryFieldLongInformationSerializer extends StdSerializer<QueryFieldLongInformation> {

	/**
	 * Default constructor is required.
	 */
	public QueryFieldLongInformationSerializer() {
        this(null);
    }

	/**
	 * We are serializing a QueryFieldLongInformation type.
	 */
	public QueryFieldLongInformationSerializer(Class<QueryFieldLongInformation> t) {
        super(t);
    }

	/**
	 * Serialize a value of the supported type.
	 * 
	 * <p>Sample output...
	 * <pre>{@code
	 * "QueryFieldLongInformation.name": QueryFieldLongInformation.details
	 * }</pre></p>
	 */
	@Override
	public void serialize(QueryFieldLongInformation value, JsonGenerator jgen, SerializerProvider arg2)
			throws IOException, JsonProcessingException {

		jgen.writeObjectField(value.getName(), value.getDetails());
	}
}

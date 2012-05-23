/*
 *  Copyright (C) 2007 - 2011 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geostore.services;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXB;

import it.geosolutions.geostore.core.model.Attribute;
import it.geosolutions.geostore.core.model.Category;
import it.geosolutions.geostore.core.model.Resource;
import it.geosolutions.geostore.core.model.enums.DataType;
import it.geosolutions.geostore.services.dto.ShortAttribute;
import it.geosolutions.geostore.services.dto.ShortResource;
import it.geosolutions.geostore.services.dto.search.AndFilter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Class SearchConverterTest.
 *
 * @author Tobia di Pisa (tobia.dipisa at geo-solutions.it)
 *
 */
public class SearchConverterTest extends ServiceTestBase {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public SearchConverterTest() {
    }

    @Test
    public void testFilterConverter() throws Exception {
        //
        // Creating Resources and Attributes
        //
        Category category = new Category();
        category.setName("MAP");

        categoryService.insert(category);

        for (int i = 0; i < 10; i++) {
            Resource resource = new Resource();
            resource.setName("resource" + i);
            resource.setDescription("resource description" + i);
            resource.setCategory(category);

            List<Attribute> attributes = new ArrayList<Attribute>();

            Attribute attr1 = new Attribute();
            attr1.setName("attr1");
            attr1.setTextValue("value" + i);
            attr1.setType(DataType.STRING);
            attributes.add(attr1);

            Attribute attr2 = new Attribute();
            attr2.setName("attr2");
            attr2.setNumberValue(Double.valueOf(i));
            attr2.setType(DataType.NUMBER);
            attributes.add(attr2);

            Attribute attr3 = new Attribute();
            attr3.setName("attr3");
            attr3.setDateValue(new Date());
            attr3.setType(DataType.DATE);
            attributes.add(attr3);

            resource.setAttribute(attributes);

            long resourceId = resourceService.insert(resource);

            List<ShortAttribute> sAttributes = resourceService.getAttributes(resourceId);
            assertNotNull(sAttributes);
            assertTrue(sAttributes.size() == 3);

            assertNotNull(resourceService.get(resourceId));
            assertTrue(resourceService.getAttributes(resourceId).size() == 3);
        }

        //
        // Complex filter with AND
        //
        {
            String xmlFilter = "<AND>"
                    + "<FIELD>"
                    + "<field>NAME</field>"
                    + "<operator>LIKE</operator>"
                    + "<value>%resource%</value>"
                    + "</FIELD>"
                    + "<AND>"
                    + "<ATTRIBUTE>"
                    + "<name>attr1</name>"
                    + "<operator>EQUAL_TO</operator>"
                    + "<type>STRING</type>"
                    + "<value>value2</value>"
                    + "</ATTRIBUTE>"
                    + "<ATTRIBUTE>"
                    + "<name>attr2</name>"
                    + "<operator>GREATER_THAN</operator>"
                    + "<type>NUMBER</type>"
                    + "<value>1.0</value>"
                    + "</ATTRIBUTE>"
                    + "</AND>"
                    + "</AND>";

            StringReader reader = new StringReader(xmlFilter);
            AndFilter searchFilter = JAXB.unmarshal(reader, AndFilter.class);
            assertNotNull(searchFilter);

            List<ShortResource> resources = resourceService.getResources(searchFilter, null);
            assertEquals(1, resources.size());
        }

        //
        // Complex filter with AND OR
        //
        {
            String xmlFilter = "<AND>"
                    + "<FIELD>"
                    + "<field>NAME</field>"
                    + "<operator>LIKE</operator>"
                    + "<value>%resource%</value>"
                    + "</FIELD>"
                    + "<AND>"
                    + "<ATTRIBUTE>"
                    + "<name>attr2</name>"
                    + "<operator>GREATER_THAN</operator>"
                    + "<type>NUMBER</type>"
                    + "<value>1.0</value>"
                    + "</ATTRIBUTE>"
                    + "<OR>"
                    + "<ATTRIBUTE>"
                    + "<name>attr1</name>"
                    + "<operator>EQUAL_TO</operator>"
                    + "<type>STRING</type>"
                    + "<value>value2</value>"
                    + "</ATTRIBUTE>"
                    + "<ATTRIBUTE>"
                    + "<name>attr1</name>"
                    + "<operator>EQUAL_TO</operator>"
                    + "<type>STRING</type>"
                    + "<value>value3</value>"
                    + "</ATTRIBUTE>"
                    + "</OR>"
                    + "</AND>"
                    + "</AND>";

            StringReader reader = new StringReader(xmlFilter);
            AndFilter searchFilter = JAXB.unmarshal(reader, AndFilter.class);
            assertNotNull(searchFilter);

            List<ShortResource> resources = resourceService.getResources(searchFilter, null);
            assertEquals(2, resources.size());
        }
    }
}

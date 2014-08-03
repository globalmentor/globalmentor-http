/*
 * Copyright © 2012 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.net.http.webdav;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import org.junit.*;

import com.globalmentor.log.Log;
import com.globalmentor.model.NameValuePair;
import com.globalmentor.test.AbstractTest;

/**
 * Tests of a WebDAV resource.
 * @author Garret Wilson
 */
public class WebDAVResourceTest extends AbstractTest {

	@Test
	@Ignore
	public final void testPROPFIND() throws IOException {
		final URI resourceURI = URI.create("https://dav.globalmentor.com/public/");
		final WebDAVResource webDAVResource = new WebDAVResource(resourceURI);
		final List<NameValuePair<URI, Map<WebDAVPropertyName, WebDAVProperty>>> propertiesList = webDAVResource.propFind(Depth.ONE);
		Log.debug(propertiesList);
	}

}

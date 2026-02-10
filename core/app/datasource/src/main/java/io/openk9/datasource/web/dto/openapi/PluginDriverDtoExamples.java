/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package io.openk9.datasource.web.dto.openapi;

public class PluginDriverDtoExamples {

    public static final String HEALTH_STATUS =
        """
		{
			"status":"UP"
		}""";
	public static final String FORM_RESPONSE =
		"""
		{
			"fields": [
			   {
			     "info": "Sitemap Urls to read to extract pages",
			     "label": "Sitemap Urls",
			     "name": "sitemapUrls",
			     "type": "list",
			     "size": 4,
			     "required": true,
			     "values": [
			        {
			          "value": "sitemapUrl1",
			          "isDefault": false
			        },
			        {
			          "value": "sitemapUrl2",
			          "isDefault": false
			        }
			      ],
			     "validator": {
			       "min": 0,
			       "max": 100,
			       "regex": "/[[:alnum:]]+/"
			     }
			   },
			   {
			     "info": "Allowed domain for crawling",
			     "label": "Allowed Domains",
			     "name": "allowedDomains",
			     "type": "list",
			     "size": 4,
			     "required": false,
			     "values": [],
			     "validator": {
			        "min": 0,
			        "max": 100,
			        "regex": "/[[:alnum:]]+/"
			     }
			   },
			   {
			     "info": "Allowed paths for crawling",
			     "label": "Allowed Paths",
			     "name": "allowedPaths",
			     "type": "list",
			     "size": 4,
			     "required": false,
			     "values": [],
			     "validator": {
			        "min": 0,
			        "max": 100,
			        "regex": "/[[:alnum:]]+/"
			     }
			   },
			   {
			     "info": "Excluded paths for crawling",
			     "label": "Excluded Paths",
			     "name": "excludedPaths",
			     "type": "list",
			     "size": 4,
			     "required": false,
			     "values": [],
			     "validator": {
			        "min": 0,
			        "max": 100,
			        "regex": "/[[:alnum:]]+/"
			     }
			   },
			   {
			     "info": "Tag to use to extract main web content",
			     "label": "Body Tag",
			     "name": "bodyTag",
			     "type": "text",
			     "size": 4,
			     "required": true,
			     "values": [
			        {
			          "value": "body",
			          "isDefault": true
			        }
			      ],
			     "validator": {
			        "min": 0,
			        "max": 100,
			        "regex": "/[[:alnum:]]+/"
			     }
			   },
			   {
			     "info": "Tag to use to extract title",
			     "label": "Title tag",
			     "name": "titleTag",
			     "type": "text",
			     "size": 4,
			     "required": true,
			     "values": [
			        {
			          "value": "title::text",
			          "isDefault": true
			        }
			      ],
			     "validator": {
			        "min": 0,
			        "max": 100,
			        "regex": "/[[:alnum:]]+/"
			     }
			   },
			   {
			     "info": "Max length for main content. If negative all text is extracted",
			     "label": "Max Length",
			     "name": "maxLength",
			     "type": "number",
			     "size": 4,
			     "required": true,
			     "values": [
			        {
			          "value": -1,
			          "isDefault": true
			        }
			      ],
			     "validator": {
			        "min": 0,
			        "max": 10000,
			        "regex": "/[[:alnum:]]+/"
			     }
			   },
			   {
			     "info": "Number of pages extracted. If 0 all pages founded in sitemap are scraped",
			     "label": "Page Count",
			     "name": "pageCount",
			     "type": "number",
			     "size": 4,
			     "required": true,
			     "values": [
			        {
			          "value": 0,
			          "isDefault": true
			        }
			      ],
			     "validator": {
			        "min": 0,
			        "max": 100,
			        "regex": "/[[:alnum:]]+/"
			     }
			   },
			   {
			     "info": "If extract documents pippo",
			     "label": "Extract Docs",
			     "name": "doExtractDocs",
			     "type": "checkbox",
			     "size": 4,
			     "required": true,
			     "values": [
			        {
			          "value": false,
			          "isDefault": true
			        }
			      ],
			     "validator": {
			        "min": 0,
			        "max": 100,
			        "regex": "/[[:alnum:]]+/"
			     }
			   },
			   {
			     "info": "File extensions allowed",
			     "label": "Document File Extensions",
			     "name": "documentFileExtensions",
			     "type": "list",
			     "size": 4,
			     "required": true,
			     "values": [],
			     "validator": {
			        "min": 0,
			        "max": 100,
			        "regex": "/[[:alnum:]]+/"
			     }
			   }
			 ]
		}""";
	public static final String PLUGIN_DRIVER_DTO = """
			{
				"type": "HTTP",
				"provisioning": "USER",
				"resourceUri": {
					"baseUri": "http://localhost:8080",
					"path": "/start"
				}
			}
		""";
}

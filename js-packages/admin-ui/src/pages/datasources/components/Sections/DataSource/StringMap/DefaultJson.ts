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
export const DEFAULT_REGEX_TEST_TEXT = `https://example.com
http://example.com/sitemap.xml
https://www.test-site.org/path/to/page
example.com   
https://abc.def123.net/folder/file.html?version=1.0&lang=en`;

export const DEFAULT_XPATH_TEST_TEXT = `<html>
  <head>
    <title>Pagina di esempio</title>
  </head>
  <body>
    <h1>Benvenuto!</h1>
    <div class="content">
      <p id="intro">Questo Ã¨ un esempio di contenuto.</p>
      <p class="highlight">Contenuto importante</p>
      <a href="https://example.com/page1">Pagina 1</a>
      <a href="https://example.com/page2">Pagina 2</a>
    </div>
    <ul class="lista">
      <li>Primo</li>
      <li>Secondo</li>
      <li>Terzo</li>
    </ul>
  </body>
</html>`;

export const DEFAULT_JSONPATH_TEST_TEXT = `{
  "store": {
    "book": [
      { "category": "reference", "author": "Nigel Rees", "title": "Sayings of the Century", "price": 8.95 },
      { "category": "fiction", "author": "Evelyn Waugh", "title": "Sword of Honour", "price": 12.99 }
    ]
  }
}`;


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
      <p id="intro">Questo è un esempio di contenuto.</p>
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

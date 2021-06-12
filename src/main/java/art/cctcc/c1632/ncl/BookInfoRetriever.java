/*
 * Copyright 2021 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package art.cctcc.c1632.ncl;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.RIOT;
//import org.apache.jena.rdf.model.Resource;
//import static org.apache.jena.rdf.model.ResourceFactory.createResource;
//import org.apache.jena.riot.Lang;
//import org.apache.jena.riot.RDFFormat;
//import org.apache.jena.riot.RDFWriter;
//import org.apache.jena.riot.RIOT;
//import org.apache.jena.vocabulary.OWL2;
//import org.apache.jena.vocabulary.RDFS;
import org.jsoup.Jsoup;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class BookInfoRetriever {

  static final String template = "https://aleweb.ncl.edu.tw/F/?func=find-d&find_code=ISBN&request=%s";
  static final Model model = ModelFactory.createDefaultModel();
//  static final Resource BOOK = model.createResource(Chapter.SCHEMA + "Book");

  public static void main(String[] args) throws IOException {

    var works = args.length == 0
            ? List.of(
                    "9789865501259",
                    "9789864762668",
                    "9789572245965",
                    "9789865021993"
            ) : Arrays.asList(args);

    for (String entry : works) {
      System.out.println("-".repeat(80));
      System.out.println("Processing " + entry);

      var book_info = extract(entry);
      System.out.println("-".repeat(80));
      book_info.getAllTriples().stream()
              .peek(System.out::println)
              .forEach(model::add);
    }
    var output = Path.of(System.getProperty("user.dir"), "output", "my_book_info.ttl");
    output.toFile().getParentFile().mkdirs();
    try (var fos = new FileOutputStream(output.toFile())) {
      RDFWriter.create()
              .set(RIOT.symTurtleDirectiveStyle, "sparql")
              .lang(Lang.TTL)
              .source(model)
              .output(fos);
    }
  }

  public static BookInfo extract(String isbn) throws IOException {

    var url = String.format(template, isbn);
    System.out.println("contacting url " + url);
    var query_doc = Jsoup.connect(url).get();
    var href = query_doc.getElementsByTag("tr").stream()
            .filter(e -> e.toString().contains(isbn))
            .filter(e -> !e.getElementsByAttribute("href").isEmpty())
            .map(e -> e.getElementsByAttribute("href").get(0))
            .map(e -> e.attr("href"))
            .findFirst().orElse(null);
    if (href == null) {
      System.out.println("ISBN " + isbn + " cannot be found from ncl catalogue.");
      return null;
    }
    href = href.replace("short-0", "full-set-set") + "&set_entry=000001&format=001";
    var book_doc = Jsoup.connect(href)
            /*
            Install the root certificate (root.crt) to prevent error.
            Ref. https://jfrog.com/knowledge-base/how-to-resolve-unable-to-find-valid-certification-path-to-requested-target-error/
            e.g.,
            keytool -importcert -keystore "%JDK_HOME%\lib\security\cacerts" -storepass changeit -file root.crt -alias "ncl-root"
            
            Alternative: (validateTLSCertificates() is deprecated)
              .timeout(30000)
              .userAgent("Mozilla") // or HttpConnection.DEFAULT_UA
              .validateTLSCertificates(false)
             */
            .get();
    /*
      Field-by-Field Guidelines for New Records: 
      https://www.oclc.org/bibformats/en/input.html
     */
    var book_info = book_doc.getElementsByTag("table").stream()
            .filter(e -> e.hasAttr("cellspacing"))
            .filter(e -> e.toString().contains(isbn))
            .findFirst().get()
            .getElementsByTag("tr").stream()
            .map(e -> e.getElementsByClass("td1").eachText())
            .sorted(Comparator.comparing(list -> list.get(0)))
            .peek(System.out::println)
            .collect(Collectors.groupingBy(
                    list -> list.get(0),
                    Collectors.mapping(list -> list.get(1), Collectors.toList())));
    return new BookInfo(isbn, book_info);
  }
}

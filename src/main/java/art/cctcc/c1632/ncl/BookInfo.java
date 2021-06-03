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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.apache.jena.rdf.model.ResourceFactory.*;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.web.LangTag;
import org.apache.jena.vocabulary.*;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public record BookInfo(String isbn, Map<String, List<String>> book_info) {

  public static String mybookinfo = "http://mybook.info/";
  public static String schema = "https://schema.org/";
  public static String lang_tw = "zh-TW";

  public String getTitle() {

    var _245 = book_info.entrySet().stream()
            .filter(e -> e.getKey().startsWith("245"))
            .findFirst().get()
            .getValue().get(0)
            .split("(\\|a )|(\\|b )|( / \\|c )")[1];
    var _246 = book_info.entrySet().stream()
            .filter(e -> e.getKey().startsWith("246"))
            .findFirst().orElse(Map.entry("", List.of("")))
            .getValue().get(0);
    _246 = _246.isEmpty() ? "" : _246.split("(\\|a )|(\\|b )|( / \\|c )")[1];
    return _245 + _246;
  }

  private String getAuthorString() {

    return book_info.entrySet().stream()
            .filter(e -> e.getKey().startsWith("245"))
            .findFirst().get()
            .getValue().get(0)
            .split("( / \\|c )")[1];
  }

  public List<Statement> getAllTriples() {

    var subject = createResource(mybookinfo + isbn);
    var triples = new ArrayList<Statement>(List.of(
            createStatement(subject, RDF.type, createResource(schema + "Book")),
            createStatement(subject, createProperty(schema, "isbn"), createPlainLiteral(isbn)),
            createStatement(subject, createProperty(schema, "name"), createLangLiteral(getTitle(), lang_tw)),
            createStatement(subject, createProperty(mybookinfo, "authorString"), createLangLiteral(getAuthorString(), lang_tw))
    ));
    return triples;
  }

}

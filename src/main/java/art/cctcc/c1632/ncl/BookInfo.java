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

import com.bigdata.rdf.internal.XSD;
import java.util.List;
import java.util.Map;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public record BookInfo(String isbn, Map book_info) {

  private static String mybookinfo = "http://mybook.info/";
  private static ValueFactory vf = new ValueFactoryImpl();
  private static String schema = "https://schema.org/";

  public List<Statement> getAllTriples() {
    var subject = vf.createURI(mybookinfo, isbn);
    return List.of(
            vf.createStatement(subject, RDF.TYPE, vf.createURI(schema, "Book"))//,
//            vf.createStatement(subject, vf.createURI(mybookinfo, "date"), vf.createLiteral(getDate())),
//            vf.createStatement(subject, vf.createURI(mybookinfo, "headline"), vf.createLiteral(headline)),
//            vf.createStatement(subject, vf.createURI(mybookinfo, "text"), vf.createLiteral(text)),
//            vf.createStatement(subject, vf.createURI(mybookinfo, "media"), vf.createLiteral(media, XSD.ANYURI)),
//            vf.createStatement(subject, vf.createURI(mybookinfo, "media_credit"), vf.createLiteral(media_credit)),
//            vf.createStatement(subject, vf.createURI(mybookinfo, "media_caption"), vf.createLiteral(media_caption))
    );
  }
}

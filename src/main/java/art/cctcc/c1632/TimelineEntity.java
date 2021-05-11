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
package art.cctcc.c1632;

import com.bigdata.rdf.internal.XSD;
import com.opencsv.bean.CsvBindByName;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import static java.util.function.Predicate.not;
import lombok.Getter;
import lombok.ToString;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
@ToString @Getter
public class TimelineEntity {

  private static String mytimeline = "http://mytimeline.info/";
  private static ValueFactory vf = new ValueFactoryImpl();

  @CsvBindByName
  private int year;

  @CsvBindByName
  private int month;

  @CsvBindByName
  private int day;

  @CsvBindByName
  private String time;

  @CsvBindByName
  private String headline;

  @CsvBindByName
  private String text;

  @CsvBindByName
  private String media;

  @CsvBindByName(column = "Media Credit")
  private String media_credit;

  @CsvBindByName(column = "Media Caption")
  private String media_caption;

  public Date getDate() {

    var hhmm = this.time.split(":");
    var hour = Integer.valueOf(hhmm[0]);
    var minute = Integer.valueOf(hhmm[1]);
    var date = String.format("%d-%02d-%02dT%02d:%02d:00+08:00", year, month, day, hour, minute);
    return Date.from(Instant.parse(date));
  }

  public String getEntityId() {

    return Arrays.stream(media.split("[/.]"))
            .filter(not("MyBlazegraphProject"::equals))
            .sorted(Comparator.comparing(String::length, Comparator.reverseOrder()))
            .findFirst().get();
  }

  public URI getSubject() {

    return vf.createURI(mytimeline, getEntityId());
  }

  public List<Statement> getAllTriples() {

    var subject = getSubject();
    return List.of(
            vf.createStatement(subject, RDF.TYPE, vf.createURI(mytimeline, "TimelineEntity")),
            vf.createStatement(subject, vf.createURI(mytimeline, "date"), vf.createLiteral(getDate())),
            vf.createStatement(subject, vf.createURI(mytimeline, "headline"), vf.createLiteral(headline)),
            vf.createStatement(subject, vf.createURI(mytimeline, "text"), vf.createLiteral(text)),
            vf.createStatement(subject, vf.createURI(mytimeline, "media"), vf.createLiteral(media, XSD.ANYURI)),
            vf.createStatement(subject, vf.createURI(mytimeline, "media_credit"), vf.createLiteral(media_credit)),
            vf.createStatement(subject, vf.createURI(mytimeline, "media_caption"), vf.createLiteral(media_caption))
    );
  }
}

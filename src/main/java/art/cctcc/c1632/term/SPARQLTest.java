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
package art.cctcc.c1632.term;

import com.bigdata.rdf.sail.webapp.client.RemoteRepositoryManager;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class SPARQLTest {

  static String default_query
          = """
            SELECT ?type ?property ?literal
            WHERE { 
              ?s ?property ?o ; 
                 a ?type
              FILTER (?type != rdf:type)
              BIND (IF (isIRI(?o), "IRI", "Literal") AS ?literal)
            } 
            GROUP BY ?type ?property ?literal
            ORDER BY ?type ?literal """;

  public static void main(String[] args) {

    if (args.length == 0) {
      System.out.printf(
              """
              Please specify your SPARQL Endpoint by:
              mvn exec:java@term -Dexec.args=http://{your_sparql_endpoint}:9999/sparql
              
              Default query is
              
              %s
              
              You can also specify it by:
              mvn exec:java@term -Dexec.args="http://your_blazegraph_ip:9999/sparql 'your_query_string'"
              """, default_query);
      System.exit(0);
    }
    String url = args[0]; //"http://23.239.21.18:9999/sparql";
    String query = (args.length > 1 && !"-".equals(args[1]))
            ? args[1]
            : "default";
    try ( var manager = new RemoteRepositoryManager()) {
      System.out.println("Connecting to SPARQL Endpoint " + url + "...\n");
      final var repo = manager.getRepositoryForURL(url)
              .getBigdataSailRemoteRepository();
      repo.initialize();
      try {
        final var cxn = repo.getConnection();
        try {
          final var tupleQuery = cxn.prepareTupleQuery(
                  QueryLanguage.SPARQL,
                  "default".equals(query) ? default_query : query);
          final var result = tupleQuery.evaluate();
          final var result_list = new ArrayList<BindingSet>();
          try {
            if ("default".equals(query)) {
              int width1 = 0, width2 = 0;
              while (result.hasNext()) {
                var rec = result.next();
                result_list.add(rec);
                var current_width1 = rec.getValue("type").toString().length();
                var current_width2 = rec.getValue("property").toString().length();
                width1 = current_width1 > width1 ? current_width1 : width1;
                width2 = current_width2 > width2 ? current_width2 : width2;
              }
              final var template = "%-" + width1 + "s  %-7s  %-" + width2 + "s\n";
              result_list.forEach(rec -> System.out.printf(
                      template,
                      rec.getValue("type"),
                      rec.getValue("literal").stringValue(),
                      rec.getValue("property")));
            } else {
              while (result.hasNext()) {
                System.out.println(result.next());
              }
            }
          } finally {
            result.close();
          }
        } finally {
          cxn.close();
        }
      } finally {
        repo.shutDown();
      }
    } catch (Exception ex) {
      Logger.getLogger(SPARQLTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}

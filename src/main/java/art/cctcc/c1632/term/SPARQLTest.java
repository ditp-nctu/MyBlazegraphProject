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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.query.QueryLanguage;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class SPARQLTest {

  public static void main(String[] args) {

    if (args.length == 0) {
      System.out.println(
              """
              Please specify SPARQL Endpoint, for example:
              mvn exec:java@term -Dexec.args=http://your_blazegraph_ip:9999/sparql
              You can also specify query string, like:
              mvn exec:java@term -Dexec.args="http://your_blazegraph_ip:9999/sparql 'your_query_string'"
              """);
      System.exit(-1);
    }
    String url = args[0]; //"http://23.239.21.18:9999/sparql";
    String query = (args.length > 1)
            ? args[1]
            : "SELECT * WHERE { ?s ?p ?o } LIMIT 10";
    try ( var manager = new RemoteRepositoryManager()) {
      System.out.println("Connecting to SPARQL Endpoint " + url + "...");
      final var repo = manager.getRepositoryForURL(url)
              .getBigdataSailRemoteRepository();
      repo.initialize();
      try {
        final var cxn = repo.getConnection();
        try {
          final var tupleQuery = cxn.prepareTupleQuery(
                  QueryLanguage.SPARQL,
                  query);
          final var result = tupleQuery.evaluate();
          try {
            while (result.hasNext()) {
              var rec = result.next();
              System.out.printf("%s\t%s\t%s\n",
                      rec.getValue("s"),
                      rec.getValue("p"),
                      rec.getValue("o")
              );
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

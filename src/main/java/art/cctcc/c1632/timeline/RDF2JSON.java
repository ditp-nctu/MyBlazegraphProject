package art.cctcc.c1632.timeline;

import com.bigdata.journal.BufferMode;
import com.bigdata.journal.Options;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class RDF2JSON {

  public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException, UpdateExecutionException, IOException {

    final var props = new Properties();

    /*
	 * For more configuration parameters see
	 * http://www.blazegraph.com/docs/api/index.html?com/bigdata/journal/BufferMode.html
     */
    props.put(Options.BUFFER_MODE, BufferMode.DiskRW); // persistent file system located journal
    props.put(Options.FILE, BuildRDFGraph.JNL_PATH.toString()); // journal file location

    final var sail = new BigdataSail(props); // instantiate a sail
    final var repo = new BigdataSailRepository(sail); // create a Sesame repository

    repo.initialize();

    try {
      // open repository connection
      var cxn = repo instanceof BigdataSailRepository
              ? ((BigdataSailRepository) repo).getReadOnlyConnection()
              : repo.getConnection();

      // evaluate sparql query
      try {
        final var tupleQuery = cxn
                .prepareTupleQuery(QueryLanguage.SPARQL,
                        """
                        PREFIX : <http://mytimeline.info/>
                        
                        SELECT ?year ?month ?day ?hour ?minute ?headline ?credit ?caption ?media ?text
                        WHERE {
                          ?s a :TimelineEntity ;
                           	 :date ?date ;
                             :headline ?headline ;
                             :media_credit ?credit ;
                             :media_caption ?caption ;
                             :media ?media ;
                             :text ?text .
                          BIND (year(?date) AS ?year) 
                          BIND (month(?date) AS ?month) 
                          BIND (day(?date) AS ?day) 
                          BIND (xsd:string(hours(?date)) AS ?hour) 
                          BIND (xsd:string(minutes(?date)) AS ?minute)
                        }
                        """);
        final var result = tupleQuery.evaluate();
        final var events = new JSONArray();
        try {
          while (result.hasNext()) {
            var bindingSet = result.next();
            System.out.println(bindingSet);
            var date = new JSONObject()
                    .put("year", Integer.valueOf(bindingSet.getValue("year").stringValue()))
                    .put("month", Integer.valueOf(bindingSet.getValue("month").stringValue()))
                    .put("day", Integer.valueOf(bindingSet.getValue("day").stringValue()))
                    .put("hour", Integer.valueOf(bindingSet.getValue("hour").stringValue()))
                    .put("minute", Integer.valueOf(bindingSet.getValue("minute").stringValue()));
            var text = new JSONObject()
                    .put("headline", bindingSet.getValue("headline").stringValue())
                    .put("text", bindingSet.getValue("text").stringValue());
            var media = new JSONObject()
                    .put("url", bindingSet.getValue("media").stringValue())
                    .put("caption", bindingSet.getValue("caption").stringValue())
                    .put("credit", bindingSet.getValue("credit").stringValue());
            var event = new JSONObject()
                    .put("start_date", date)
                    .put("text", text)
                    .put("media", media);
            events.put(event);
          }
          var json = new JSONObject()
                  .put("events", events);
          System.out.println("json = " + json.toString(2));
          var path = Path.of(System.getProperty("user.dir"), "output");
          path.toFile().mkdirs();
          Files.writeString(path.resolve("timeline.json"), json.toString(2));
        } finally {
          result.close();
        }
      } finally {
        cxn.close();  // close the repository connection
      }
    } finally {
      repo.shutDown();
    }

  }

}

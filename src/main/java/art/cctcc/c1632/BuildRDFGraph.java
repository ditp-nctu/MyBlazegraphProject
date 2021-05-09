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

import com.bigdata.journal.BufferMode;
import com.bigdata.journal.Options;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class BuildRDFGraph {

  public static void main(String[] args) throws FileNotFoundException, IOException, RepositoryException {

    var filename = BuildRDFGraph.class.getResource("/TimelieJS.csv").getFile();
    var timelineEntities = new CsvToBeanBuilder<TimelineEntity>(new FileReader(filename))
            .withType(TimelineEntity.class)
            .build()
            .parse();

    final Properties props = new Properties();

    /*
		 * For more configuration parameters see
		 * http://www.blazegraph.com/docs/api/index.html?com/bigdata/journal/BufferMode.html
     */
    props.put(Options.BUFFER_MODE, BufferMode.DiskRW); // persistent file system located journal
    props.put(Options.FILE, "/tmp/blazegraph/blazegraph.jnl"); // journal file location

    final BigdataSail sail = new BigdataSail(props); // instantiate a sail
    final Repository repo = new BigdataSailRepository(sail); // create a Sesame repository

    repo.initialize();

    try {
      // open repository connection
      RepositoryConnection cxn = repo.getConnection();
      // upload data to repository
      try {
        cxn.begin();
        for (TimelineEntity timelineEntity : timelineEntities) {
          cxn.add(timelineEntity.getAllTriples());
        }
        cxn.commit();
      } catch (OpenRDFException ex) {
        cxn.rollback();
        throw ex;
      } finally {
        // close the repository connection
        cxn.close();
      }
    } finally {
      repo.shutDown();
    }
  }
}

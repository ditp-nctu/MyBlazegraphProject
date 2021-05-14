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
package art.cctcc.c1632.timeline;

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
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class BuildRDFGraph {

  public static void main(String[] args) throws FileNotFoundException, IOException, RepositoryException {

    var filename = BuildRDFGraph.class.getResource("/timeline/data.csv").getFile();
    var timelineEntities = new CsvToBeanBuilder<TimelineEntity>(new FileReader(filename))
            .withType(TimelineEntity.class)
            .build()
            .parse();

    timelineEntities.stream()
            .peek(entity -> System.out.println(entity.getEntityId()))
            .map(TimelineEntity::getAllTriples)
            .flatMap(List::stream)
            .forEach(System.out::println);

    /*
     * For more configuration parameters see
     * http://www.blazegraph.com/docs/api/index.html?com/bigdata/journal/BufferMode.html
     */
    final var props = new Properties();

    props.put(Options.BUFFER_MODE, BufferMode.DiskRW); // persistent file system located journal
    props.put(Options.FILE, "/tmp/blazegraph/blazegraph.jnl"); // journal file location

    final var sail = new BigdataSail(props); // instantiate a sail
    final var repo = new BigdataSailRepository(sail); // create a Sesame repository

    repo.initialize();

    try {
      var cxn = repo.getConnection(); // open repository connection      
      try {
        cxn.begin();
        for (var entity : timelineEntities) {
          cxn.add(entity.getAllTriples());
        }
        cxn.commit(); // upload data to repository
      } catch (OpenRDFException ex) {
        cxn.rollback();
        throw ex;
      } finally {
        cxn.close();  // close the repository connection
      }
    } finally {
      repo.shutDown();
    }
  }
}

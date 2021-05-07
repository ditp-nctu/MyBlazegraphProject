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
import java.util.Properties;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class BuildRDFGraph {

  public static void main(String[] args) throws RepositoryException {
    final Properties props = new Properties();

    /*
		 * For more configuration parameters see
		 * http://www.blazegraph.com/docs/api/index.html?com/bigdata/journal/BufferMode.html
     */
    props.put(Options.BUFFER_MODE, BufferMode.DiskRW); // persistent file system located journal
    props.put(Options.FILE, "/tmp/blazegraph/test.jnl"); // journal file location

    final BigdataSail sail = new BigdataSail(props); // instantiate a sail
    final Repository repo = new BigdataSailRepository(sail); // create a Sesame repository

    repo.initialize();

    try {
      // prepare a statement
      final URIImpl subject = new URIImpl("http://blazegraph.com/Blazegraph");
      final URIImpl predicate = new URIImpl("http://blazegraph.com/says");
      final Literal object = new LiteralImpl("hello");
      final Statement stmt = new StatementImpl(subject, predicate, object);

      // open repository connection
      RepositoryConnection cxn = repo.getConnection();

      // upload data to repository
      try {
        cxn.begin();
        cxn.add(stmt);
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

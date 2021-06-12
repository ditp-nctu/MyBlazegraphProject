# RDF Graph Examples with Blazegraph
Supplemental material for NYCU DIPT Course: *A Computational Journey Towards Humanities and Creativity: Starting from Graph*

## Final Term SPARQL Endpoint validation

Please execute the following command (with Java and Maven properly installed and setup):
```
mvn clean compile
```
then:
```
mvn exec:java@term -Dexec.args={your_sparql_endpoint}
```
For example:
```
mvn exec:java@term -Dexec.args=http://23.239.21.18:9999/sparql
```
A valid output with RDF types and their properties correctly shown would be like:
```
Connecting to SPARQL Endpoint http://23.239.21.18:9999/sparql...

http://c1632.cctcc.art/Assignment         http://c1632.cctcc.art/topic                     IRI
http://c1632.cctcc.art/Assignment         http://www.w3.org/1999/02/22-rdf-syntax-ns#type  IRI
http://c1632.cctcc.art/Assignment         http://c1632.cctcc.art/underTopic                IRI
http://c1632.cctcc.art/Assignment         http://c1632.cctcc.art/url                       Literal
http://c1632.cctcc.art/Assignment         https://schema.org/name                          Literal
http://c1632.cctcc.art/Assignment_State   http://c1632.cctcc.art/owned_by                  IRI
http://c1632.cctcc.art/Assignment_State   http://c1632.cctcc.art/state_of                  IRI
http://c1632.cctcc.art/Assignment_State   http://www.w3.org/1999/02/22-rdf-syntax-ns#type  IRI
http://c1632.cctcc.art/Assignment_State   http://c1632.cctcc.art/status                    Literal
http://c1632.cctcc.art/Assignment_State   http://c1632.cctcc.art/submitted                 Literal
http://c1632.cctcc.art/CourseTopic        http://c1632.cctcc.art/hasAssignment             IRI
http://c1632.cctcc.art/CourseTopic        https://schema.org/isPartOf                      IRI
http://c1632.cctcc.art/CourseTopic        http://www.w3.org/1999/02/22-rdf-syntax-ns#type  IRI
http://c1632.cctcc.art/CourseTopic        http://c1632.cctcc.art/hasActivity               IRI
http://c1632.cctcc.art/CourseTopic        http://c1632.cctcc.art/gradePercentage           Literal
http://c1632.cctcc.art/CourseTopic        https://schema.org/name                          Literal                   
...                
```
You can also add your own query:
```
mvn exec:java@term -Dexec.args="{your_sparql_endpoint} '{your_sparql_code}'"
```
For example:
```
mvn exec:java@term -Dexec.args="http://23.239.21.18:9999/sparql 'SELECT * WHERE { ?s ?p ?o } LIMIT 50'"
```
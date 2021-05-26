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

http://c1632.cctcc.art/Assignment         IRI      http://c1632.cctcc.art/topic                   
http://c1632.cctcc.art/Assignment         IRI      http://www.w3.org/1999/02/22-rdf-syntax-ns#type
http://c1632.cctcc.art/Assignment         IRI      http://c1632.cctcc.art/underTopic              
http://c1632.cctcc.art/Assignment         Literal  http://c1632.cctcc.art/url                     
http://c1632.cctcc.art/Assignment         Literal  https://schema.org/name                        
http://c1632.cctcc.art/Assignment_State   IRI      http://c1632.cctcc.art/owned_by                
http://c1632.cctcc.art/Assignment_State   IRI      http://c1632.cctcc.art/state_of                
http://c1632.cctcc.art/Assignment_State   IRI      http://www.w3.org/1999/02/22-rdf-syntax-ns#type
http://c1632.cctcc.art/Assignment_State   Literal  http://c1632.cctcc.art/status                  
http://c1632.cctcc.art/Assignment_State   Literal  http://c1632.cctcc.art/submitted               
http://c1632.cctcc.art/CourseTopic        IRI      http://c1632.cctcc.art/hasAssignment           
http://c1632.cctcc.art/CourseTopic        IRI      https://schema.org/isPartOf                    
http://c1632.cctcc.art/CourseTopic        IRI      http://www.w3.org/1999/02/22-rdf-syntax-ns#type
http://c1632.cctcc.art/CourseTopic        IRI      http://c1632.cctcc.art/hasActivity             
http://c1632.cctcc.art/CourseTopic        Literal  http://c1632.cctcc.art/gradePercentage         
http://c1632.cctcc.art/CourseTopic        Literal  https://schema.org/name                        
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
# Exercise Description

You’ve been hired as a market consultant to try and help a local supermarket come up with better placement of items based on buyer’s preferences, and towards that goal you’d like to identify certain association rules based on existing records of buyer’s transactions. 

 
You are given as input:

- A transaction database—a file consisting of a single row per transaction, with individual product's SKUs given as space-separated integers. A single transaction consisting of products with SKUs 1001, 1002 and 1003 would have a line that looks like: ‘1001 1002 1003' 
    
- A minimal ’support level’ parameter, sigma – a positive integer

Implement, in Python or Java, an efficient algorithm for generating all frequent item sets of size 3 or more: groups of 3 or more items that appear together in the transactions log at least as often as the support level parameter value sigma.

 
For example, given a value of sigma = 2, all sets of 3 items or more that appear 2 or more times together in the transaction log should be returned. The results should be returned as a file with the following format: <item set size (N)>, <co-occurrence frequency>, <item 1 id >, <item 2 id>, …. <item N id>  Run the algorithm on the attached transaction log file and provide the results obtained for a value of sigma = 4.

# Solution proposed
As a first solution, 
- I clean the data (remove lines with less than 3 items, remove product ids which occur less than sigma times)
- I compare all the rows of the transaction database with themselves
- for each comparison
    - find all common product ids
    - generate all possible combinations with 3 or more items
    - store them in storage engine using both memory and disk
- filter combinations appearing less than sigma times
- display the results as requested in the app log in an output file

On laptop with 8 cores, using 6gb of memory and max space of 60gb on disk,
it took X to complete the analysis.
It stored X millions combinations and among them X unique combinations with at least 4 occurrences.

# How to run it
Or create the jar file in the repository folder:
`mvn package -Dmaven.test.skip=true`

Then run it the following way with java 8: 

`java -jar java-interview-exercises-1.0-SNAPSHOT.jar <transactions database file> <sigma> <parent directory for store> <output filepath>`

`java -Xmx4g -jar java-interview-exercises-1.0-SNAPSHOT.jar retail_25k.dat 4 "/tmp/globalityCache" "/tmp/output.txt"` 

# Improvements
- ehCache is a caching system, not a database even if it can be used as such.
I chose to use it because I wanted a very simple key value store that can use both the heap and the disk to manage the large amount of combinations we have, and which can be directly embedded in the application without running a separate service.
<br/>
I would evaluate different solutions (postgreSQL, Mongo, Redis, Neo4j, etc) based on the following needs:
-- atomic updates, speed of writing, storage on disk 

- Need properties files to manage different environments and more work probably to enable running unit tests on another laptop. Had to put the storage folder on another custom partition. Or you will need to change it in the unit test. This is why I offered to package the app while skipping tests. 

- I considered using Neo4j, but didn't find an obvious solution in a first quick search of its graph algorithms. I didn't want to add the extra effort for just this interview exercise to learn again neo4j that I haven't used for some time.
Nevertheless, it might be helpful in that case to use a graph database or even some clustering algorithm. Something to explore with more time.

- the current configuration of the storage use primarily the disk and less so the heap. It's required since the amount to store is very large but it also makes the whole system quite slow. Also considering the amount of possible combinations (nearly 200 millions total occurrences half way through), it would make sense, depending on the budget, to run this on a server with a large amount of memory or a cluster of servers with database partitions. Running it on a laptop is really challenging.
 

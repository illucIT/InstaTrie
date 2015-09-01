# InstaTrie

Performant Prefix Index and Searchword Highlighter, implemented with Trie data structures.

## Key Features

* Sophisticated generic data structure for efficient prefix-lookup
* Support for custom extractor functions to retrieve keywords from POJOs
* Ability to ignore (simple) HTML Tags
* Highlighting of found parts in searched
* Order of entries is retained
* Optional ASCII-Normalization of search keywords and queries
* Ability to create proxies for the data structurw with additional filters
* Compatible to Java List API
* Support for Java8 Streams API
* No further dependencies (except for the ASCII-normalization utility)

## Setup

To use `InstaTrie` to your Maven project, you will need to add the public illucIT Maven reporitory to your Maven `pom.xml`. Then you can simply add `InstaTrie` as dependency:

    <dependency>
        <groupId>com.illucit</groupId>
        <artifactId>InstaTrie</artifactId>
        <version>1.5</version>
    </dependency>
    
    <repositories>
        <!-- illucIT Company Repository -->
        <repository>
            <id>illucit</id>
            <name>illucIT Public Maven Repository</name>
            <url>http://repository.illucit.com</url>
        </repository>
    </repositories>

The only dependency of `InstaTrie` is the library `lucene-utils` for String normalization, which has no dependencies itself.

## Trie Data Structure

A "Trie" is a tree-like data structure, which has a character on each edge and where each node represents a prefix.
It is known to be extremely efficient for storing and retrieving all words with a common prefix.
For further theoretical information about tries, you can look in the [Wikipedia](https://en.wikipedia.org/wiki/Trie).

The interface `com.illucit.instatrie.trie.PrefixDictionary<T>` and class `com.illucit.instatrie.trie.Trie<T>`
provide an implementation of this data structure, which can hold a set of stored Strings and can retrieve all matching Strings with a given prefix.
For each inserted String you can also add a payload object of the generic type `<T>`.

Although the `Trie` class was basically implemented for the Prefix Index functionality of this library, you can use it as its own.
The `Trie` class does not implement `java.util.Collection`, but provides the most common methods to manipulate data. See the public
interface `com.illucit.instatrie.trie.PrefixDictionary<T>` for a full list of supported operations.

## Prefix Index Data Structure

The "Prefix Index" data structure resembles a generic collection of models of a specific type, which can be filtered by search queries.

### Word Splitter

First of all, you define a function to extract a searchable String from each model, which will be indexed. This function is called a "Word Splitter".  
E.g. if your model type is a class `Person` with properties `firstName` and `lastName`, your Word Splitter should take an instance of `Person`
and return a Set containing the first name and last name of the person.  
You can also use the more convenient class `com.illucit.instatrie.splitter.StringWordSplitter<T>`,
for which you only have to prove a function get one single String from your model and the class will provide all "indexable" text fragments (per default all
connected alphanumeric substrings) from this String representation.  
E.g. for your example with the `Person` class:

    WordSplitter<Person> splitter = new StringWordSplitter<>(p -> p.getFirstName() + " " + p.getLastName());

So if you have a `Person` with `firstName = "John Walter"` and `lastName = "Doe"`, your Splitter will provide a set with `"John"`, `"Walter"` and `"Doe"``.

#### Unicode Normalization

If you actually use the `StringWordSplitter` class, the given String can be normalized, i.e. special unicode characters are replaced by their "basic" characters where possible,
e.g. `"ö"` -> `"o"`, `"Â"` -> `"A"` or `"©"` -> `"c"`.

### TriePrefixIndex

You can instatiate the `com.illucit.instatrie.index.TriePrefixIndex<T>` class by providing a Splitter for `"<T>"`. Once the prefix index instance is created,
you can add data to it by calling the `TriePrefixIndex.createIndex(Collection<T> models)` method, which will remove the previous index and build a new index
with the given collcction and the word splitter.

#### Concurrency

The class `TriePrefixIndex` is designed for concurrent access. An instance of `TriePrefixIndex` always takes the most recent available index data.
If you call the `createIndex` method, until the method is finished all method call querying the prefix index will still be going to the current index.
Once the index calculation is done, it will be set as most recent index for the `TriePrefixIndex` in an atomic operation and all future query calls will use the new index.

You can use a `TriePrefixIndex` instance from multiple threads simultaniously without gettings "concurrent modification" problems, as each query call will use the index data
from the beginning of the call during the whole execution of the query.

If multiple Threads share an instance of `TriePrefixIndex` and one thread refreshes the index, all Threads will get the new index from the moment it is calculated completely.



# InstaTrie

Performant Prefix Index and Searchword Highlighter, implemented with Trie data structures.

## Key Features

* Sophisticated generic data structure for efficient prefix-lookup
* Support for custom extractor functions to retrieve keywords from POJOs
* Ability to ignore (simple) HTML Tags
* Highlighting of found parts in searched models
* Order of entries is retained
* Optional ASCII-Normalization of search keywords and queries
* Ability to create proxies for the data structure with additional filters
* Compatible to Java List API
* Support for Java8 Streams API
* No further dependencies (except for the ASCII-normalization utility)

## Setup

To use `InstaTrie` in your Maven project, you will need to add the public illucIT Maven reporitory to your Maven `pom.xml`. Then you can simply add `InstaTrie` as dependency:

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

The only dependency of `InstaTrie` is the library `lucene-utils` for String normalization, which has no further dependencies itself.

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

First of all, you define a function to extract a searchable String from each model, which will be indexed. This function is called a "Word Splitter" and
has to be realized by a class that implements.`com.illucit.instatrie.splitter.WordSplitter<T>`.  
E.g. if your model type is a class `Person` with properties `firstName` and `lastName`, your Word Splitter should take an instance of `Person`
and return a Set containing the first name and last name of the person.  
You can also use the more convenient class `com.illucit.instatrie.splitter.StringWordSplitter<T>`,
for which you only have to prove a function get one single String from your model and the class will provide all "indexable" text fragments by a regular expression (per default all
connected alphanumeric substrings) from this String representation. This also has the advantage that you don't need to provide an individual splitting into different words (e.g. if your person has multiple first names), but you only give the regular expression a String to work on.  
E.g. for your example with the `Person` class:

    WordSplitter<Person> splitter;
    splitter = new StringWordSplitter<>(p -> p.getFirstName() + " " + p.getLastName());

So if you have a `Person` with `firstName = "John Walter"` and `lastName = "Doe"`, your Splitter will provide a set with `"John"`, `"Walter"` and `"Doe"``.

*Note:* If your model contains values with HTML tags which you want to make searchable (and therefore include in the splitter), make sure you remove the tags before
(e.g. with an HTML parser utility like JSoup).

#### Unicode Normalization

If you actually use the `StringWordSplitter` class, the given String can be normalized, i.e. special unicode characters are replaced by their "basic" characters where possible,
e.g. `"ö"` -> `"o"`, `"Â"` -> `"A"` or `"©"` -> `"c"`.

### TriePrefixIndex

The interface `com.illucit.instatrie.index.PrefixIndex<T>` and class `com.illucit.instatrie.index.TriePrefixIndex<T>` provide an implementation
for the "Prefix Index" collection type. The collection basically consists of an (ordered) list of models of the generic type `"<T>"`, but provides additional
methods to query the list of models for a search String and get back a (reduced, still ordered) list of models that contain this search query.

You can create and instance of the `com.illucit.instatrie.index.TriePrefixIndex<T>` class with an empty index by providing a Splitter for `"<T>"`.
Once the prefix index instance is created, you can add data to it by calling the `TriePrefixIndex.createIndex(Collection<T> models)`
method with a new list of models, which will overwrite the previous index and build a new index with the given collection and the fixed word splitter.

    TriePrefixIndex<Person> index = new TriePrefixIndex<>(wordSplitter);
    List<Person> personList = ...
    index.createIndex(personList);

#### Querying

The "Prefix Index" collection provides two basic query operations: `search` and `searchExact`. While the operation `search` will retrieve all models which
match all given search terms as prefix, the operation `searchExact` will get you all models which contain all given search terms as complete words. Searching is
alsways case-insensititve, so the case of the model search terms and the query String won't matter.

First of all, the search query will itself always be splitted into individual words with the help of a `StringSearchSplitter`. So if you search for multiple words at once,
it is always considered as "AND" search, meaning all searched terms must be found for a hit. There is currently no support for query operations, as 
the "Prefix Index" is built for filtering and narrowing something down, not as full-fledged search engine.

* The query `"DOE"` will only search for the word `"doe"`.
* The queries `"doe, john"`, `"john doe"` and `"......(joHN]] DOe?"` will all result in the search for the two terms `"john"` and `"doe"`.
* An empty query (i.e. no alphanumerical characters) will always return the full list of models as there are no filter criteria that can fail.

When one of the search methods is called, the search words are compared with the splitted words in the index for each model individually,
so a model in the index can either match the criteria or not.
Given the `Person` model mentioned above, the model has the index terms `"john"`, `"walter"` and `"doe"`.

* If you make an exact query for `"doe"` the model will match, as the word `"doe"` is fully contained in the set of index terms
* If you make an exact query for `"walt"` the model will not match, as the word `"walt"` is only a prefix of the index term `"walter"`, but not fully contained in the index terms
* If you make a (non-exact) query for `"walt"` the model will match, because `"walt"` is a prefix of the index term `"walter"`
* If you make a query for `"jane"` and `"doe"` the model will not match, because `"jane"` is not a prefix of an index term (although `"doe"` is)

*Note*:If a model matches an exact query, it always also matches the non-exact query, because each word is considered a prefix of itself

#### List Interface

The `PrefixIndex` interface does not directly extend a class from the Java Collection API. It provides, however, a method `decorateAsList`
which will return a proxy object which implements `java.util.List<T>` and is still connected to the original `PrefixIndex` instance.
All write-operations of the `List`-proxy will result in `UnsupportedOperationException`, and all read-operations will be delegated to the
internal list of models contained in the current index of the `PrefixIndex`.

The returned proxy is permanently connected to the original `TriePrefixIndex` instance, so when you rebuild the index on one of the instances,
it will also change the index of the other instance. All Trie query operations on both instances will return exactly the same result.

#### Concurrency

The class `TriePrefixIndex` is designed for concurrent access. An instance of `TriePrefixIndex` always takes the most recent available index data.
If you call the `createIndex` method, until the method is finished all method call querying the prefix index will still be going to the current index.
Once the index calculation is done, it will be set as most recent index for the `TriePrefixIndex` in an atomic operation and all future query calls will use the new index.

You can use a `TriePrefixIndex` instance from multiple threads simultaniously without gettings "concurrent modification" problems, as each query call will use the index data
from the beginning of the call during the whole execution of the query.

If multiple Threads share an instance of `TriePrefixIndex` and one thread refreshes the index, all Threads will get the new index from the moment it is calculated completely.

#### Java8 Features

In Java8 it is possible to use "Lambda Expressions" to provide implementation sof interfaces with only 1 unimplemented method (like `java.util.Function`).
Instead of providing a `WordSplitter` object when creating a `TriePrefixIndex`, you can also directly pass a lambda expression for a `StringWordSplitter` in the constructor:

     TriePrefixIndex<Person> index = new TriePrefixIndex<>(p -> p.getFirstName() + " " + p.getLastName());

The `PrefixIndex` also provides a method `searchStream` which behaves exactly like the `search` method, but instead of returning a `List` of models, it will return
a Java8 `java.util.stream.Stream<T>` containing the models.

#### Filtering

In some cases it might be helpful to get a "view" for an existing prefix index, which only returns a subset of the indexed models. You could have e.g. a database of articles
from multiple books and want to have a prefix index containing all articles, and then one prefix index for each book containing only the articles from that book.

One simple solution would be to create multiple indexes, but that will consume twice the memory of the single, full index. Another solution might be to
always append Java8 filters on each search result from the big index, depending on the use case.

However, the `PrefixIndex` interface provides a method itself to help you with this use case. The method `getFilteredView(filterFunction)` will return
a proxy to the original prefix index, but where all search results are automatically filtered by the given filter function paramete (which is a Java8 predicate
and can be written as lambda expression). The two instances are permanently connected and the refreshing of the index on one instance will also change the index of the other instance.
Because both instances share the same state, the memory usage is exactly the same as with the original index (except for the additional proxy object which contains no data).
You can even create a new filter proxy with another filter predicate from the first filter proxy, which will yield a new filter proxy with a combined filter predicate (both must be true).

This feature can be combined with the "list proxy" functionality described above; when you create a filter proxxy< from a list proxy, you will get a proxy of the original
prefix index which has a filtered view as well as list capabilities.

## Highlighting

When you use the `PrefixIndex` to filter down elements in a graphical user interface (GUI), especially on a web page, it is helpful to see where a model matched the search terms.
For this, the InstaTrie library also provides String highlighting capabilities.

The interface `com.illucit.instatrie.highlight.SubwordHighlighter` contains methods to generate a String segmentation of a given model String property,
in which the highlighted parts can be identified. The default implementation of `SubwordHighlighter` is the class `StringWordSplitter`, also used in splitting.
The class `TriePrefixIndex` also implements the interface `SubwordHighlighter` and delegates all its calls to the `StringWordSplitter` used
for splitting the search queries.

There are two different modi in which the highlighting can run: "HTML mode" and "Text mode". The "HTML mode" is designed for the cases in which the searchable
model properties can contain simple HTML markup, e.g. bold, underlined, subscript etc. Because InstaTrie has no bundled HTML parser, only simple tags without attributes
and with distinct closing tags (i.e. no "empty" tags) are supported. These tags are ignored when calculating the search highlights, even if the highlights span across multiple tags.
In "Text mode" the given model String is taken "as-is". In both cases the matching is ignoring case and also ignoring unicode specialities (unless deactivated
in the `StringWordSplitter`), so that e.g. `"DØĚ"` will match `"doe"`.

To use the highlighting, you can call the methods `highlightSubwordPrefixes(value, queryWords)` or `highlightSubwordPrefixesWithHtml(value, queryWords)`
on a `SubwordHighlighter` instance. The first parameter is the model String value that you want to render with highlighting and the second parameter is a `Set` with the
search termn that were used for search, as returned by the `WordSplitter`.
The `TriePrefixIndex` class also provides the methods `getHighlighted(modelValue, query)` and `getHighlightedHtml(modelValue, query)` which do the splitting
of the query String `query` into the `queryWords` set for you with the internal query splitter.

The returned value is an instance of the class `com.illucit.instatrie.highlight.HighlightedString` which contains a method `getSegments()` returning a list of all
highlighted and non-highligted segmenty, you can use for rendering.

 E.g.

    Person john = new Person("John Walter", "Doe");
    Person jane = new Person("Jane", "Doe");
    TriePrefixIndex<Person> index = ...
    String search = "walt do";
    HighlightedString highlighted;
    
    highlighted = index.getHighlighted(john.getFirstName(), search);
    for (HighlightSegment segment : highlighted.getSegments()) {
        if (segment.isHighlighted()) {
            System.out.print("[[" + segment.getValue() + "]]");
        } else {
            System.out.print(segment.getValue());
        }
    }
    System.out.println();
    // Prints: John [[Walt]]er
    
    highlighted = index.getHighlighted(john.getLastName(), search);
    for (HighlightSegment segment : highlighted.getSegments()) {
        if (segment.isHighlighted()) {
            System.out.print("[[" + segment.getValue() + "]]");
        } else {
            System.out.print(segment.getValue());
        }
    }
    System.out.println();
    // Prints: [[Do]]e
    
    highlighted = index.getHighlighted(jane.getFirstName(), search);
    for (HighlightSegment segment : highlighted.getSegments()) {
        if (segment.isHighlighted()) {
            System.out.print("[[" + segment.getValue() + "]]");
        } else {
            System.out.print(segment.getValue());
        }
    }
    System.out.println();
    // Prints: Jane

## Future Planning

There are some featur ideas which didn't make it into the current implementation:

* Currently only the whole index can be refreshed. If you want to add a model to the index, you have to combine the current list of models with the new model in a temporary list,
and create a new index from the whole new list. 

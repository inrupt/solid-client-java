# Digital Book Library on a Pod

You are building the next-generation digital book library based on Solid. You can think about some similar services such as Goodreads and Audible personalized to your experience. To start you must implement a prototype first, a command-line interface application that only works for your user.

  ## The following user stories (US) will help:

    * US1: As a digital book library owner I want to be able to authenticate to my digital book library available on my Pod (see available resources to access your Pod).
    
    *  US2: As a digital book library owner I want to be able to add a book to my digital book library (on my Pod).

    * US3: As a digital book library owner I want to be able to edit the description of a book from my digital library (on my Pod).

    * US4: As a digital book library owner I want to be able to delete a book from my digital library (on my Pod).
    
    * US5: As a digital book library owner I want to be able to read the details of a digital book.

    A friend of yours has a list of really cool books. It is a collection of books compiled by all her closest friends. Your friend's list is also in a digital library on her Pod. She gives you access to that list in the form of an access grant (see available resources).

    * US6: As a digital book library owner I want to read the book list my friend shared with me and gave me access to.

    * US7: As a digital book library owner I want to write to my friend’s book list she gave me write access to, an additional book title.


## Vocabulary
```
@prefix vocabulary : <https://inrupt.github.io/solid-client-java/vocab/BookLibraryVocabulary#>

###
# Ontology metadata
###

<https://inrupt.github.io/solid-client-java/vocab/BookLibraryVocabulary> a owl:Ontology;
  swc:BaseUrl "https://inrupt.github.io/solid-client-java/vocab/BookLibraryVocabulary";
  swc:ResourceSeparator "#".

###
# Ontology classes
###

vocabulary:BookLibrary a owl:Class;
  rdfs:label "Book Library" .

vocabulary:Book a owl:Class;
  rdfs:label "Book" .

###
# Ontology relationships.
###

vocabulary:containsBook a owl:ObjectProperty;
  rdfs:domain vocabulary:BookLibrary
  rdfs:label "contains book"@en;
  rdfs:range vocabulary:Book ;
  owl:inverseOf vocabulary:bookInLibrary .

vocabulary:bookInLibrary a owl:ObjectProperty;
  rdfs:domain vocabulary:Book
  rdfs:label "book in library"@en;
  rdfs:range vocabulary:BookLibrary ;
  owl:inverseOf vocabulary:containsBook .

###
# Ontology attributes which are also relationships but range is rdfs:Resource or others.
###

vocabulary:author a owl:DatatypeProperty;
  rdfs:label "book author"@en;
  rdfs:domain vocabulary:Book;
  rdfs:range xsd:string .

vocabulary:description a owl:DatatypeProperty;
  rdfs:label "book description"@en;
  rdfs:domain vocabulary:Book;
  rdfs:range xsd:string .

```


## Data Model

`podStorage` is the root of a user's Pod. We choose to write the Book Library data on the Pod in a container called `bookLibrary`. Together the form the base for where the data is stored ona user's Pod: `podStorage/bookLibrary`

### The Book model

```
@prefix book : <{podStorage}/bookLibrary#> .
@prefix vocabulary : <https://inrupt.github.io/solid-client-java/vocab/BookLibraryVocabulary#>

<{podStorage}/bookLibrary> a vocabulary:BookLibrary .

book:uuid1 a vocabulary:Book ;
    dc:title "Dracula" ;
    vocabulary:author "Bram Stoker" .

book:uuid2 a vocabulary:Book;
  dc:title "Little Women" ;
  vocabulary:author "May Alcott" .

book:uuid3 a vocabulary:Book;
  dc:title "The graveyard book" ;
  vocabulary:author "Neil Gaiman" .

book:uuid4 a vocabulary:Book;
  dc:title "The alchemist" ;
  vocabulary:author "Paulo Coelho" .

```
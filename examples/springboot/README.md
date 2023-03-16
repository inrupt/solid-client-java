# Digital Book Library on a Pod

You are building the next-generation digital book library based on Solid. You can think about some similar services such as Goodreads and Audible personalized to your experience. To start you must implement a prototype first, a command-line interface application that only works for your user.

## How to get the example to run

The example web application expects as of now a `{podStorage}` link. Make sure you replace it in the `Vocabulary.java`.
Make sure you have a book library resource in the location `{podStorage}/MyBookLibrary/bookLibResource.ttl`. See example below at The book model section.

## The following user stories (US) will help:

    * US1: As a digital book library owner I want to see all the publicly available books I have in my Digital Book Library.
    * US2: As a digital book library owner I want to see all the books which have a specified title. 


## Vocabulary

```
@prefix vocabulary: <{podStorage}/MyBookLibrary/bookLibraryVocabulary#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

###
# Ontology metadata
###

<{podStorage}/MyBookLibrary/bookLibraryVocabulary.ttl> a owl:Ontology .

###
# Ontology classes
###

vocabulary:BookLibrary a owl:Class;
  rdfs:label "Book Library" .

vocabulary:Book a owl:Class;
  rdfs:label "Book" .

###
# Ontology relations
###

vocabulary:containsBook a owl:DatatypeProperty;
  rdfs:label "contains book"@en;
  rdfs:domain vocabulary:BookLibrary;
  rdfs:range vocabulary:Book .

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

`podStorage` is the root of a user's storage. We choose to write the Book Library data on the storage in a container called `bookLibrary`. Together the form the base for where the data is stored on a user's storage: `podStorage/bookLibrary`.
Data model design choice: by the Book being part of the BookLibrary resource directly we express implicitly that the Book is part of only this library. For integration into other libraries one can add the Book triples into the new BookLibrary resource or consider a vocabulary with a dedicated relation (ex: contains book) and dedicated resources per Book.

### The Book model

```
@prefix book: <{podStorage}/MyBookLibrary/bookLibResource.ttl#> .
@prefix vocabulary: <{podStorage}/MyBookLibrary/bookLibraryVocabulary#> .
@prefix dc: <http://purl.org/dc/elements/1.1/> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

<{podStorage}/MyBookLibrary/bookLibResource.ttl> a vocabulary:BookLibrary ;
    vocabulary:containsBook book:uuid1;
    vocabulary:containsBook book:uuid2;
    vocabulary:containsBook book:uuid3;
    vocabulary:containsBook book:uuid4.

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
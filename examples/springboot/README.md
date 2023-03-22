# Digital Book Library on a Pod

This example Web Application is a Digital Book Library based on Solid. It is based on [SpringBoot 3](https://spring.io/guides/gs/spring-boot/) and [Thymeleaf](https://www.thymeleaf.org/) for frontend.

## How to get the example to run

To run this application you need to have a Book Library Solid resource already somewhere on a Pod. That is all.
See an example below of how a Book Library Solid resource could look like.

## The following user stories (US) will help:

    * US1: As a digital book library owner I want to see all the available books I have in my Digital Book Library.
    * US2: As a digital book library owner I want to see all the books which have a specified word in the title. 
    * US3: As a digital book library owner I want to see all the books by a given author. 

## Data Model

This is an example Book Library Solid resource. It is important to have the same same vocabulary because it is hardcoded in the code. 
`podStorage` is the root of a user's storage. This is just an example and the book prefix can look however you'd like.
Data model design choice: the Book is part of the BookLibrary resource directly. We must express explicitly that the Book is part of the library by using a predicate like `vocabulary:containsBook`.
The Book Library uses a dedicated vocabulary which does not need to be deployed anywhere, but if you chose to deploy it, it can be on the same pod or another pod. Let's say it is on a different Pod and that `{vocabularyPodStorage}` is the root storage.

### The Book model

```
@prefix book: <{podStorage}/MyBookLibrary/bookLibResource.ttl#> .
@prefix vocabulary: <{vocabularyPodStorage}/MyBookLibrary/bookLibraryVocabulary#> .
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

## Vocabulary

The vocabulary expects a `{vocabularyPodStorage}` link. You can replace it in the `Vocabulary.java` if you deploy it on a Pod.

```
@prefix vocabulary: <{vocabularyPodStorage}/MyBookLibrary/bookLibraryVocabulary#> .
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

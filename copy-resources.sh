#!/bin/bash

for MODULE in api bom core httpclient jackson jena jsonb okhttp openid parser rdf4j uma vc vocabulary webid
do
    cp -R src/site/resources $MODULE/src/site/
done

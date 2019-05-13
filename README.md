Rana Nostra (our Frog)
======================

A web service that wraps the [Frog](https://languagemachines.github.io/frog/)
named entity recognizer.

Rana Nostra's main feature is that it can run XML files through Frog and add
the entity annotations to the XML while retaining the XML structure.


Getting started
---------------

Build and start using Docker:

    docker build -t rananostra -f docker/Dockerfile .
    docker run -t -p 8080:8080 rananostra

Query it:

    curl -H "Content-type: application/json" -XPOST http://localhost:8080/xml \
        -d '{"xml": "<p>Hallo, Henk!</p>", "xpath": "/p",
             "starttag": "start", "endtag": "end",
             "classattr": "type", "textattr": "text"}'

You should get the response

    <?xml version="1.0"?>
    <p>Hallo, <start type="per" text="Henk" />Henk<end />!</p>

For a more challenging example, try:

    curl -H "Content-type: application/json" -XPOST http://localhost:8080/xml \
        -d '{"xml": "<p>Hallo, Rana <lang l=\"latin\">Nostra!</lang></p>",
             "xpath": "/p", "starttag": "start", "endtag": "end", "classattr": "type"}'

This example shows why the entity annotations are stored as pairs of milestones
instead of regular XML elements.

(Frog may tell you that Rana Nostra is a person, but that is its assessment,
not ours.)


Developing
----------

Make sure you have Frog running in server mode. Pick some unused port number,
say, 9999, then run Frog using either

    port=9999
    frog --skip=mptcla -S ${port} -X

or, using LaMachine,

    port=9999
    docker pull proycon/lamachine
    docker run -t -p ${port}:9999 proycon/lamachine frog --skip=mptcla -S 9999 -X

Now build and run Rana Nostra:

    mvn clean package
    echo host: localhost > config.yml
    echo port: 9999     >> config.yml
    target/appassembler/bin/rananostra server config.yml 

To run the unit tests, make sure you have Frog available through a port on
localhost and set the environment variable `RANA_TEST_PORT`:

    RANA_TEST_PORT=9999 mvn clean test



Differences with Frog, CLAM, LaMachine, etc.
--------------------------------------------

Compared to vanilla Frog and its various service wrappers, Rana Nostra:

* does only named entity recognition, not parsing, expression finding, etc.;
* produces exact indices of the entity locations, or milestone tags in XML;
* uses the OpenNLP tokenizer instead of Frog's own (Ucto). This is because
  it seems to be impossible to get token indices out of Frog/Ucto, and we
  need those.


Legal matters
-------------

Copyright 2016-2019 Koninklijke Nederlandse Academie van Wetenschappen

Distributed under the terms of the GNU General Public License, version 3.
See the file COPYING for details.

The file ./src/main/resources/nl-token.bin is taken from [OpenNLP][opennlp],
which is Copyright 2017 The Apache Software Foundation.

[opennlp]: http://opennlp.sourceforge.net/models-1.5/

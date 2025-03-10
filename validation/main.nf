#!/usr/bin/env nextflow

process sayHello {
  input:
  val x
  output:
  stdout
  script:
  """
    python3 \$PWD/../../../hello.py "$x"   
  """
}

workflow {
  Channel.of('Bonjour', 'Ciao', 'Hello', 'Hola') | sayHello | view
}
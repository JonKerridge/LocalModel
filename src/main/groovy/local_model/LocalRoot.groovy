package local_model

import groovy_jcsp.ChannelInputList
import groovy_jcsp.ChannelOutputList
import jcsp.lang.Barrier
import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.lang.ChannelOutput

class LocalRoot implements CSProcess {

  // properties
  ChannelInput input
  ChannelOutput output
  ChannelOutputList toNodes
  ChannelInputList fromNodes
  Barrier syncReproduce
  int instances //number of different problems instances to be created from the same specification

  /**
   * This defines the actions of the process.*/
  @Override
  void run() {
    def addAscending = {
      List <LocalIndividual> population,
      List <LocalIndividual> toAdd ->
        int last
        last = population.size()
        toAdd.each { val ->
          int i
          boolean added
          i = 0
          added = false
          while ((i < last)&&(!added)){
            if (val.getFitness() < population[i].getFitness()){
              population.add(i, val)
              added = true
              population.remove(last)
            }
            else
              i = i + 1
          }
        }
      return population
    }
    def addDescending = {
      List <LocalIndividual> population,
      List <LocalIndividual> toAdd ->
        int last
        last = population.size()
        toAdd.each { val ->
          int i
          boolean added
          i = 0
          added = false
          while ((i < last)&&(!added)){
            if (val.getFitness() > population[i].getFitness()){
              population.add(i, val)
              added = true
              population.remove(last)
            }
            else
              i = i + 1
          }
        }
        return population
    }
    LocalIndividual result
    LocalProblemSpecification specification
    int totalPopulation, nodes, populationPerNode
    int replaceInterval, replaceNumber, maxGenerations
    boolean  minimise
    List evaluateData
    // now process each instance of the problem
    for ( i in 0 ..< instances) {
      specification = input.read() as LocalProblemSpecification
//      println "Root has read specification $i"
      output.write(specification)   // to the Local Collect Solution process every instance
      if (i == 0) {
//        println "Root processing specification 0"
        // obtain non-changing values from specification
        nodes = specification.nodes
        populationPerNode = specification.populationPerNode
        replaceInterval = specification.replaceInterval
        replaceNumber = specification.replaceNumber
        maxGenerations = specification.maxGenerations
        assert populationPerNode >= 2: "Population Per Node must be at least 2; $populationPerNode specified"
        minimise = specification.minOrMax == 'MIN'
        // determine population indices
        totalPopulation = populationPerNode * nodes
//        println "Root: fixed values $nodes, $populationPerNode, $minOrMax, $lastIndex, $totalIndex"
      } // end of initialisation
      Class populationClass = Class.forName(specification.populationClass)
//      Class individualClass = Class.forName(specification.individualClass)
      LocalPopulation populationData = populationClass.newInstance(
          totalPopulation,  // the number of individuals that are created
          specification.geneLength,
          specification.crossoverPoints,
          specification.convergenceLimit,
          specification.crossoverProbability,
          specification.mutationProbability
      ) as LocalPopulation
      // read in the data file to evaluateData iff first iteration
      if (i == 0) evaluateData = populationData.processDataFile(specification.dataFileName) // extract evaluationData from file
      populationData.population = []  // set population to empty list
      (0 ..< nodes).each {
        toNodes[it].write([specification,  populationData, evaluateData])
        // this will automatically cause the creation of a list of constructed individuals
      }
      // the responses will result in population comprising nodes instances of a sorted sublist
      for ( n in 0 ..< nodes) populationData.population = populationData.population + fromNodes[n].read() as List<LocalIndividual>
//      println "Initial Population $populationData.population"
//      populationData.population.each{println "$it"}
      // now interact with nodes until convergence
      boolean converged, generationsExceeded
      def currentBestFitness
      int replaceCount, generation, replacements
      replaceCount = 0
      generation = 0
      replacements = 0
      converged = false
      generationsExceeded = false
      currentBestFitness = populationData.population[0].getFitness()
      while (!generationsExceeded && !converged){ // both false initially
        if ( currentBestFitness == populationData.population[0].getFitness()) replaceCount++
        if (replaceCount == replaceInterval){
          (0 ..< nodes).each { toNodes[it].write(['REPLACE', replaceNumber]) }
          (0 ..< nodes).each {
            List <LocalIndividual> response = fromNodes[it].read() as List<LocalIndividual>
            // the responses are sorted
            if (minimise)
              populationData.population = addAscending(populationData.population, response)
            else
              populationData.population = addDescending(populationData.population, response)
          }
          replacements++
          replaceCount = 0
        }// end of replaceCount test
        // now do the reproduction
        syncReproduce.reset(nodes+1)
        (0 ..< nodes).each { toNodes[it].write(['REPRODUCE', totalPopulation]) }
        syncReproduce.sync()
        // population has shared read access; sync ensures that all shared access is complete
        // before any responses are read and could be processed, thereby changing the
        // underlying population for any nodes still doing their reproduction processing

        (0 ..< nodes).each {
          List<LocalIndividual> response = fromNodes[it].read() as List<LocalIndividual>
          // the responses are sorted on fitness and the converged property will be set true
          // for any response that satisfies the convergence limit criterion
          // only need to check that the first response has converged
          if (response[0].converged) {
            result = response[0]
            converged = true
//            println "Convergence at generation $generation"
          }
          else {
            if (minimise)
              populationData.population = addAscending(populationData.population, response)
            else
              populationData.population = addDescending(populationData.population, response)
          }
        } // reading responses
        generation = generation + 1
        if (generation == maxGenerations) generationsExceeded = true
      } //end of while loop
//      println "Final Population $populationData.population in $generation generations"
      if (converged){
        output.write([specification.seeds[0], result, generation, replacements])
      }
      else {
        result = populationData.population[0]
        output.write([specification.seeds[0], result, generation, replacements])
      }
      (0 ..< nodes).each { toNodes[it].write(['FINISH', 0])}
    } // end for instances
  } // end run()
}

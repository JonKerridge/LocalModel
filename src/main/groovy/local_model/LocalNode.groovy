package local_model

import jcsp.lang.Barrier
import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.lang.ChannelOutput

class LocalNode implements CSProcess{

  ChannelInput fromRoot
  ChannelOutput toRoot
  int nodeID, instances
  Barrier syncReproduce

  /**
   * This defines the actions of the process.*/
  @Override
  void run() {
    int ppn, geneLength
    def convergenceLimit
    Random rng
    boolean minimise
    for (i in 0..<instances) {
      List inputList = fromRoot.read() as List
      LocalProblemSpecification problem = inputList[0] as LocalProblemSpecification
      LocalPopulation nodePopulation = inputList[1] as LocalPopulation
      List evaluateData = inputList[2] as List
      assert i == problem.instance: "instance being processed and loop control do not match node $nodeID"
      ppn = problem.populationPerNode
      rng = new Random(problem.seeds[nodeID])
      geneLength = problem.geneLength
      convergenceLimit = problem.convergenceLimit
      minimise = problem.minOrMax == "MIN"
      Class individualClass = Class.forName(problem.individualClass)
      List <LocalIndividual> newIndividuals
      def addAscIndiv ={
        // adds newIndividual to newIndividuals s.t. fitness values are in ascending order
        LocalIndividual newIndividual ->
        if (newIndividuals.empty)
          newIndividuals << newIndividual
        else {
          int last = newIndividuals.size()
          int p = 0
          while ( (p < last)  &&(newIndividual.getFitness() > newIndividuals[p].getFitness()) ) p++
          if (p == last)
            newIndividuals << newIndividual
          else
            newIndividuals.add(p, newIndividual)
        }
      } // addAscIndiv
      def addDescIndiv ={
          // adds newIndividual to newIndividuals s.t. fitness values are in descending order
        LocalIndividual newIndividual ->
          if (newIndividuals.empty)
            newIndividuals << newIndividual
          else {
            int last = newIndividuals.size()
            int p = 0
            while ( (p < last)  &&(newIndividual.getFitness() < newIndividuals[p].getFitness()) ) p++
            if (p == last)
              newIndividuals << newIndividual
            else
              newIndividuals.add(p, newIndividual)
          }
      } // addDescIndiv
      newIndividuals = []
      for ( p in 0 ..< ppn) {
        LocalIndividual individual = individualClass.newInstance(geneLength, rng) as LocalIndividual
        individual.evaluateFitness( evaluateData )
        individual.determineConvergence(convergenceLimit)
        if (minimise)
          addAscIndiv(individual)
        else
          addDescIndiv(individual)
      }  // ppn loop
      // node has created a sorted sublist of individuals to send to root
      toRoot.write(newIndividuals)
      boolean finished
      finished = false
      while (!finished){
        List requiredAction = fromRoot.read() as List
        String action = requiredAction[0]
        int number = requiredAction[1] as Integer
        switch (action){
          case 'REPLACE' :
            // number is the number of replacement individuals to create
            newIndividuals = []
            for ( p in 0 ..< number) {
              LocalIndividual individual = individualClass.newInstance(geneLength, rng) as LocalIndividual
              individual.evaluateFitness( evaluateData )
              individual.determineConvergence(convergenceLimit)
              if (minimise)
                addAscIndiv(individual)
              else
                addDescIndiv(individual)
            }  // number loop
            toRoot.write(newIndividuals)
            break
          case 'REPRODUCE':
            // number indicates the total population size used to identify parents
            int parent1, parent2
            newIndividuals = []
            parent1 = rng.nextInt(number)
            parent2 = rng.nextInt(number)
            while (parent2 == parent1) parent2 = rng.nextInt(number)
            newIndividuals  = nodePopulation.reproduce(parent1, parent2, rng)
            syncReproduce.sync()
            toRoot.write(newIndividuals)
            break
          case 'FINISH':
            finished = true
        } // switch
      }// finished loop
    } // instances

  } // run()
}

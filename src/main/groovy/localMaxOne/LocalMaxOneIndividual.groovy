package localMaxOne

import local_model.LocalIndividual

class LocalMaxOneIndividual implements LocalIndividual{
  int fitness    // this can be any comparable type
  List chromosome
  int geneLength
  boolean converged

  LocalMaxOneIndividual (int geneLength, Random rng){
    chromosome = []
    this.geneLength = geneLength
    fitness = 0
    converged = false
    for (g in 0 ..< geneLength)
      chromosome << (Integer) rng.nextInt(2)
  }

  LocalMaxOneIndividual(int geneLength){
    chromosome = []
    this.geneLength = geneLength
    fitness = 0
    converged = false
    for (g in 0 ..< geneLength)
      chromosome << 0
  }
  /**
   *
   * calculates the fitness value(s) of an individual and directly updates the fitness variable
   *
   * @param evaluateData contains any data required to calculate the fitness function and is null if not required
   */
  @Override
  void evaluateFitness(List evaluateData) {
    fitness = 0
    for ( g in 0 ..< geneLength) {
      fitness = fitness + chromosome[g]
    }
  }

  /**
   * undertakes the mutation operation of this individual
   *
   * @param rng the Random number generator used by the node to which
   * this individual belongs
   */
  @Override
  void mutate(Random rng) {
    int subscript = rng.nextInt(geneLength)
    chromosome[subscript] = 1 - chromosome[subscript]
  }

  /**
   * checks the fitness value of the individual and if it satisfies the convergenceLimit sets
   * converged true, false otherwise.  The value of minimise is used to determine the nature
   * of the test required.  For a minimisation problem minimise is true.
   *
   * @param convergenceLimit
   */
  @Override
  void determineConvergence(Object convergenceLimit) {
    if ( fitness == convergenceLimit as Integer) {
      this.converged = true
//      println "Convergence found"
    }
  }

  /**
   * @return the fitness value of an Individual
   */
  @Override
  Object getFitness() {
    return fitness
  }

  String toString(){
    String conv = "F"
    if (converged) conv = "T"
//    return "[ $conv,$fitness:$chromosome ]"
    return "[ $conv,$fitness ]"
  }
}

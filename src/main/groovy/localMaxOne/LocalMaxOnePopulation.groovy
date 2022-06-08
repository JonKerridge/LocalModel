package localMaxOne

import local_model.LocalIndividual
import local_model.LocalPopulation

class LocalMaxOnePopulation implements LocalPopulation{

  int individuals   // active population
  int geneLength    // the length of each individuals chromosome
  int crossoverPoints // the EVEN number of points used to break the chromosome when doing reproduction
  int convergenceLimit  // required fitness value for convergence
  double crossoverProbability // the probability that reproduction will lead to an acutal crossover operation
  double mutateProbability  // the probability that mutation will occur after crossover

  List <LocalMaxOneIndividual> population // to hold the list of individuals that form the population
  List evaluateData   // the data structure used to hold the fitness evaluation data if required

  LocalMaxOnePopulation (
      int individuals,
      int geneLength,
      int crossoverPoints,
      int convergenceLimit,
      double crossoverProbability,
      double mutateProbability){
    this.geneLength = geneLength
    this.individuals = individuals
    this.crossoverPoints = crossoverPoints
    this.convergenceLimit = convergenceLimit
    this.crossoverProbability = crossoverProbability
    this.mutateProbability = mutateProbability
    population = []
    evaluateData = []
  }

  /** reproduce creates the children resulting from a crossover operation
   * The parameters are all subscripts of individuals in the population List
   * @param parent1
   * @param parent2
   * @param minimise indicates the order required in return list
   * @param rng the random number generator to be used, specific to each node
   * @return List of the two children created by the method in sorted order
   *
   */
  @Override
  List<LocalIndividual> reproduce(
      int parent1,
      int parent2,
      Random rng) {
    LocalMaxOneIndividual child1, child2
    List <LocalMaxOneIndividual> result
    result = []
    child1 = new LocalMaxOneIndividual(geneLength)
    child2 = new LocalMaxOneIndividual(geneLength)
    // this uses a single point crossover for MaxOnes
    int crossoverPoint = rng.nextInt(geneLength)
    if ( rng.nextDouble() < crossoverProbability){
      // doing the crossover
      for ( p in 0 ..< crossoverPoint){
        child1.chromosome[p] = population[parent1].chromosome[p]
        child2.chromosome[p] = population[parent2].chromosome[p]
      }
      for ( p in crossoverPoint ..< geneLength){
        child1.chromosome[p] = population[parent2].chromosome[p]
        child2.chromosome[p] = population[parent1].chromosome[p]
      }
      // now see if we undertake a mutation on each child
      if (rng.nextDouble() < mutateProbability)
        child1.mutate(rng)
      if (rng.nextDouble() < mutateProbability)
        child2.mutate(rng)
      child1.evaluateFitness(evaluateData)
      child2.evaluateFitness(evaluateData)
      child1.determineConvergence(convergenceLimit)
      child2.determineConvergence(convergenceLimit)
      // now order the children and return
      if (child1.getFitness() > child2.getFitness())
        result = [child1, child2]
      else
        result = [child2, child1]
//      println "Reproduce outcome $result"
      return result
    }
  }

  /**
   * bestSolution is used to find the individual that has the best solution once the
   * maximum number of generations has been exceeded.  It does not require knowledge of
   * the convergence criteria as it is based solely on the relative values of Individual.fitness
   * @return the individual that has the best solution within maxGenerations
   */
  @Override
  LocalIndividual bestSolution() {
    return null
  }

  /**
   * processDataFile used to read content of file with name dataFilename
   * and place them into the List object evaluateData within population used in the
   * Individual evaluateFitness method
   *
   * @param dataFileName the name of the file containing the evaluation data or null
   * @return a List containing the evaluation data
   */
  @Override
  List processDataFile(String dataFileName) {
    return null
  }
}

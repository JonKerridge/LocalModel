package local_model


/**
 * Any Individual using the Local Model Engine uses this interface to specify
 * the methods an Individual class must implement.
 */
interface LocalIndividual {
  /**
   * Any Individual using the Locality Model Engine uses this interface to specify
   * the methods an Individual class must implement.  <br>
   *
   * Constructors that create an  individual are required:
   *
   * localIndividual( int geneLength, Random rng)
   * where geneLength is the number of genes in a chromosome
   * and rng is the random number generator to be used in creating the chromosome
   *
   * localIndividual(int geneLength) to create an empty chromosome
   *
   * properties of the class are:
   * fitness is the current value of the fitness function applied to this individual
   * chromosome is the set of values that make up the individuals data points
   * geneLength is the number of elements in the chromosome
   * converged is true if a solution is found that has satisfied converges, false otherwise

   def fitness    // this can be any comparable type
   List chromosome
   int geneLength
   boolean converged
   */

  def fitness    // this can be any comparable type
  List chromosome
  int geneLength
  boolean converged


  /**
   *
   * calculates the fitness value(s) of an individual and directly updates the fitness variable
   *
   * @param evaluateData contains any data required to calculate the fitness function and is null if not required
   */
  void evaluateFitness(List evaluateData)
  /**
   * undertakes the mutation operation of this individual
   *
   * @param rng the Random number generator used by the node to which
   * this individual belongs
   */
  void mutate(Random rng)

  /**
   * checks the fitness value of the individual and if it satisfies the convergenceLimit sets
   * converged true, false otherwise.
   *
   * @param convergenceLimit
   *
   */
  void determineConvergence (def convergenceLimit)

  /**
   * @return the fitness value of an Individual
   */
  Object getFitness()

  /**
   * It is recommended that a String toString() method is supplied
   */
}
  
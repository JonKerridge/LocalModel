package localMaxOne

import local_model.LocalEngine
import local_model.LocalProblemSpecification

def problem = new LocalProblemSpecification()

int nodes = 16  // 4 8 12 16
int populationPerNode = 6  // 4 6 8
int geneLength = 512
int instances = 11
boolean doSeedModify = true

problem.minOrMax = "MAX"
problem.instances = instances
problem.nodes = nodes
problem.geneLength = geneLength
problem.maxGenerations = 4000
problem.replaceInterval = 4
problem.replaceNumber = 2
problem.crossoverPoints = 1
problem.crossoverProbability = 1.0
problem.mutationProbability = 0.1
problem.dataFileName = null
problem.convergenceLimit = geneLength
problem.populationPerNode = populationPerNode
problem.doSeedModify = doSeedModify
problem.populationClass = LocalMaxOnePopulation.getName()
problem.individualClass = LocalMaxOneIndividual.getName()
problem.seeds = [3, 211, 419, 631, 839, 1039, 1249, 1451,
                 1657, 1861, 2063, 4073, 6079, 8081, 10091, 10301,
                 10487, 10687, 10883, 11083, 11273, 11471, 11689, 11867,
                 12043, 12241, 122412, 12583, 12763, 12959, 13147, 13331]

String outFile = "./localMaxOneSingle.csv"
def fw = new FileWriter(outFile, true)
def bw = new BufferedWriter(fw)
def printWriter = new PrintWriter(bw)

def localEngine = new LocalEngine(problemSpecification: problem,
    nodes: problem.nodes,
    instances: instances,
    printWriter: printWriter)
localEngine.run()
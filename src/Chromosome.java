/**
 * Created by armandmaree on 2016/05/29.
 */
public class Chromosome {
	private Gene[] genes;
	private int numGenes;
	private long fitness = 0;

	public Chromosome(int numGenes) {
		this.numGenes = numGenes;
		genes = new Gene[numGenes];
	}

	public void setGenes(Gene[] genes) {
		this.genes = genes;
	}

	public Gene[] getGenes() {
		return genes;
	}

	public int getNumGenes() {
		return numGenes;
	}

	public void setNumGenes(int numGenes) {
		this.numGenes = numGenes;
	}

	public void setGene(int index, Gene gene) {
		genes[index] = gene;
	}

	public Gene getGene(int index) {
		return genes[index];
	}

	public int getColor() {
		int color = 0;
		int numRead = 0;

		for (int i = 0; i < numGenes; i++) {
			color = (color * numRead++ + genes[i].getImageColor()) / numRead;
		}

		return color;
	}

	public long getFitness() {
		return fitness;
	}

	public void setFitness(long fitness) {
		this.fitness = fitness;
	}

	@Override
	public String toString() {
		String val = "";
		val += "[";

//		for (int i = 0; i < numGenes - 1; i++) {
//			val += genes[i] + ",\n";
//		}

		return val + genes[numGenes - 1] + "]";

	}
}

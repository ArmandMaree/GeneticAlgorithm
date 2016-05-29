import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by armandmaree on 2016/05/29.
 */
public class GeneticAlgorithm {
	private String imageDBPath;
	private int numRows;
	private int numCols;
	private int numPixelsRow;
	private int numPixelsCol;
	private BufferedImage inputImage;
	private Raster rasterImage;
	private int[][] cellColor;
	private Chromosome[] chromosomes;
	private int numChromosomes = 10;
	private long minMSE;
	private int mutateProbability = 20;
	private int crossOverProbability = 90;
	private int tournamentSize = 4;
	private int type;
	private ArrayList<String> images = new ArrayList<>();
	Random random = new Random(System.currentTimeMillis());

	public GeneticAlgorithm(String imageDBPath, String inputImagePath, int numRows, int numCols) {
		this.imageDBPath = imageDBPath;

		try {
			inputImage = ImageIO.read(new File(inputImagePath));
			// TODO add grey scale functionality
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		rasterImage = inputImage.getRaster();
		type = inputImage.getType();
		this.numRows = numRows;
		this.numCols = numCols;

		minMSE = 100 * numCols * numRows;

		numPixelsRow = rasterImage.getHeight() / numRows;
		numPixelsCol = rasterImage.getWidth() / numCols;

		cellColor = new int[numRows][numCols];

		// get average color of each cell in the input image
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				cellColor[i][j] = getCellColor(inputImage, i, j, numPixelsRow, numPixelsCol);
			}
		}

		chromosomes = new Chromosome[numChromosomes];

		for (int i = 0; i < numChromosomes; i++) {
			chromosomes[i] = new Chromosome(numRows * numCols);
		}

		if (numChromosomes * 0.1 > tournamentSize)
			tournamentSize = (int)(numChromosomes * 0.1);
	}

	private int getCellColor(BufferedImage img, int row, int col, int rowDist, int colDist) {
		int color = 0;
		int numRead = 0;
		Raster raster = inputImage.getRaster();
		int maxRow = raster.getHeight() - 1;
		int maxCol = raster.getWidth() - 1;

		for (int i = row * rowDist; i < (row + 1) * rowDist; i++) {
			for (int j = col * colDist; j < (col + 1) * colDist; j++) {
				if (i > maxRow)
					i = maxRow;

				if (j > maxCol)
					j = maxCol;

				color = (color * numRead++ + img.getRGB(j, i)) / numRead;
			}
		}

		return color;
	}

	public void start() {
		readImages();
		init();
		sortChromosomes(chromosomes);

		int solution = -1;
		int genCounter = 0;

		while (solution == -1) {
			genCounter++;
			int best = 0;

			for (int i = 0; i < numChromosomes; i++) {
				if (chromosomes[i].getFitness() <= minMSE) {
					solution = i;
					break;
				}

				if (chromosomes[i].getFitness() < chromosomes[best].getFitness())
					best = i;
			}

			drawChromosome(best, "" + (genCounter));

			Chromosome[] newGeneration = new Chromosome[numChromosomes];
			int numNew = 0;

			while (numNew < numChromosomes) {
				System.out.printf("\rSolution generation " + (genCounter) + ". Best: " + chromosomes[best].getFitness() + ". GenerationProgress: " + (int)(((double)numNew)/numChromosomes*100) + "%%");
				Chromosome[] offspring = getOffspring();

				if (offspring == null)
					continue;

				newGeneration[numNew++] = offspring[0];

				for (int i = 0; i < newGeneration[numNew - 1].getNumGenes(); i++) {
					if (random.nextInt() % 100 < mutateProbability) {
						newGeneration[numNew - 1].setGene(i, mutate());
						calcFitness(newGeneration[numNew - 1]);
					}
				}

				newGeneration[numNew++] = offspring[1];

				for (int i = 0; i < newGeneration[numNew - 1].getNumGenes(); i++) {
					if (random.nextInt() % 100 < mutateProbability) {
						newGeneration[numNew - 1].setGene(i, mutate());
						calcFitness(newGeneration[numNew - 1]);
					}
				}
			}

			if (newGeneration.length == 0)
				continue;

			sortChromosomes(newGeneration);

			Chromosome[] successors = new Chromosome[numChromosomes];
			int newIndex = 0;
			int oldIndex = 0;

			for (int i = 0; i < numChromosomes; i++) {
				if (newGeneration[newIndex].getFitness() < chromosomes[oldIndex].getFitness())
					successors[i] = newGeneration[newIndex++];
				else
					successors[i] = chromosomes[oldIndex++];
			}

			chromosomes = successors;
		}

		System.out.println("\rSolution found:                                ");

		for (int i = 0; i < chromosomes[solution].getNumGenes(); i++) {
			System.out.println(chromosomes[solution].getGene(i).getImageName());
		}
	}

	private void readImages() {
		System.out.printf("Reading images...");
		readImages(new File(imageDBPath));
		System.out.println("\r" + images.size() + " images found.");
	}

	private void readImages(File folder) {
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				images.add(listOfFiles[i].getAbsolutePath());
			} else if (listOfFiles[i].isDirectory()) {
				readImages(listOfFiles[i]);
			}
		}
	}

	private void drawChromosome(int c, String outName) {
		BufferedImage finalImg = new BufferedImage(numCols * numPixelsCol, numRows * numPixelsRow, type);

		int num = 0;
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				BufferedImage tmp = null;

				try {
					tmp = ImageIO.read(new File(chromosomes[c].getGene(num).getImageName()));
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(0);
				}

				finalImg.createGraphics().drawImage(tmp, numPixelsCol * j, numPixelsRow * i, null);
				num++;
			}
		}

		try {
			ImageIO.write(finalImg, "jpeg", new File("output/" + outName + ".jpg"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

	}

	private void sortChromosomes(Chromosome[] c) {
		boolean changed = true;

		for (int i = 0; i < c.length - 1 && changed; i++) {
			changed = false;

			for (int j = i + 1; j < c.length; j++) {
				if (c[i].getFitness() > c[j].getFitness()) {
					Chromosome tmp = c[i];
					c[i] = c[j];
					c[j] = tmp;
					changed = true;
				}
			}
		}
	}

	// initialize all chromosomes with random genes
	private void init() {
		for (int i = 0; i < numChromosomes; i++) {
			System.out.printf("\rInitializing chromosome " + (i + 1) + " of " + numChromosomes + ".");
			Gene[] genes = new Gene[chromosomes[i].getNumGenes()];

			for (int j = 0; j < chromosomes[i].getNumGenes(); j++) {
				genes[j] = mutate();
			}

			chromosomes[i].setGenes(genes);
			calcFitness(chromosomes[i]);
		}

		System.out.println("\rInitialization done.                                                         ");
	}

	// assign random image to a gene and return gene
	private Gene mutate() {
		int randomImage = random.nextInt() % images.size();

		if (randomImage < 0)
			randomImage += images.size();

		return new Gene(images.get(randomImage));
	}

	private void calcFitness(Chromosome c) {
		long dist = 0;

		for (int i = 0; i < c.getGenes().length; i++) {
			dist += Math.pow(cellColor[i / numRows][i % numCols] - c.getGene(i).getImageColor(), 2);
		}

		c.setFitness(dist);
	}

	private Chromosome[] getOffspring() {
		int[] parents = getParents();
		int crossOver = random.nextInt() % 100;

		if (crossOver < 0)
			crossOver += 100;

		if (crossOver < crossOverProbability) {
			int numGenes = chromosomes[parents[0]].getNumGenes();
			int firstPoint = random.nextInt() % numGenes;

			if (firstPoint < 0)
				firstPoint += numGenes;

			int secondPoint = random.nextInt() % numGenes;

			if (secondPoint < 0)
				secondPoint += numGenes;

			if (firstPoint > secondPoint) {
				int tmp = firstPoint;
				firstPoint = secondPoint;
				secondPoint = tmp;
			}

			Chromosome[] children = new Chromosome[2];
			children[0] = new Chromosome(numGenes);
			children[1] = new Chromosome(numGenes);

			for (int i = 0; i < firstPoint; i++) {
				try {
					children[0].setGene(i, chromosomes[parents[0]].getGene(i));
				}
				catch (Exception e) {
					e.printStackTrace();
					System.out.println("ERROR: " + i + "  " + parents[0] + "  " + chromosomes[parents[0]]);
					System.exit(0);
				}
				children[1].setGene(i, chromosomes[parents[1]].getGene(i));
			}

			for (int i = firstPoint; i < secondPoint; i++) {
				children[0].setGene(i, chromosomes[parents[1]].getGene(i));
				children[1].setGene(i, chromosomes[parents[0]].getGene(i));
			}

			for (int i = secondPoint; i < numGenes; i++) {
				children[0].setGene(i, chromosomes[parents[0]].getGene(i));
				children[1].setGene(i, chromosomes[parents[1]].getGene(i));
			}

			return children;
		}
		else
			return null;
	}

	private int[] getParents() {
		int[] competitors = new int[tournamentSize];
		int numCompetitors = 0;

		while (numCompetitors < tournamentSize) {
			int potentialCompetitor = random.nextInt() % numChromosomes;

			if (potentialCompetitor < 0)
				potentialCompetitor += numChromosomes;

			boolean notInTournament = true;

			for (int i = 0; i < competitors.length; i++) {
				if (competitors[i] == potentialCompetitor) {
					notInTournament = false;
					break;
				}
			}

			if (notInTournament)
				competitors[numCompetitors++] = potentialCompetitor;
		}

		int[] parents = {competitors[0], competitors[1]};

		if (chromosomes[parents[0]].getFitness() > chromosomes[parents[1]].getFitness()) {
			int tmp = parents[0];
			parents[0] = parents[1];
			parents[1] = tmp;
		}

		for (int i = 2; i < competitors.length; i++) {
			if (chromosomes[competitors[i]].getFitness() < chromosomes[parents[0]].getFitness()) {
				parents[1] = parents[0];
				parents[0] = i;
			}
			else if (chromosomes[competitors[i]].getFitness() < chromosomes[parents[1]].getFitness())
				parents[1] = i;
		}

		return parents;
	}
}

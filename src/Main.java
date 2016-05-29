
public class Main {

	public static void usage() {
		System.out.println("Main <dir or database> <image name> <num rows> <num cols>");
	}

    public static void main(String[] args) {
		if (args.length != 4) {
			usage();
			return;
		}

		GeneticAlgorithm ga = new GeneticAlgorithm(args[0], args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
		ga.start();
	}
}

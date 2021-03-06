package ca.mcgill.mcb.pcingola.binseq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.MarkerSeq;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.tree.IntervalForest;
import ca.mcgill.mcb.pcingola.interval.tree.IntervalTree;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * This class stores all "relevant" sequences in a genome
 *
 * This class is able to:
 * 		i) Add all regions of interest
 * 		ii) Store genomic sequences for those regions of interest
 * 		iii) Retrieve genomic sequences by interval
 *
 *
 * @author pcingola
 */
public class GenomicSequences implements Iterable<MarkerSeq>, Serializable {

    	private static final long serialVersionUID = 2339867422366567569L;

    	public static final int MAX_ITERATIONS = 1000000;

	public static boolean debug = false;
	public static boolean verbose = false;

	boolean disableLoad = false; // Do not load sequences from disk. Used minly for test cases
	Genome genome; // Reference genome
	IntervalForest intervalForest; // This is an interval forest of 'MarkerSeq' (genomic markers that have sequences)

	public GenomicSequences(Genome genome) {
		this.genome = genome;
		intervalForest = new IntervalForest();
	}

	/**
	 * Create a sequence for the whole chromsome (mostly used in test cases)
	 */
	public void addChromosomeSequence(String chr, String chrSeq) {
		MarkerSeq ms = new MarkerSeq(genome.getOrCreateChromosome(chr), 0, chrSeq.length() - 1, chrSeq);
		intervalForest.add(ms);
	}

	/**
	 * Add sequences from genome's exons
	 */
	boolean addExonSequences(String chr) {
		if (verbose) Timer.showStdErr("Creating sequences from exon information '" + chr + "'");
		IntervalTree tree = intervalForest.getOrCreateTree(chr);

		// Add all exon sequences. Collapse them if possible
		Markers exonMarkers = exonMarkers(chr);
		if (debug) Gpr.debug("Before union: " + exonMarkers.size());
		exonMarkers = exonMarkers.union();
		if (debug) Gpr.debug("After union: " + exonMarkers.size());
		tree.add(exonMarkers);

		// Build tree
		if (verbose) Timer.showStdErr("Building sequence tree for chromosome '" + chr + "'");
		tree.build();
		if (verbose) Timer.showStdErr("Done. Loaded " + tree.getIntervals().size() + " sequences.");

		return !tree.isEmpty();
	}

	/**
	 * Add sequences for each gene in the genome
	 */
	public int addGeneSequences(String chr, String chrSeq) {
		int seqsAdded = 0;

		// Get all genes in this chromosome
		Markers markers = genesMarkers(chr, chrSeq.length());

		// Merge (collapse) overlapping markers
		markers = markers.merge();

		// Find and add sequences for all markers
		for (Marker genes : markers) {
			if (!genes.getChromosomeName().equalsIgnoreCase(chr)) continue; // Different chromosome? => Skip

			int ssStart = genes.getStart();
			int ssEnd = genes.getEnd() + 1; // String.substring does not include the last character in the interval (so we have to add 1)

			if ((ssStart < 0) || (ssEnd > chrSeq.length())) {
				System.err.println("Ignoring gene outside chromosome range (chromo length: " + chrSeq.length() + "). Sequence (merged genes): " + genes.toStr());
			} else {
				try {
					String seq = chrSeq.substring(ssStart, ssEnd).toUpperCase();
					seqsAdded++;

					// Create a marker sequence and add it to interval forest
					MarkerSeq m = new MarkerSeq(genes.getChromosome(), genes.getStart(), genes.getEnd(), false, genes.getChromosomeName() + ":" + genes.getStart() + "-" + genes.getEnd());
					m.setSequence(seq);
					intervalForest.add(m);
				} catch (Throwable t) {
					t.printStackTrace();
					throw new RuntimeException("Error trying to add sequence for gene:\n\tChromosome sequence length: " + chrSeq.length() + "\n\tGene: " + genes.toStr());
				}
			}
		}

		return seqsAdded;
	}

	public void build() {
		intervalForest.build();
	}

	/**
	 * List of all exons
	 */
	Markers exonMarkers(String chr) {
		Markers markers = new Markers();

		// Add exons sequences
		for (Gene g : genome.getGenes()) {
			if (g.getChromosomeName().equals(chr)) {
				for (Transcript tr : g)
					for (Exon ex : tr) {
						String seq = ex.getSequence();

						// Only add exons that have full sequences
						if (seq != null && seq.length() >= ex.size()) {

							if (ex.isStrandPlus()) {
								markers.add(ex);
							} else {
								// We must reverse complement the sequence, since it's on the other strand
								Exon exRwc = (Exon) ex.clone();
								exRwc.setSequence(GprSeq.reverseWc(ex.getSequence()));
								markers.add(exRwc);
							}
						}
					}
			}
		}

		return markers;
	}

	/**
	 * Create a list of markers
	 */
	Markers genesMarkers(String chr, int chrLen) {
		Markers markers = new Markers();
		for (Gene gene : genome.getGenes()) {
			if (!gene.getChromosomeName().equalsIgnoreCase(chr)) continue; // Different chromosome? => Skip

			int ssStart = gene.getStart();
			int ssEnd = gene.getEnd() + 1; // String.substring does not include the last character in the interval (so we have to add 1)

			if ((ssStart < 0) || (ssEnd > chrLen)) {
				System.err.println("Ignoring gene outside chromosome range (chromo length: " + chrLen + "). Gene: " + gene.toStr());
			} else {
				try {
					// Create a marker sequence and add it to interval forest
					MarkerSeq m = new MarkerSeq(gene.getChromosome(), gene.getStart(), gene.getEnd(), false, gene.getId());
					markers.add(m);
				} catch (Throwable t) {
					t.printStackTrace();
					throw new RuntimeException("Error trying to add sequence for gene:\n\tChromosome sequence length: " + chrLen + "\n\tGene: " + gene.toStr());
				}
			}
		}

		return markers;
	}

	/**
	 * Do we have sequence information for this chromosome?
	 */
	public boolean hasChromosome(String chr) {
		if (!intervalForest.hasTree(chr)) return false;

		// Tried to load tree and it's empty?
		IntervalTree tree = intervalForest.getTree(chr);
		if (tree != null && tree.isEmpty()) return false; // Tree is empty, means we could not load any sequence from 'database'

		return true;
	}

	public boolean isEmpty() {
		for (IntervalTree tree : intervalForest)
			if (!tree.getIntervals().isEmpty()) return false;

		return true;
	}

	@Override
	public Iterator<MarkerSeq> iterator() {
		ArrayList<MarkerSeq> all = new ArrayList<MarkerSeq>();

		for (IntervalTree tree : intervalForest)
			for (Marker m : tree.getIntervals())
				all.add((MarkerSeq) m);

		return all.iterator();
	}

	/**
	 * Load sequences from databases
	 */
	public synchronized boolean load(String chr) {
		// Already loaded?
		if (hasChromosome(chr)) return true;
		if (disableLoad) return false; // Loading form database disabled?

		// File does not exists?  Cannot load...
		String fileName = Config.get().getFileNameSequence(chr);
		if (!Gpr.exists(fileName)) {
			if (Config.get().isDebug()) Timer.showStdErr("Attempting to load sequences for chromosome '" + chr + "' from file '" + fileName + "' failed, nothing done.");
			return false;
		}

		// Load markers
		if (verbose) Timer.showStdErr("Loading sequences for chromosome '" + chr + "' from file '" + fileName + "'");
		IntervalTree tree = intervalForest.getOrCreateTree(chr);
		tree.load(fileName, genome);
		if (verbose) Timer.showStdErr("Building sequence tree for chromosome '" + chr + "'");
		tree.build();
		if (verbose) Timer.showStdErr("Done. Loaded " + tree.getIntervals().size() + " sequences.");
		return !tree.isEmpty();
	}

	/**
	 * Load sequences from genomic sequence file or (if not file is available) generate some sequences from exons.
	 */
	public synchronized boolean loadOrCreateFromGenome(String chr) {
		if (hasChromosome(chr)) return true;
		if (load(chr)) return true;
		return addExonSequences(chr);
	}

	/**
	 * Find a marker (with sequence) containing query 'marker'
	 * Could trigger loading sequences form database
	 *
	 * @return A markerSeq containing 'marker' or null if nothing is found
	 */
	public synchronized MarkerSeq queryMarkerSequence(Marker marker) {
		String chr = marker.getChromosomeName();

		// Get or load interval tree
		if (!intervalForest.hasTree(chr)) loadOrCreateFromGenome(chr);
		IntervalTree tree = intervalForest.getTree(chr);

		// Nothing available
		if (tree == null || tree.isEmpty()) return null;

		// Find marker sequence
		Markers res = tree.query(marker);
		if (res.isEmpty()) return null;

		// Return the first markerSeq containing 'marker'
		// Note: We should look for the 'best'. But the sequences are
		//       be maximal by construction (when the database is built).
		//       So we can just return the first one (and only one) we
		//       find. The loop is necessary to filter out 'Chromosome'.
		for (Marker m : res)
			if (m.includes(marker) && (m instanceof MarkerSeq)) //
				return (MarkerSeq) m;

		return null;
	}

	/**
	 * Get sequence for a marker
	 */
	public String querySequence(Marker marker) {
		MarkerSeq ms = queryMarkerSequence(marker);
		if (ms == null) return null;

		// Calculate start and end coordiantes
		int sstart = marker.getStart() - ms.getStart();
		int ssend = marker.size() + sstart;
		String seq = ms.getSequence().substring(sstart, ssend);

		// Return sequence in same direction as 'marker'
		if (marker.isStrandMinus()) seq = GprSeq.reverseWc(seq);
		return seq;
	}

	public void reset() {
		intervalForest = new IntervalForest();
	}

	/**
	 * Save genomic sequence into separate files (per chromosome)
	 */
	public void save(Config config) {
		if (isEmpty()) return; // Nothing to do

		ArrayList<String> chrNames = new ArrayList<String>();
		chrNames.addAll(intervalForest.getTreeNames());
		Collections.sort(chrNames);

		for (String chr : chrNames)
			save(chr);
	}

	/**
	 * Save sequences from chromosome 'chr' to a binary file
	 */
	void save(String chr) {
		if (!intervalForest.hasTree(chr)) {
			if (verbose) Timer.showStdErr("No tree found for chromosome '" + chr + "'");
			return;
		}

		// OK, there is something to save => Save markers to file
		IntervalTree tree = intervalForest.getTree(chr);
		String fileName = Config.get().getFileNameSequence(chr);
		if (verbose) Timer.showStdErr("Saving sequences for chromosome '" + chr + "' to file '" + fileName + "'");
		tree.getIntervals().save(fileName);
	}

	public void setDisableLoad(boolean disableLoad) {
		this.disableLoad = disableLoad;
	}

	public void setVerbose(boolean verbose) {
		GenomicSequences.verbose = verbose;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Genomic sequences '" + genome.getId() + "'\n");

		long sumMarkers = 0;
		long sumLen = 0;
		for (String chr : intervalForest.getTreeNames()) {
			IntervalTree tree = intervalForest.getTree(chr);

			// Calculate total sequence length stored
			long len = 0;
			for (Marker m : tree.getIntervals()) {
				len += m.size();
				sumLen += m.size();
			}

			sumMarkers += tree.getIntervals().size();

			sb.append("\t" + chr + "\t" + tree.size() + "\t" + len + "\n");
		}
		sb.append("\tTOTAL\t" + sumMarkers + "\t" + sumLen + "\n");

		return sb.toString();
	}
}

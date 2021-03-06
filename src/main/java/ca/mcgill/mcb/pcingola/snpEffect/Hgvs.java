package ca.mcgill.mcb.pcingola.snpEffect;

import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;

/**
 * HGSV notation
 *
 * References: http://www.hgvs.org/
 *
 * @author pcingola
 */
public class Hgvs {

	// Don't show sequences that are too long
	public static final int MAX_SEQUENCE_LEN_HGVS = 100;

	protected VariantEffect variantEffect;
	protected Variant variant;
	protected Marker marker;
	protected Transcript tr;
	protected Genome genome;

	protected boolean duplication;
	protected boolean strandPlus, strandMinus;

	public static String parseTranscript(String hgvs) {
		int idxTr = hgvs.indexOf(':');
		if (idxTr < 0) return null;
		return hgvs.substring(0, idxTr);
	}

	public static String removeTranscript(String hgvs) {
		int idxTr = hgvs.indexOf(':');
		if (idxTr < 0) return hgvs;
		return hgvs.substring(idxTr + 1);
	}

	public Hgvs(VariantEffect variantEffect) {
		this.variantEffect = variantEffect;
		variant = variantEffect.getVariant();
		marker = variantEffect.getMarker();
		tr = variantEffect.getTranscript();
		genome = marker != null ? marker.getGenome() : null;
		initStrand();
	}

	protected void initStrand() {
		// Strand information
		if (tr != null) strandMinus = tr.isStrandMinus();
		else if (marker != null) strandMinus = marker.isStrandMinus();

		strandPlus = !strandMinus;
	}

}

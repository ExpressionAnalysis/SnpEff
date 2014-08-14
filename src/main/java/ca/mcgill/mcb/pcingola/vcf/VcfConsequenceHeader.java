package ca.mcgill.mcb.pcingola.vcf;

import java.util.HashMap;
import java.util.Map;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;

/**
 * An 'CSQ' entry in a vcf header line
 *
 * @author pablocingolani
 */
public class VcfConsequenceHeader {

	VcfInfo vcfInfoCsq;
	String fieldNames[];
	Map<String, Integer> fieldNum;

	public VcfConsequenceHeader(VcfFileIterator vcf) {
		parseCsqHeader(vcf);
	}

	public String[] getFieldNames() {
		return fieldNames;
	}

	public Integer getFieldNum(String fieldName) {
		return fieldNum.get(fieldName);
	}

	void parseCsqHeader(VcfFileIterator vcf) {
		vcfInfoCsq = vcf.getVcfHeader().getVcfInfo(VcfConsequence.VCF_INFO_CSQ_NAME);
		if (vcfInfoCsq == null) throw new RuntimeException("Cannot find " + VcfConsequence.VCF_INFO_CSQ_NAME + " in VCF header");

		String descr = vcfInfoCsq.getDescription();
		String fields = descr.substring(descr.lastIndexOf(':'));
		fieldNames = fields.split("\\|");

		fieldNum = new HashMap<String, Integer>();
		for (int i = 0; i < fieldNames.length; i++)
			fieldNum.put(fieldNames[i], i);
	}
}

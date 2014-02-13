#!/bin/sh -e

zcat ORI/c_elegans.PRJNA13758.WS241.annotations.gff3.gz \
        | grep -v SNP \
        | grep -v translated_nucleotide_match \
        | grep -v repeat_region \
        | grep -v inverted_repeat \
        | grep -v tandem_repeat \
        | grep -v nucleotide_match \
		| grep -v transposable_element_pseudogene \
		| grep -v regulatory_region \
		| grep -v promoter \
		| grep -v transposable_element_CDS \
		| grep -v transposable_element \
		| grep -v possible_base_call_error \
		| grep -v operon \
		| grep -v base_call_error_correction \
		| grep -v pseudogenic_transcript \
		| grep -v enhancer \
		| grep -v polyA_signal_sequence \
		| grep -v G_quartet \
		| grep -v duplication \
		| grep -v histone_binding_site \
		| grep -v sequence_alteration \
		| grep -v assembly_component \
		| grep -v DNAseI_hypersensitive_site \
		| grep -v SL2_acceptor_site \
		| grep -v substitution \
		| grep -v protein_coding_primary_transcript \
		| grep -v transposable_element_insertion_site \
		| grep -v SL1_acceptor_site \
		| grep -v experimental_result_region \
		| grep -v nucleotide_match \
		| grep -v complex_substitution \
		| grep -v tandem_repeat \
		| grep -v inverted_repeat \
		| grep -v TSS_region \
		| grep -v polyA_site \
		| grep -v binding_site \
		| grep -v transcription_end_site \
		| grep -v insertion_site \
		| grep -v PCR_product \
		| grep -v repeat_region \
		| grep -v translated_nucleotide_match \
		| grep -v deletion \
		| grep -v sequence_motif \
		| grep -v low_complexity_region \
		| grep -v mRNA_region \
		| grep -v SAGE_tag \
		| grep -v TF_binding_site \
		| grep -v intron \
		| grep -v RNAi_reagent \
		| grep -v reagent \
		| grep -v conserved_region \
		| grep -v SNP \
		| grep -v point_mutation \
		| grep -v transcript_region \
		| grep -v transcribed_fragment \
		| grep -v expressed_sequence_match \
		| sed "s/Transcript://g" \
		| sed "s/Gene://g" \
        > genes.gff


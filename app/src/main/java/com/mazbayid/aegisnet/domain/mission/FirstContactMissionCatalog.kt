package com.mazbayid.aegisnet.domain.mission

data class AuthoredMission(
    val sequenceNumber: Int,
    val chapterId: String,
    val briefing: String,
    val learningConcepts: List<String>,
    val definition: MissionDefinition
)

/**
 * Canonical learning sequence for Campaign 01 / First Contact.
 * Evidence fixtures and story dialogue are deliberately authored separately.
 */
object FirstContactMissionCatalog {

    const val CHAPTER_ID = "campaign_01_first_contact"
    const val OPERATION_ID = "first_contact_01"

    val missions: List<AuthoredMission> = listOf(
        mission(
            number = 1,
            id = "fc_m01_first_signal",
            title = "Sinyal Pertama",
            briefing = "Baca fragmen pesan yang diterima A.E.G.I.S. dan laporkan isi yang benar-benar terlihat.",
            concepts = listOf("variables", "print()"),
            objectiveId = "report_message_fragment",
            objectiveDescription = "Simpan fragmen pesan dalam variabel dan tampilkan isinya.",
            approach = "python_variable_and_print",
            outcome = "message_fragment_reported",
            requiredEvidence = setOf("message_fragment"),
            requiredOutput = setOf("ACCESS GRANTED")
        ),
        mission(
            number = 2,
            id = "fc_m02_identity_check",
            title = "Verifikasi Identitas",
            briefing = "Gunakan variabel bernama untuk menyimpan dan menampilkan informasi identitas yang diberikan oleh briefing resmi.",
            concepts = listOf("named variables", "output"),
            objectiveId = "report_identity_fields",
            objectiveDescription = "Simpan nilai pada variabel bernama dan tampilkan hasilnya.",
            approach = "python_named_variables",
            outcome = "identity_fields_reported",
            requiredEvidence = setOf("identity_record"),
            requiredOutput = setOf("RECRUIT-014", "FIRSTCONTACT")
        ),
        mission(
            number = 3,
            id = "fc_m03_system_trace_audit",
            title = "Audit Jejak Sistem",
            briefing = "Periksa log operasi dan hitung entri ERROR tanpa mengubah sumber aslinya.",
            concepts = listOf("for", "if", "counting"),
            objectiveId = "count_error_entries",
            objectiveDescription = "Telusuri setiap baris log dan hitung entri ERROR.",
            approach = "python_loop_and_condition",
            outcome = "error_entries_counted",
            requiredEvidence = setOf("system_trace_log"),
            requiredOutput = setOf("ERROR_COUNT=2")
        ),
        mission(
            number = 4,
            id = "fc_m04_record_inventory",
            title = "Inventaris Rekaman",
            briefing = "Periksa inventaris rekaman dan laporkan jumlah item yang benar-benar tersedia.",
            concepts = listOf("len()"),
            objectiveId = "count_records",
            objectiveDescription = "Hitung jumlah rekaman dari koleksi yang diberikan.",
            approach = "python_len_collection",
            outcome = "record_count_reported",
            requiredEvidence = setOf("record_inventory"),
            requiredOutput = setOf("RECORD_COUNT=5")
        ),
        mission(
            number = 5,
            id = "fc_m05_reference_trace",
            title = "Penelusuran Referensi",
            briefing = "Temukan referensi yang relevan, pertahankan indeks sumber, dan jangan mengarang posisi data.",
            concepts = listOf("enumerate()", "filtering", "source indices"),
            objectiveId = "trace_reference",
            objectiveDescription = "Telusuri koleksi dengan indeks sumber dan saring referensi yang dicari.",
            approach = "python_enumerate_and_filter",
            outcome = "reference_source_index_verified",
            requiredEvidence = setOf("reference_catalog"),
            requiredOutput = setOf("SOURCE_INDEX=2|RX-14|RECEIPT")
        ),
        mission(
            number = 6,
            id = "fc_m06_two_source_comparison",
            title = "Perbandingan Dua Sumber",
            briefing = "Bandingkan catatan A.E.G.I.S. dan ARCHIVE untuk referensi transmisi yang sama.",
            concepts = listOf("list indexing", "source comparison"),
            objectiveId = "compare_receipt_records",
            objectiveDescription = "Ambil dan bandingkan dua catatan dari sumber yang berbeda.",
            approach = "python_list_indexing",
            outcome = "two_source_records_compared"
        ),
        mission(
            number = 7,
            id = "fc_m07_time_normalization",
            title = "Normalisasi Waktu",
            briefing = "Ubah komponen waktu menjadi bilangan dan hitung selisih bertanda antara dua catatan.",
            concepts = listOf("int()", "indexing", "signed difference"),
            objectiveId = "calculate_signed_time_delta",
            objectiveDescription = "Normalisasi komponen waktu dan pertahankan tanda selisih.",
            approach = "python_int_and_signed_difference",
            outcome = "signed_time_delta_reported"
        ),
        mission(
            number = 8,
            id = "fc_m08_delta_validation",
            title = "Validasi Selisih",
            briefing = "Validasi besar selisih waktu dan pisahkan fakta teramati dari penyebab yang belum diketahui.",
            concepts = listOf("abs()", "absolute difference", "responsible uncertainty"),
            objectiveId = "validate_absolute_delta",
            objectiveDescription = "Hitung selisih absolut dari dua catatan penerimaan.",
            approach = "python_abs_validation",
            outcome = "four_second_delta_verified",
            additionalObjectives = listOf(
                MissionObjective(
                    id = "report_unresolved_cause",
                    description = "Nyatakan bahwa penyebab selisih belum dapat ditetapkan dari bukti yang tersedia.",
                    acceptedApproachIds = setOf("evidence_based_uncertainty"),
                    acceptedOutcomeIds = setOf("cause_not_established")
                )
            )
        )
    )

    fun find(missionId: String): AuthoredMission? =
        missions.firstOrNull { it.definition.id == missionId }

    private fun mission(
        number: Int,
        id: String,
        title: String,
        briefing: String,
        concepts: List<String>,
        objectiveId: String,
        objectiveDescription: String,
        approach: String,
        outcome: String,
        requiredEvidence: Set<String> = emptySet(),
        requiredOutput: Set<String> = emptySet(),
        additionalObjectives: List<MissionObjective> = emptyList()
    ) = AuthoredMission(
        sequenceNumber = number,
        chapterId = CHAPTER_ID,
        briefing = briefing,
        learningConcepts = concepts,
        definition = MissionDefinition(
            id = id,
            operationId = OPERATION_ID,
            title = title,
            objectives = listOf(
                MissionObjective(
                    id = objectiveId,
                    description = objectiveDescription,
                    acceptedApproachIds = setOf(approach),
                    acceptedOutcomeIds = setOf(outcome),
                    requiredEvidenceIds = requiredEvidence,
                    requiredOutputContains = requiredOutput,
                    requiresSuccessfulRun = true
                )
            ) + additionalObjectives
        )
    )
}

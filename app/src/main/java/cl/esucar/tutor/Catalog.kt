package cl.esucar.tutor

/** Contenido ilustrativo para el MVP; requiere validación del docente antes de uso académico real. */
object Catalog {
    val stages = listOf(
        Stage("problema", 1, "Planteamiento del problema", "Delimita una situación investigable y explica por qué merece ser estudiada.",
            "Informe editable: contexto, problema, pregunta, objetivo general, objetivos específicos y justificación.",
            listOf("Describe el contexto sin incluir datos personales ni operativos sensibles.", "Delimita el problema por población, espacio y período.", "Formula una pregunta clara que pueda responderse con evidencia.", "Redacta un objetivo general y objetivos específicos coherentes con la pregunta.", "Explica relevancia, viabilidad y límites del estudio."),
            listOf("Claridad y delimitación del problema", "Coherencia entre pregunta y objetivos", "Justificación sustentada y viabilidad"), 7),
        Stage("marco", 1, "Marco teórico", "Organiza antecedentes y conceptos que fundamentan tu investigación.",
            "Informe con antecedentes, conceptos centrales y lista de referencias según la pauta del docente.",
            listOf("Selecciona palabras clave y busca fuentes académicas pertinentes.", "Registra autor, año, título y procedencia de cada fuente.", "Compara hallazgos y reconoce vacíos de conocimiento.", "Organiza conceptos por temas vinculados a tus objetivos.", "Revisa que cada cita tenga su referencia y evita copiar sin atribución."),
            listOf("Pertinencia y calidad de fuentes", "Síntesis crítica de antecedentes", "Trazabilidad entre citas y referencias"), 21),
        Stage("metodo", 2, "Diseño metodológico", "Define cómo obtendrás evidencia para responder tu pregunta.",
            "Diseño: enfoque, alcance, participantes, técnicas, instrumentos, procedimiento y resguardos éticos.",
            listOf("Justifica un enfoque cualitativo, cuantitativo o mixto.", "Define población, muestra y criterios de selección.", "Relaciona cada objetivo con una técnica de recolección.", "Diseña y revisa tus instrumentos con el docente.", "Documenta consentimiento, confidencialidad y almacenamiento seguro."),
            listOf("Coherencia del diseño con los objetivos", "Justificación de participantes e instrumentos", "Resguardos éticos y factibilidad"), 35),
        Stage("datos", 2, "Recogida de información", "Aplica el plan aprobado y registra la evidencia de forma ordenada.",
            "Bitácora de aplicación e informe de recolección; anexos anonimizados y autorizados.",
            listOf("Confirma aprobación del docente y autorizaciones necesarias.", "Realiza un piloto y ajusta el instrumento si corresponde.", "Aplica el procedimiento de manera consistente.", "Anonimiza y organiza los registros de investigación.", "Documenta incidencias y límites de la recolección."),
            listOf("Cumplimiento del procedimiento", "Calidad y organización de los registros", "Protección de participantes y evidencia"), 49),
        Stage("analisis", 3, "Análisis de resultados", "Transforma los datos en hallazgos relacionados con tus objetivos.",
            "Capítulo de resultados y discusión con tablas o categorías explicadas y fuentes identificadas.",
            listOf("Revisa calidad y consistencia de los datos.", "Aplica el análisis definido en el diseño metodológico.", "Presenta resultados por objetivo con tablas o categorías.", "Distingue evidencia, interpretación y limitaciones.", "Contrasta tus hallazgos con el marco teórico."),
            listOf("Trazabilidad de resultados con la evidencia", "Rigor y claridad del análisis", "Discusión vinculada a los objetivos"), 63),
        Stage("final", 3, "Informe final y presentación", "Integra tu trabajo, responde la pregunta y prepara la defensa.",
            "Informe final y presentación. Extensión, tipografía y tiempos definidos por el docente.",
            listOf("Redacta conclusiones que respondan a los objetivos.", "Explicita limitaciones y recomendaciones sustentadas.", "Verifica estructura, numeración, citas, referencias y anexos.", "Revisa la pauta de evaluación con tu profesor guía.", "Ensaya una presentación clara y prepara respuestas sobre tu método."),
            listOf("Integración y coherencia del informe", "Conclusiones respaldadas por resultados", "Claridad de presentación y dominio metodológico"), 77)
    )
    val faqs = listOf(
        Faq("¿Cómo delimito mi problema?", "Metodología", "Identifica una situación concreta, la población, el contexto y el período. Explica qué se conoce y qué falta investigar. Evita preguntas tan amplias que no puedas responder en el tiempo disponible."),
        Faq("¿Cómo redacto un objetivo?", "Metodología", "Usa un verbo en infinitivo que indique el propósito, por ejemplo describir, analizar o comparar. Precisa qué estudiarás y en qué contexto. Los objetivos específicos deben contribuir al general."),
        Faq("¿Qué enfoque debo elegir?", "Metodología", "Parte de la pregunta: explorar experiencias puede requerir un enfoque cualitativo; medir frecuencias o relaciones, uno cuantitativo. Un enfoque mixto debe justificar cómo se integran ambos. Valida la elección con tu docente."),
        Faq("¿Qué diferencia hay entre población y muestra?", "Metodología", "La población es el conjunto de unidades sobre el que se centra el estudio. La muestra es el subconjunto que observarás. Justifica criterios de selección y las limitaciones de generalización."),
        Faq("¿Qué incluyo en el marco teórico?", "Metodología", "Antecedentes relevantes, conceptos y perspectivas que ayuden a comprender el problema. Relaciona fuentes entre sí y con tus objetivos; no te limites a una lista de definiciones."),
        Faq("¿Cómo organizo el análisis?", "Metodología", "Sigue tus objetivos y el diseño aprobado. Separa los resultados observados de su interpretación. Explica cómo obtuviste las categorías, tablas o indicadores y reconoce los límites de la evidencia."),
        Faq("¿Dónde consulto las normas APA?", "Citación", "Consulta el manual o las orientaciones APA que indique tu docente y el sitio oficial apastyle.apa.org. Este MVP no reemplaza esa pauta. Verifica siempre autoría, fecha, título y procedencia de cada fuente."),
        Faq("¿Qué diferencia hay entre cita y referencia?", "Citación", "La cita identifica dentro del texto la fuente de una idea. La referencia contiene los datos necesarios para localizarla. Revisa que las citas y la lista final correspondan entre sí según la norma exigida."),
        Faq("¿Debo citar una paráfrasis?", "Citación", "Sí. Expresar una idea con tus propias palabras no elimina su autoría. Identifica la fuente y comprueba que la paráfrasis conserve el sentido. Revisa con el docente las exigencias de localización de la idea."),
        Faq("¿Cuál es la estructura del informe?", "Formato", "Como orientación: portada, índice, introducción, problema y objetivos, marco teórico, metodología, resultados, discusión, conclusiones, referencias y anexos. La estructura definitiva la establece tu docente."),
        Faq("¿Qué tipografía o extensión debo usar?", "Formato", "El caso no especifica tipografía, extensión ni interlineado institucional. Revisa el formato requerido en cada hito y solicita la pauta oficial al profesor. No asumas que estos ejemplos son reglas de ESUCAR."),
        Faq("¿Cómo mejoro mi redacción académica?", "Formato", "Dedica cada párrafo a una idea, utiliza términos precisos y conecta afirmaciones con evidencia. Revisa coherencia, ortografía y atribución de fuentes. Evita afirmaciones generales que no puedas respaldar."),
        Faq("¿Qué significa enviado?", "Entregas", "En este prototipo significa que marcaste tu avance para revisión local. No envía archivos al docente por internet. Comparte el documento por el canal que indique la asignatura y usa el modo docente para demostrar la revisión."),
        Faq("¿Puedo trabajar sin internet?", "Entregas", "Sí. Las guías, FAQ, listas, fechas y estados se guardan en el dispositivo. La actualización opcional de FAQ requiere una fuente HTTPS configurada. Los recordatorios requieren permiso de notificaciones."),
        Faq("¿Qué hago si un hito se retrasa?", "Entregas", "Revisa qué falta en la lista, registra tus dificultades y consulta al docente para acordar un plan. La aplicación identifica hitos vencidos; solamente el modo docente puede modificar fechas y aprobar avances.")
    )
}

package lucaslimb.com.github.superautoito.engine

import lucaslimb.com.github.superautoito.model.Character

data class AbilityContext(
    val caster: Character,                    // Quem está usando a habilidade
    val casterIndex: Int,                     // Posição no deck
    val allies: MutableList<Character>,       // Time aliado
    val enemies: MutableList<Character>,      // Time inimigo
    val graveyard: List<Character>,           // Cartas mortas no round
    val isPlayerTeam: Boolean,                // Se é do time do jogador
    val victim: Character? = null,            // Carta que foi morta (ON_KILL)
    val victimPreviousDefense: Int = 0,       // Defesa da vítima antes de morrer
    val killer: Character? = null             // Quem matou (ON_DEATH)
)
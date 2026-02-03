package lucaslimb.com.github.superautoito.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import lucaslimb.com.github.superautoito.R

enum class CharacterType(val desc: String) {
    CHILD("Criança"),
    SUPERNATURAL("Sobrenatural"),
    CORPSE("Cadáver"),
    COMMON("Comum"),
    MASC("Masculino"),
    FEM("Feminino")
}

enum class TriggerType(val desc: String) {
    ON_KILL("Ao matar"),
    ON_DEATH("Ao morrer"),
    ON_PREPARE("Antes da batalha"),
    BEFORE_BATTLE("Ao iniciar a batalha"),
    AFTER_BATTLE("Após a batalha"),
    NONE("Em nenhum momento")
}

//add power desc
@Parcelize
data class Character(
    val id: Int,
    val name: String,
    val attack: Int,
    val defense: Int,
    val types: List<CharacterType>,
    val trigger: TriggerType,
    val imageResId: Int = 0,
    val power: String = "",
) : Parcelable {

    companion object {
        fun getDefaultCharacters(): List<Character> {
            return listOf(
                Character(
                    id = 27,
                    name = "Oshikiri",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_PREPARE,
                    imageResId = R.drawable.oshikiri,
                    power = "+50% de DEFESA para amigo SOBRENATURAL"
                ),
                Character(
                    id = 16,
                    name = "Professor Okabe",
                    attack = 4,
                    defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_PREPARE,
                    imageResId = R.drawable.professor,
                    power ="+50% de ATAQUE contra inimigo SOBRENATURAL"
                ),
                Character(
                    id = 12,
                    name = "Moço do Sorvete",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_PREPARE,
                    imageResId = R.drawable.moco,
                    power ="-50% de DEFESA para todas as CRIANÇAS"
                ),
                Character(
                    id = 8,
                    name = "Kuriko",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.ON_PREPARE,
                    imageResId = R.drawable.kuriko,
                    power ="+50% de DEFESA para amigo CRIANÇA"
                ),
                Character(
                    id = 3,
                    name = "Fuchi",
                    attack = 3,
                    defense = 5,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.ON_PREPARE,
                    imageResId = R.drawable.fuchi,
                    power ="+1 de ATAQUE contra inimigo MASCULINO"
                ),
                Character(
                    id = 4,
                    name = "Garoto da Encruzilhada",
                    attack = 2,
                    defense = 3,
                    types = listOf(CharacterType.SUPERNATURAL, CharacterType.CHILD, CharacterType.MASC),
                    trigger = TriggerType.ON_PREPARE,
                    imageResId = R.drawable.garoto,
                    power = "+1 de ATAQUE contra amigo FEMININO"
                ),
                Character(
                    id = 19,
                    name = "Yuki",
                    attack = 1,
                    defense = 2,
                    types = listOf(CharacterType.CORPSE, CharacterType.FEM),
                    trigger = TriggerType.ON_PREPARE,
                    imageResId = R.drawable.yuki,
                    power = "+50% de DEFESA para amigo CADÁVER"
                ),
                Character(
                    id = 15,
                    name = "Tio Kingoro",
                    attack = 2,
                    defense = 1,
                    types = listOf(CharacterType.CORPSE, CharacterType.MASC),
                    trigger = TriggerType.ON_PREPARE,
                    imageResId = R.drawable.tio,
                    power = "-50% de DEFESA para inimigo CADÁVER"
                ),
                Character(
                    id = 11,
                    name = "Misuzu",
                    attack = 1,
                    defense = 2,
                    types = listOf(CharacterType.CORPSE, CharacterType.FEM),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.misuzu,
                    power = "Inverte ATAQUE e DEFESA de inimigo aleatório"
                ),
                Character(
                    id = 26,
                    name = "Yuko",
                    attack = 2,
                    defense = 1,
                    types = listOf(CharacterType.CHILD, CharacterType.FEM),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.yuko,
                    power = "Inimigo aleatório removido do DECK no PRÓXIMO round"
                ),
                Character(
                    id = 5,
                    name = "Mulher das Costelas",
                    attack = 3,
                    defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.mulher,
                    power = "-1 de DEFESA para inimigo aleatório"
                ),
                Character(
                    id = 30,
                    name = "Goro",
                    attack = 1,
                    defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.goro,
                    power = "Troca a POSIÇÃO de inimigo aleatório"
                ),
                Character(
                    id = 31,
                    name = "Binzo",
                    attack = 5,
                    defense = 3,
                    types = listOf(CharacterType.CHILD, CharacterType.MASC),
                    trigger = TriggerType.BEFORE_BATTLE,
                    imageResId = R.drawable.binzo,
                    power = "-1 de ATAQUE para inimigo aleatório"
                ),
                Character(
                    id = 1,
                    name = "Tomie",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.tomie,
                    power = "Volta no FIM do DECK (apenas 1x)"
                ),
                Character(
                    id = 2,
                    name = "Souichi",
                    attack = 3,
                    defense = 2,
                    types = listOf(CharacterType.CHILD, CharacterType.MASC),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.souichi,
                    power = "Invoca uma ARANHA 1/1"
                ),
                Character(
                    id = 29,
                    name = "Kazuya Tani",
                    attack = 2,
                    defense = 1,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.kazuya,
                    power = "Invoca um MORCEGO 1/1"
                ),
                Character(
                    id = 22,
                    name = "Terumi",
                    attack = 1,
                    defense = 3,
                    types = listOf(CharacterType.SUPERNATURAL, CharacterType.CORPSE, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.terumi,
                    power = "Invoca uma CABEÇA 1/1"
                ),
                Character(
                    id = 6,
                    name = "Chiemi",
                    attack = 3,
                    defense = 1,
                    types = listOf(CharacterType.CORPSE, CharacterType.SUPERNATURAL, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.chiemi,
                    power = "Invoca o INIMIGO que a MATOU"
                ),
                Character(
                    id = 9,
                    name = "Soldado Furukawa",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.CORPSE, CharacterType.MASC),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.soldado,
                    power = "Invoca um dublê de uma CARTA aleatória do DECK (se houver)"
                ),
                Character(
                    id = 24,
                    name = "Kumi",
                    attack = 1,
                    defense = 2,
                    types = listOf(CharacterType.CORPSE, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.kumi,
                    power = "Invoca a ÚLTIMA carta que MORREU"
                ),
                Character(
                    id = 7,
                    name = "Reanimador",
                    attack = 4,
                    defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.reanimador,
                    power = "Reanima uma CARTA morta no ROUND com 50% de stats"
                ),
                Character(
                    id = 23,
                    name = "Maya",
                    attack = 2,
                    defense = 1,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.maya,
                    power = "+50% de DEFESA ao amigo ao LADO"
                ),
                Character(
                    id = 14,
                    name = "Nakayama",
                    attack = 1,
                    defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.FEM),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.nakayama,
                    power = "-1 de DEFESA a TODOS"
                ),
                Character(
                    id = 10,
                    name = "Manchas do Beco",
                    attack = 3,
                    defense = 3,
                    types = listOf(CharacterType.CORPSE, CharacterType.SUPERNATURAL),
                    trigger = TriggerType.ON_DEATH,
                    imageResId = R.drawable.manchas,
                    power = "-1 de DEFESA para o INIMIGO que o MATOU"
                ),
                Character(
                    id = 32,
                    name = "Misaki",
                    attack = 3,
                    defense = 2,
                    types = listOf(CharacterType.CHILD, CharacterType.FEM),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.misaki,
                    power = "Rouba 50% da DEFESA de quem MATOU"
                ),
                Character(
                    id = 21,
                    name = "Numei",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.numei,
                    power = "-50% de DEFESA para o PRÓXIMO inimigo"
                ),
                Character(
                    id = 20,
                    name = "Ryo Tsukano",
                    attack = 3,
                    defense = 1,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.ryo,
                    power = "Rouba o PODER de quem MATOU"
                ),
                Character(
                    id = 28,
                    name = "Frankenstein",
                    attack = 3,
                    defense = 3,
                    types = listOf(CharacterType.SUPERNATURAL, CharacterType.MASC),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.frank,
                    power = "+50% de ATAQUE a si próprio"
                ),
                Character(
                    id = 25,
                    name = "Ayumi",
                    attack = 3,
                    defense = 3,
                    types = listOf(CharacterType.CORPSE, CharacterType.SUPERNATURAL, CharacterType.FEM),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.ayumi,
                    power = "+50% de DEFESA a si próprio"
                ),
                Character(
                    id = 17,
                    name = "Esculturas Sem Cabeça",
                    attack = 4,
                    defense = 3,
                    types = listOf(CharacterType.SUPERNATURAL, CharacterType.FEM),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.esculturas,
                    power = "-50% de ATAQUE para o PRÓXIMO inimigo"
                ),
                Character(
                    id = 18,
                    name = "Hideo",
                    attack = 2,
                    defense = 1,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.ON_KILL,
                    imageResId = R.drawable.hideo,
                    power = "+1 de ATAQUE a si próprio"
                ),
                Character(
                    id = 13,
                    name = "Tomoo",
                    attack = 3,
                    defense = 2,
                    types = listOf(CharacterType.COMMON, CharacterType.MASC),
                    trigger = TriggerType.AFTER_BATTLE,
                    imageResId = R.drawable.tomoo,
                    power = "Inimigo aleatório removido do DECK no PRÓXIMO round"
                )
            )
        }
    }
}

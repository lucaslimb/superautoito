package lucaslimb.com.github.superautoito.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import lucaslimb.com.github.superautoito.R

enum class CharacterType {
    CHILD,
    SUPERNATURAL,
    CORPSE,
    COMMON
}

enum class TriggerType {
    ON_KILL,
    ON_DEATH,
    ON_PREPARE,
    BEFORE_BATTLE,
    AFTER_BATTLE,
    NONE
}

@Parcelize
data class Character(
    val id: Int,
    val name: String,
    val attack: Int,
    val defense: Int,
    val types: List<CharacterType>,
    val trigger: TriggerType,
    val image: Int = 0
) : Parcelable {

    companion object {
        fun getDefaultCharacters(): List<Character> {
            return listOf(
                Character(
                    id = 27,
                    name = "Oshikiri",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_PREPARE,
                    image = R.drawable.oshikiri
                ),
                Character(
                    id = 16,
                    name = "Professor Okabe",
                    attack = 4,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_PREPARE,
                    image = R.drawable.professor
                ),
                Character(
                    id = 12,
                    name = "Moço do Sorvete",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_PREPARE,
                    image = R.drawable.moco
                ),
                Character(
                    id = 8,
                    name = "Kuriko",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_PREPARE,
                    image = R.drawable.kuriko
                ),
                Character(
                    id = 3,
                    name = "Fuchi",
                    attack = 3,
                    defense = 5,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_PREPARE,
                    image = R.drawable.fuchi
                ),
                Character(
                    id = 4,
                    name = "Garoto da Encruzilhada",
                    attack = 2,
                    defense = 3,
                    types = listOf(CharacterType.SUPERNATURAL, CharacterType.CHILD),
                    trigger = TriggerType.ON_PREPARE,
                    image = R.drawable.garoto
                ),
                Character(
                    id = 19,
                    name = "Yuki",
                    attack = 1,
                    defense = 2,
                    types = listOf(CharacterType.CORPSE),
                    trigger = TriggerType.ON_PREPARE,
                    image = R.drawable.yuki
                ),
                Character(
                    id = 15,
                    name = "Tio Kingoro",
                    attack = 2,
                    defense = 1,
                    types = listOf(CharacterType.CORPSE),
                    trigger = TriggerType.ON_PREPARE,
                    image = R.drawable.tio
                ),
                Character(
                    id = 11,
                    name = "Misuzu",
                    attack = 1,
                    defense = 2,
                    types = listOf(CharacterType.CORPSE),
                    trigger = TriggerType.BEFORE_BATTLE,
                    image = R.drawable.misuzu
                ),
                Character(
                    id = 26,
                    name = "Yuko",
                    attack = 2,
                    defense = 1,
                    types = listOf(CharacterType.CHILD),
                    trigger = TriggerType.BEFORE_BATTLE,
                    image = R.drawable.yuko
                ),
                Character(
                    id = 5,
                    name = "Mulher das Costelas",
                    attack = 3,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.BEFORE_BATTLE,
                    image = R.drawable.mulher
                ),
                Character(
                    id = 30,
                    name = "Goro",
                    attack = 1,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.BEFORE_BATTLE,
                    image = R.drawable.goro
                ),
                Character(
                    id = 31,
                    name = "Binzo",
                    attack = 5,
                    defense = 3,
                    types = listOf(CharacterType.CHILD),
                    trigger = TriggerType.BEFORE_BATTLE,
                    image = R.drawable.binzo
                ),
                Character(
                    id = 1,
                    name = "Tomie",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_DEATH,
                    image = R.drawable.tomie
                ),
                Character(
                    id = 2,
                    name = "Souichi",
                    attack = 3,
                    defense = 2,
                    types = listOf(CharacterType.CHILD),
                    trigger = TriggerType.ON_DEATH,
                    image = R.drawable.souichi
                ),
                Character(
                    id = 29,
                    name = "Kazuya Tani",
                    attack = 2,
                    defense = 1,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_DEATH,
                    image = R.drawable.kazuya
                ),
                Character(
                    id = 22,
                    name = "Terumi",
                    attack = 1,
                    defense = 3,
                    types = listOf(CharacterType.SUPERNATURAL, CharacterType.CORPSE),
                    trigger = TriggerType.ON_DEATH,
                    image = R.drawable.terumi
                ),
                Character(
                    id = 6,
                    name = "Chiemi",
                    attack = 3,
                    defense = 1,
                    types = listOf(CharacterType.CORPSE, CharacterType.SUPERNATURAL),
                    trigger = TriggerType.ON_DEATH,
                    image = R.drawable.chiemi
                ),
                Character(
                    id = 9,
                    name = "Soldado Furukawa",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.CORPSE),
                    trigger = TriggerType.ON_DEATH,
                    image = R.drawable.soldado
                ),
                Character(
                    id = 24,
                    name = "Kumi",
                    attack = 1,
                    defense = 2,
                    types = listOf(CharacterType.CORPSE),
                    trigger = TriggerType.ON_DEATH,
                    image = R.drawable.kumi
                ),
                Character(
                    id = 7,
                    name = "Reanimador",
                    attack = 4,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_DEATH,
                    image = R.drawable.reanimador
                ),
                Character(
                    id = 23,
                    name = "Maya",
                    attack = 2,
                    defense = 1,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_DEATH,
                    image = R.drawable.maya
                ),
                Character(
                    id = 14,
                    name = "Nakayama",
                    attack = 1,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_DEATH,
                    image = R.drawable.nakayama
                ),
                Character(
                    id = 10,
                    name = "Manchas do Beco",
                    attack = 3,
                    defense = 3,
                    types = listOf(CharacterType.CORPSE, CharacterType.SUPERNATURAL),
                    trigger = TriggerType.ON_DEATH,
                    image = R.drawable.manchas
                ),
                Character(
                    id = 32,
                    name = "Misaki",
                    attack = 3,
                    defense = 2,
                    types = listOf(CharacterType.CHILD),
                    trigger = TriggerType.ON_KILL,
                    image = R.drawable.misaki
                ),
                Character(
                    id = 21,
                    name = "Numei",
                    attack = 2,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_KILL,
                    image = R.drawable.numei
                ),
                Character(
                    id = 20,
                    name = "Ryo Tsukano",
                    attack = 3,
                    defense = 1,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_KILL,
                    image = R.drawable.ryo
                ),
                Character(
                    id = 28,
                    name = "Frankenstein",
                    attack = 3,
                    defense = 3,
                    types = listOf(CharacterType.SUPERNATURAL),
                    trigger = TriggerType.ON_KILL,
                    image = R.drawable.frank
                ),
                Character(
                    id = 25,
                    name = "Ayumi",
                    attack = 3,
                    defense = 3,
                    types = listOf(CharacterType.CORPSE, CharacterType.SUPERNATURAL),
                    trigger = TriggerType.ON_KILL,
                    image = R.drawable.ayumi
                ),
                Character(
                    id = 17,
                    name = "Esculturas Sem Cabeça",
                    attack = 4,
                    defense = 3,
                    types = listOf(CharacterType.SUPERNATURAL),
                    trigger = TriggerType.ON_KILL,
                    image = R.drawable.esculturas
                ),
                Character(
                    id = 18,
                    name = "Hideo",
                    attack = 2,
                    defense = 1,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.ON_KILL,
                    image = R.drawable.hideo
                ),
                Character(
                    id = 13,
                    name = "Tomoo",
                    attack = 3,
                    defense = 2,
                    types = listOf(CharacterType.COMMON),
                    trigger = TriggerType.AFTER_BATTLE,
                    image = R.drawable.tomoo
                )
            )
        }
    }
}
